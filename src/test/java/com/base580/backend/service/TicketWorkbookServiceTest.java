package com.base580.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import com.base580.backend.model.TicketWorkbookDocument;
import com.base580.backend.model.TicketWorkbookDocument.Captura;
import com.base580.backend.model.TicketWorkbookState;
import com.base580.backend.repository.TicketWorkbookRepository;

@ExtendWith(MockitoExtension.class)
class TicketWorkbookServiceTest {

	@Mock
	private TicketWorkbookRepository repository;

	private TicketWorkbookService service;

	@BeforeEach
	void setUp() {
		service = new TicketWorkbookService(repository);
	}

	private static TicketWorkbookState request(Long version, Map<String, String> capturas) {
		return new TicketWorkbookState(capturas, version, null);
	}

	private static TicketWorkbookDocument stored(long version, String celda, String valor) {
		TicketWorkbookDocument document = new TicketWorkbookDocument();
		document.setId(TicketWorkbookDocument.DEFAULT_ID);
		document.setVersion(version);
		document.setCapturas(List.of(new Captura(celda, valor)));
		return document;
	}

	@Test
	void sinGuardarDevuelveLibroVacioSinVersion() {
		when(repository.findById(TicketWorkbookDocument.DEFAULT_ID)).thenReturn(Optional.empty());

		TicketWorkbookState state = service.get();

		assertTrue(state.getCapturas().isEmpty());
		assertNull(state.getVersion());
	}

	@Test
	void primerGuardadoCreaElDocumentoConLasCapturas() {
		when(repository.findById(TicketWorkbookDocument.DEFAULT_ID)).thenReturn(Optional.empty());
		when(repository.save(any())).thenAnswer(invocation -> {
			TicketWorkbookDocument document = invocation.getArgument(0);
			document.setVersion(0L);
			return document;
		});

		Map<String, String> capturas = new LinkedHashMap<>();
		capturas.put("NORTE 26!A3", "2");
		capturas.put("BASE NTE 25 !B3", "80");
		TicketWorkbookState saved = service.save(request(null, capturas));

		assertEquals(Long.valueOf(0L), saved.getVersion());
		assertEquals("2", saved.getCapturas().get("NORTE 26!A3"));
		assertEquals("80", saved.getCapturas().get("BASE NTE 25 !B3"));
	}

	@Test
	void versionVencidaEsConflictoConElEstadoActual() {
		when(repository.findById(TicketWorkbookDocument.DEFAULT_ID)).thenReturn(Optional.of(stored(3L, "NORTE 26!A3", "5")));

		WorkbookConflictException conflict = assertThrows(WorkbookConflictException.class,
			() -> service.save(request(2L, Map.of("NORTE 26!A3", "1"))));

		assertEquals(Long.valueOf(3L), conflict.getActual().getVersion());
		assertEquals("5", conflict.getActual().getCapturas().get("NORTE 26!A3"));
		verify(repository, never()).save(any());
	}

	@Test
	void escrituraConcurrenteEsConflicto() {
		when(repository.findById(TicketWorkbookDocument.DEFAULT_ID)).thenReturn(Optional.of(stored(3L, "NORTE 26!A3", "5")));
		when(repository.save(any())).thenThrow(new OptimisticLockingFailureException("otra escritura"));

		assertThrows(WorkbookConflictException.class, () -> service.save(request(3L, Map.of("NORTE 26!A3", "1"))));
	}

	@Test
	void rechazaCeldasInvalidas() {
		assertThrows(IllegalArgumentException.class, () -> service.save(request(null, Map.of("sin-referencia", "1"))));
		assertThrows(IllegalArgumentException.class, () -> service.save(request(null, Map.of("NORTE 26!A0", "1"))));
		verify(repository, never()).save(any());
	}

	@Test
	void guardaFormatosJuntoConCapturas() {
		when(repository.findById(TicketWorkbookDocument.DEFAULT_ID)).thenReturn(Optional.empty());
		when(repository.save(any())).thenAnswer(invocation -> {
			TicketWorkbookDocument document = invocation.getArgument(0);
			document.setVersion(0L);
			return document;
		});

		TicketWorkbookState request = new TicketWorkbookState(new LinkedHashMap<>(), null, null);
		Map<String, String> formatos = new LinkedHashMap<>();
		formatos.put("NORTE 26!A3", "{\"b\":true,\"sz\":14}");
		request.setFormatos(formatos);

		TicketWorkbookState saved = service.save(request);

		assertEquals("{\"b\":true,\"sz\":14}", saved.getFormatos().get("NORTE 26!A3"));
	}

	@Test
	void rechazaFormatoConCeldaInvalida() {
		TicketWorkbookState request = new TicketWorkbookState(new LinkedHashMap<>(), null, null);
		request.setFormatos(Map.of("sin-referencia", "{}"));

		assertThrows(IllegalArgumentException.class, () -> service.save(request));
		verify(repository, never()).save(any());
	}
}
