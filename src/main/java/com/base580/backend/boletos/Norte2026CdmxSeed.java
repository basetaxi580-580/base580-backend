package com.base580.backend.boletos;

import java.util.ArrayList;
import java.util.List;

import com.base580.backend.model.TicketTariffDocument;

/**
 * Sencillo y doble CDMX de la hoja BASE NTE 26, filas 3 a 15.
 * No incluye Edomex, nocturnos U–Z ni otras bases.
 */
public final class Norte2026CdmxSeed {

	public static final String BASE = "NORTE";
	public static final String VIGENCIA = "2026";
	public static final String AMBITO = "CDMX";

	private static final String SENCILLO = """
			1|78|10|6|3
			2|93|12.9|7|3
			3|125|15.5|8|3
			4|155|17.7|9|3
			5|182|18.9|10|3
			6|210|21|11|3
			7|240|24|12|3
			8|270|27|13|3
			9|297|29.7|14|3
			10|330|33|15|3
			11|370|37|16|3
			12|420|42|20|3
			SAL|70|22|10|3
			""";

	private static final String DOBLE = """
			1|94|12|7.2|3.35
			2|112|15.48|8.4|3.35
			3|150|18.6|9.6|3.35
			4|186|21.24|10.8|3.35
			5|218|22.68|12|3.35
			6|252|25.2|13.2|3.35
			7|288|28.8|14.4|3.35
			8|324|32.4|15.6|3.35
			9|356|35.64|16.8|3.35
			10|396|39.6|18|3.35
			11|444|44.4|19.2|3.35
			12|504|50.4|24|3.35
			SAL|84|26.4|12|3.35
			""";

	private Norte2026CdmxSeed() {
	}

	public static List<TicketTariffDocument> tariffs() {
		List<TicketTariffDocument> tariffs = new ArrayList<>();
		add(tariffs, "sencillo", SENCILLO);
		add(tariffs, "doble", DOBLE);
		return List.copyOf(tariffs);
	}

	public static TicketTariffDocument find(String tipo, String zona) {
		return tariffs().stream()
			.filter(tariff -> tipo.equals(tariff.getTipo()) && zona.equals(tariff.getZona()))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Zona " + zona + " " + tipo + " no está en la semilla."));
	}

	private static void add(List<TicketTariffDocument> tariffs, String tipo, String table) {
		for (String line : table.strip().split("\\R")) {
			if (line.isBlank()) {
				continue;
			}
			String[] parts = line.split("\\|");
			TicketTariffDocument document = new TicketTariffDocument();
			document.setBase(BASE);
			document.setVigencia(VIGENCIA);
			document.setAmbito(AMBITO);
			document.setTipo(tipo);
			document.setZona(parts[0].trim());
			document.setPrecio(TicketMoney.money(parts[1]));
			document.setDescuento(TicketMoney.money(parts[2]));
			document.setSitio(TicketMoney.money(parts[3]));
			document.setPlata(TicketMoney.money(parts[4]));
			document.setNeto(TicketMoney.neto(document.getPrecio(), document.getDescuento(), document.getSitio(), document.getPlata()));
			tariffs.add(document);
		}
	}
}
