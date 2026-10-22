package com.base580.backend.boletos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;

import com.base580.backend.model.TicketTariffDocument;

/**
 * Tarifas leídas de PAGO BOLETOS 2026.xlsx. El neto se vuelve a calcular;
 * la columna plata vacía significa que esa base no la resta.
 */
public final class TicketTariffCatalog {

	private static final List<TicketTariffDocument> CACHE = load();

	private TicketTariffCatalog() {
	}

	public static List<TicketTariffDocument> tariffs() {
		return CACHE.stream().map(TicketTariffCatalog::copy).toList();
	}

	public static TicketTariffDocument find(String base, String vigencia, String ambito, String tipo, String zona) {
		return tariffs().stream()
			.filter(tariff -> base.equals(tariff.getBase())
				&& vigencia.equals(tariff.getVigencia())
				&& ambito.equals(tariff.getAmbito())
				&& tipo.equals(tariff.getTipo())
				&& zona.equals(tariff.getZona()))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException(
				"No hay tarifa " + base + " " + vigencia + " " + ambito + " " + tipo + " zona " + zona + "."
			));
	}

	public static String key(TicketTariffDocument tariff) {
		return tariff.getBase() + "|" + tariff.getVigencia() + "|" + tariff.getAmbito() + "|" + tariff.getTipo() + "|" + tariff.getZona();
	}

	private static List<TicketTariffDocument> load() {
		List<TicketTariffDocument> tariffs = new ArrayList<>();
		ClassPathResource resource = new ClassPathResource("boletos/tarifas.tsv");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isBlank() || line.startsWith("#")) {
					continue;
				}
				String[] parts = line.split("\t", -1);
				if (parts.length < 12) {
					throw new IllegalStateException("Fila de tarifa incompleta: " + line);
				}
				TicketTariffDocument document = new TicketTariffDocument();
				document.setBase(parts[0]);
				document.setVigencia(parts[1]);
				document.setAmbito(parts[2]);
				document.setTipo(parts[3]);
				document.setZona(parts[4]);
				document.setPrecio(TicketMoney.money(parts[5]));
				document.setDescuento(TicketMoney.money(parts[6]));
				document.setSitio(TicketMoney.money(parts[7]));
				document.setPlata(parts[8].isBlank() ? null : TicketMoney.money(parts[8]));
				document.setNeto(TicketMoney.neto(document.getPrecio(), document.getDescuento(), document.getSitio(), document.getPlata()));
				String clave = parts[10].isBlank()
					? key(document)
					: parts[10];
				document.setClave(clave);
				document.setOrden(Integer.valueOf(parts[11]));
				tariffs.add(document);
			}
		} catch (IOException ex) {
			throw new UncheckedIOException("No se pudo leer la semilla de tarifas.", ex);
		}
		if (tariffs.isEmpty()) {
			throw new IllegalStateException("La semilla de tarifas está vacía.");
		}
		return List.copyOf(tariffs);
	}

	private static TicketTariffDocument copy(TicketTariffDocument source) {
		TicketTariffDocument copy = new TicketTariffDocument();
		copy.setBase(source.getBase());
		copy.setVigencia(source.getVigencia());
		copy.setAmbito(source.getAmbito());
		copy.setTipo(source.getTipo());
		copy.setZona(source.getZona());
		copy.setPrecio(source.getPrecio());
		copy.setDescuento(source.getDescuento());
		copy.setSitio(source.getSitio());
		copy.setPlata(source.getPlata());
		copy.setNeto(source.getNeto());
		copy.setClave(source.getClave());
		copy.setOrden(source.getOrden());
		return copy;
	}
}
