package com.base580.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;

import com.base580.backend.boletos.TicketTariffCatalog;
import com.base580.backend.model.TicketTariffDocument;
import com.base580.backend.repository.TicketTariffRepository;

@ExtendWith(MockitoExtension.class)
class TicketTariffServiceTest {

	@Mock
	private TicketTariffRepository repository;

	@Mock
	private MongoTemplate mongoTemplate;

	@Mock
	private IndexOperations indexOperations;

	private TicketTariffService service;

	@BeforeEach
	void setUp() {
		lenient().when(mongoTemplate.indexOps(TicketTariffDocument.class)).thenReturn(indexOperations);
		service = new TicketTariffService(repository, mongoTemplate);
	}

	@Test
	void calculaNetoAlGuardarYRechazaLaMismaLlave() {
		TicketTariffDocument input = zoneOne();
		input.setNeto(new BigDecimal("1.00"));
		when(repository.existsByBaseAndVigenciaAndAmbitoAndTipoAndZona("NORTE", "2026", "CDMX", "sencillo", "1")).thenReturn(false);
		when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		TicketTariffDocument saved = service.create(input);

		assertEquals(new BigDecimal("59.00"), saved.getNeto());

		when(repository.existsByBaseAndVigenciaAndAmbitoAndTipoAndZona("NORTE", "2026", "CDMX", "sencillo", "1")).thenReturn(true);
		assertThrows(DuplicateTariffException.class, () -> service.create(input));
		verify(repository).save(any());
	}

	@Test
	void tarifaDePruebaSinPlataNoEntraEnLaSemilla() {
		TicketTariffDocument input = new TicketTariffDocument();
		input.setBase("TAPO");
		input.setVigencia("2025");
		input.setAmbito("CDMX");
		input.setTipo("sencillo");
		input.setZona("1");
		input.setPrecio(new BigDecimal("100"));
		input.setDescuento(new BigDecimal("10"));
		input.setSitio(new BigDecimal("5"));
		input.setPlata(null);
		when(repository.existsByBaseAndVigenciaAndAmbitoAndTipoAndZona("TAPO", "2025", "CDMX", "sencillo", "1")).thenReturn(false);
		when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		TicketTariffDocument saved = service.create(input);

		assertEquals(new BigDecimal("85.00"), saved.getNeto());
		assertEquals(null, saved.getPlata());
	}

	@Test
	void noVuelveASembrarSiLaTarifaYaExiste() {
		when(repository.findAll()).thenReturn(TicketTariffCatalog.tariffs());

		service.ensureSeed();

		verify(repository, never()).saveAll(any());
	}

	@Test
	void noDuplicaNorte2026YAgregaLoQueFalta() {
		List<TicketTariffDocument> norte = TicketTariffCatalog.tariffs().stream()
			.filter(tariff -> "NORTE".equals(tariff.getBase())
				&& "2026".equals(tariff.getVigencia())
				&& "CDMX".equals(tariff.getAmbito()))
			.toList();
		assertEquals(26, norte.size());
		when(repository.findAll()).thenReturn(norte);
		when(repository.saveAll(any())).thenAnswer(invocation -> {
			List<TicketTariffDocument> saved = new java.util.ArrayList<>();
			((Iterable<TicketTariffDocument>) invocation.getArgument(0)).forEach(saved::add);
			assertEquals(TicketTariffCatalog.tariffs().size() - 26, saved.size());
			assertTrue(saved.stream().noneMatch(tariff ->
				"NORTE".equals(tariff.getBase()) && "2026".equals(tariff.getVigencia()) && "CDMX".equals(tariff.getAmbito())));
			assertTrue(saved.stream().anyMatch(tariff -> "PTE".equals(tariff.getBase()) && "1".equals(tariff.getZona())));
			assertTrue(saved.stream().anyMatch(tariff -> "A".equals(tariff.getZona()) && "EDOMEX".equals(tariff.getAmbito())));
			return saved;
		});

		service.ensureSeed();
		verify(repository).saveAll(any());
	}

	private static TicketTariffDocument zoneOne() {
		TicketTariffDocument input = new TicketTariffDocument();
		input.setBase("NORTE");
		input.setVigencia("2026");
		input.setAmbito("CDMX");
		input.setTipo("sencillo");
		input.setZona("1");
		input.setPrecio(new BigDecimal("78"));
		input.setDescuento(new BigDecimal("10"));
		input.setSitio(new BigDecimal("6"));
		input.setPlata(new BigDecimal("3"));
		return input;
	}
}
