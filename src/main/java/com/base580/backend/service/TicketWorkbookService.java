package com.base580.backend.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.base580.backend.model.TicketWorkbookDocument;
import com.base580.backend.model.TicketWorkbookDocument.Captura;
import com.base580.backend.model.TicketWorkbookDocument.Formato;
import com.base580.backend.model.TicketWorkbookDocument.PestanaPersonalizada;
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
	static final int MAX_ESTILO = 1_000;
	static final int MAX_ESTRUCTURA = 50_000;
	static final int MAX_PESTANAS = 100;
	static final int MAX_NOMBRE_PESTANA = 64;
	private static final Pattern ID_PESTANA = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");
	private static final Set<String> HOJAS_IMPORTADAS = Set.of("BASE NTE 25", "BASE NTE 26", "BASE NORTE ESPECIALES", "BASE TAPO 24", "BASE TAPO 25", "BASE TAPO REZ", "BASE PTE 25", "BASE PTE", "BASE SUR 25", "BASE SUR 26", "TAPO 25", "NORTE 25", "NORTE 26", "NORTE ESPECIALES", "PTE", "SUR 25", "SUR 26", "MANUALES", "TOTAL");
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
		List<Formato> formatos = validarFormatos(request.getFormatos());
		List<PestanaPersonalizada> pestanas = validarPestanas(request.getPestanasPersonalizadas());
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
		document.setFormatos(formatos);
		document.setPestanasPersonalizadas(pestanas);
		document.setEstructura(validarEstructura(request.getEstructura()));
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

	static List<Formato> validarFormatos(Map<String, String> formatos) {
		List<Formato> lista = new ArrayList<>();
		if (formatos == null) {
			return lista;
		}
		if (formatos.size() > MAX_CAPTURAS) {
			throw new IllegalArgumentException("Demasiadas celdas con formato.");
		}
		for (Map.Entry<String, String> entry : new TreeMap<>(formatos).entrySet()) {
			String celda = entry.getKey();
			String estilo = entry.getValue() == null ? "" : entry.getValue();
			if (celda == null || !CELDA.matcher(celda).matches()) {
				throw new IllegalArgumentException("Celda inválida: " + celda);
			}
			if (estilo.length() > MAX_ESTILO) {
				throw new IllegalArgumentException("Formato demasiado largo en " + celda);
			}
			lista.add(new Formato(celda, estilo));
		}
		return lista;
	}

	static String validarEstructura(String estructura) {
		if (estructura == null) {
			return null;
		}
		if (estructura.length() > MAX_ESTRUCTURA) {
			throw new IllegalArgumentException("Estructura de columnas demasiado grande.");
		}
		return estructura;
	}

	static List<PestanaPersonalizada> validarPestanas(List<PestanaPersonalizada> pestanas) {
		List<PestanaPersonalizada> lista = new ArrayList<>();
		if (pestanas == null) return lista;
		if (pestanas.size() > MAX_PESTANAS) throw new IllegalArgumentException("Demasiadas pestañas personalizadas.");
		java.util.Set<String> ids = new java.util.HashSet<>();
		java.util.Set<String> nombres = new java.util.HashSet<>();
		for (PestanaPersonalizada pestana : pestanas) {
			String id = pestana == null ? null : pestana.getId();
			String nombre = pestana == null ? null : pestana.getNombre();
			if (id == null || !ID_PESTANA.matcher(id).matches()) throw new IllegalArgumentException("Identificador de pestaña inválido.");
			if (nombre == null || nombre.isBlank() || nombre.length() > MAX_NOMBRE_PESTANA || nombre.contains("!")) throw new IllegalArgumentException("Nombre de pestaña inválido.");
			String clave = nombre.trim().toUpperCase(java.util.Locale.ROOT);
			if (HOJAS_IMPORTADAS.contains(clave)) throw new IllegalArgumentException("No se puede usar el nombre de una hoja importada.");
			if (!ids.add(id) || !nombres.add(clave)) throw new IllegalArgumentException("Las pestañas deben tener identificador y nombre únicos.");
			lista.add(new PestanaPersonalizada(id, nombre.trim()));
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
		Map<String, String> formatos = new LinkedHashMap<>();
		if (document.getFormatos() != null) {
			for (Formato formato : document.getFormatos()) {
				formatos.put(formato.getCelda(), formato.getEstilo());
			}
		}
		TicketWorkbookState state = new TicketWorkbookState(capturas, document.getVersion(), document.getActualizado());
		state.setFormatos(formatos);
		state.setEstructura(document.getEstructura());
		state.setPestanasPersonalizadas(document.getPestanasPersonalizadas() == null ? new ArrayList<>() : new ArrayList<>(document.getPestanasPersonalizadas()));
		return state;
	}

	static TicketWorkbookState empty() {
		return new TicketWorkbookState(new LinkedHashMap<>(), null, null);
	}
}
