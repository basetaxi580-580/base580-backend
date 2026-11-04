package com.base580.backend.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.base580.backend.model.TicketWorkbookDocument;
import com.base580.backend.model.TicketWorkbookDocument.Captura;
import com.base580.backend.model.TicketWorkbookState;
import com.base580.backend.repository.TicketWorkbookRepository;

/**
 * Guarda las capturas compartidas de la réplica del Excel de boletos.
 * Usa bloqueo optimista: si la versión enviada no es la actual, responde conflicto
 * con el estado vigente para que el cliente fusione sus celdas.
 */
@Service
public class TicketWorkbookService {

	static final int MAX_CAPTURAS = 5_000;
	static final int MAX_VALOR = 500;
	private static final Pattern CELDA = Pattern.compile("^[^!]{1,64}![A-Z]{1,3}[1-9][0-9]{0,5}$");

	private final TicketWorkbookRepository repository;

	public TicketWorkbookService(TicketWorkbookRepository repository) {
		this.repository = repository;
	}

	public TicketWorkbookState get() {
		return repository.findById(TicketWorkbookDocument.DEFAULT_ID).map(TicketWorkbookService::toState).orElseGet(TicketWorkbookService::empty);
	}

	public TicketWorkbookState save(TicketWorkbookState request) {
		if (request == null) {
			throw new IllegalArgumentException("Solicitud vacía.");
		}
		List<Captura> capturas = validar(request.getCapturas());
		Optional<TicketWorkbookDocument> actual = repository.findById(TicketWorkbookDocument.DEFAULT_ID);
		TicketWorkbookDocument document;
		if (actual.isEmpty()) {
			if (request.getVersion() != null) {
				throw new WorkbookConflictException(empty());
			}
			document = new TicketWorkbookDocument();
			document.setId(TicketWorkbookDocument.DEFAULT_ID);
		} else {
			document = actual.get();
			if (!Objects.equals(document.getVersion(), request.getVersion())) {
				throw new WorkbookConflictException(toState(document));
			}
		}
		document.setCapturas(capturas);
		document.setActualizado(OffsetDateTime.now().toString());
		try {
			return toState(repository.save(document));
		} catch (OptimisticLockingFailureException | DuplicateKeyException exception) {
			throw new WorkbookConflictException(get());
		}
	}

	static List<Captura> validar(Map<String, String> capturas) {
		List<Captura> lista = new ArrayList<>();
		if (capturas == null) {
			return lista;
		}
		if (capturas.size() > MAX_CAPTURAS) {
			throw new IllegalArgumentException("Demasiadas celdas capturadas.");
		}
		for (Map.Entry<String, String> entry : new TreeMap<>(capturas).entrySet()) {
			String celda = entry.getKey();
			String valor = entry.getValue() == null ? "" : entry.getValue();
			if (celda == null || !CELDA.matcher(celda).matches()) {
				throw new IllegalArgumentException("Celda inválida: " + celda);
			}
			if (valor.length() > MAX_VALOR) {
				throw new IllegalArgumentException("Valor demasiado largo en " + celda);
			}
			lista.add(new Captura(celda, valor));
		}
		return lista;
	}

	static TicketWorkbookState toState(TicketWorkbookDocument document) {
		Map<String, String> capturas = new LinkedHashMap<>();
		if (document.getCapturas() != null) {
			for (Captura captura : document.getCapturas()) {
				capturas.put(captura.getCelda(), captura.getValor());
			}
		}
		return new TicketWorkbookState(capturas, document.getVersion(), document.getActualizado());
	}

	static TicketWorkbookState empty() {
		return new TicketWorkbookState(new LinkedHashMap<>(), null, null);
	}
}
