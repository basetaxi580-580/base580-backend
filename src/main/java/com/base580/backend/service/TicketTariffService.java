package com.base580.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Service;

import com.base580.backend.boletos.Norte2026CdmxSeed;
import com.base580.backend.boletos.TicketMoney;
import com.base580.backend.boletos.TicketTariffCatalog;
import com.base580.backend.model.TicketTariffDocument;
import com.base580.backend.repository.TicketTariffRepository;

@Service
public class TicketTariffService {

	private static final Logger log = LoggerFactory.getLogger(TicketTariffService.class);

	private final TicketTariffRepository repository;
	private final MongoTemplate mongoTemplate;
	private boolean indexReady;
	private boolean seeded;

	public TicketTariffService(TicketTariffRepository repository, MongoTemplate mongoTemplate) {
		this.repository = repository;
		this.mongoTemplate = mongoTemplate;
	}

	public synchronized List<TicketTariffDocument> list(String base, String vigencia, String ambito) {
		ensureSeed();
		List<TicketTariffDocument> tariffs = new ArrayList<>();
		for (TicketTariffDocument tariff : repository.findAll()) {
			if (base != null && !base.isBlank() && !base.equals(tariff.getBase())) {
				continue;
			}
			if (vigencia != null && !vigencia.isBlank() && !vigencia.equals(tariff.getVigencia())) {
				continue;
			}
			if (ambito != null && !ambito.isBlank() && !ambito.equals(tariff.getAmbito())) {
				continue;
			}
			tariffs.add(tariff);
		}
		tariffs.sort(Comparator
			.comparing((TicketTariffDocument tariff) -> tariff.getOrden() == null ? Integer.MAX_VALUE : tariff.getOrden())
			.thenComparingInt(tariff -> zoneRank(tariff.getZona()))
			.thenComparingInt(tariff -> tipoRank(tariff.getTipo())));
		return tariffs;
	}

	public synchronized TicketTariffDocument require(String tipo, String zona) {
		return require(Norte2026CdmxSeed.BASE, Norte2026CdmxSeed.VIGENCIA, Norte2026CdmxSeed.AMBITO, tipo, zona);
	}

	public synchronized TicketTariffDocument require(String base, String vigencia, String ambito, String tipo, String zona) {
		ensureSeed();
		String normalizedBase = required(base, "La base es obligatoria.");
		String normalizedVigencia = required(vigencia, "La vigencia es obligatoria.");
		String normalizedAmbito = required(ambito, "El ámbito es obligatorio.");
		String normalizedTipo = normalizeTipo(tipo);
		String normalizedZona = normalizeZona(zona);
		return repository.findByBaseAndVigenciaAndAmbitoAndTipoAndZona(
			normalizedBase,
			normalizedVigencia,
			normalizedAmbito,
			normalizedTipo,
			normalizedZona
		).orElseThrow(() -> new IllegalArgumentException(
			"No hay tarifa " + normalizedBase + " " + normalizedVigencia + " " + normalizedAmbito
				+ " para la zona " + normalizedZona + " y el tipo " + normalizedTipo + "."
		));
	}

	public synchronized TicketTariffDocument create(TicketTariffDocument input) {
		if (input == null) {
			throw new IllegalArgumentException("La tarifa es obligatoria.");
		}
		String base = required(input.getBase(), "La base es obligatoria.");
		String vigencia = required(input.getVigencia(), "La vigencia es obligatoria.");
		String ambito = required(input.getAmbito(), "El ámbito es obligatorio.");
		String tipo = normalizeTipo(input.getTipo());
		String zona = normalizeZona(input.getZona());
		if (repository.existsByBaseAndVigenciaAndAmbitoAndTipoAndZona(base, vigencia, ambito, tipo, zona)) {
			throw new DuplicateTariffException();
		}

		TicketTariffDocument document = new TicketTariffDocument();
		document.setBase(base);
		document.setVigencia(vigencia);
		document.setAmbito(ambito);
		document.setTipo(tipo);
		document.setZona(zona);
		document.setPrecio(input.getPrecio());
		document.setDescuento(input.getDescuento());
		document.setSitio(input.getSitio());
		document.setPlata(input.getPlata());
		document.setNeto(TicketMoney.neto(input.getPrecio(), input.getDescuento(), input.getSitio(), input.getPlata()));
		document.setClave(base + "|" + vigencia + "|" + ambito + "|" + tipo + "|" + zona);
		try {
			return repository.save(document);
		} catch (DuplicateKeyException ex) {
			throw new DuplicateTariffException();
		}
	}

	public synchronized void ensureSeed() {
		if (seeded) {
			return;
		}
		ensureIndex();
		Set<String> existing = new HashSet<>();
		for (TicketTariffDocument tariff : repository.findAll()) {
			existing.add(TicketTariffCatalog.key(tariff));
		}
		List<TicketTariffDocument> missing = new ArrayList<>();
		for (TicketTariffDocument tariff : TicketTariffCatalog.tariffs()) {
			if (!existing.contains(TicketTariffCatalog.key(tariff))) {
				missing.add(tariff);
			}
		}
		if (!missing.isEmpty()) {
			repository.saveAll(missing);
			log.info("Semilla de tarifas: {} nuevas en ticket_tariffs.", missing.size());
		}
		seeded = true;
	}

	private void ensureIndex() {
		if (indexReady || mongoTemplate == null) {
			return;
		}
		mongoTemplate.indexOps(TicketTariffDocument.class).createIndex(new Index()
			.on("base", Sort.Direction.ASC)
			.on("vigencia", Sort.Direction.ASC)
			.on("ambito", Sort.Direction.ASC)
			.on("tipo", Sort.Direction.ASC)
			.on("zona", Sort.Direction.ASC)
			.unique()
			.named("ticket_tariff_identity"));
		indexReady = true;
	}

	static String normalizeTipo(String tipo) {
		String value = required(tipo, "El tipo es obligatorio.").toLowerCase();
		if (!"sencillo".equals(value) && !"doble".equals(value) && !"nocturno".equals(value)) {
			throw new IllegalArgumentException("El tipo debe ser sencillo, doble o nocturno.");
		}
		return value;
	}

	static String normalizeZona(String zona) {
		String value = required(zona, "La zona es obligatoria.");
		return "sal".equalsIgnoreCase(value) ? "SAL" : value;
	}

	private static String required(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(message);
		}
		return value.trim();
	}

	private static int zoneRank(String zona) {
		if (zona == null) {
			return 1000;
		}
		String bare = zona.replaceAll(" \\(\\d+\\)$", "");
		if ("SAL".equalsIgnoreCase(bare) || "SALIDA".equalsIgnoreCase(bare)) {
			return 13;
		}
		try {
			return Integer.parseInt(bare);
		} catch (NumberFormatException ex) {
			return 1000;
		}
	}

	private static int tipoRank(String tipo) {
		if ("sencillo".equals(tipo)) {
			return 0;
		}
		if ("doble".equals(tipo)) {
			return 1;
		}
		return 2;
	}
}
