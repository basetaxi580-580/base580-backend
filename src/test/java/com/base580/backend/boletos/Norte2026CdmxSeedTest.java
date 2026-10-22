package com.base580.backend.boletos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.base580.backend.model.TicketTariffDocument;

class Norte2026CdmxSeedTest {

	@Test
	void zona1SencilloQuedaEn59() {
		TicketTariffDocument tariff = Norte2026CdmxSeed.find("sencillo", "1");

		assertEquals("NORTE", tariff.getBase());
		assertEquals("2026", tariff.getVigencia());
		assertEquals("CDMX", tariff.getAmbito());
		eq("78.00", tariff.getPrecio());
		eq("10.00", tariff.getDescuento());
		eq("6.00", tariff.getSitio());
		eq("3.00", tariff.getPlata());
		eq("59.00", tariff.getNeto());
	}

	@Test
	void zona12YSalSencillo() {
		eq("355.00", Norte2026CdmxSeed.find("sencillo", "12").getNeto());
		eq("35.00", Norte2026CdmxSeed.find("sencillo", "SAL").getNeto());
	}

	@Test
	void semillaSoloTraeSencilloYDobleCdmx() {
		var tariffs = Norte2026CdmxSeed.tariffs();
		Set<String> zones = tariffs.stream().map(TicketTariffDocument::getZona).collect(Collectors.toSet());

		assertEquals(26, tariffs.size());
		assertEquals(Set.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "SAL"), zones);
		assertTrue(tariffs.stream().allMatch(tariff ->
			"NORTE".equals(tariff.getBase())
				&& "2026".equals(tariff.getVigencia())
				&& "CDMX".equals(tariff.getAmbito())
				&& ("sencillo".equals(tariff.getTipo()) || "doble".equals(tariff.getTipo()))
		));
		eq("2840.00", sum(tariffs, "sencillo"));
		eq("3408.00", sum(tariffs, "doble"));
	}

	@Test
	void cadaFilaCumpleLaFormulaDelNeto() {
		for (TicketTariffDocument tariff : Norte2026CdmxSeed.tariffs()) {
			assertEquals(
				TicketMoney.neto(tariff.getPrecio(), tariff.getDescuento(), tariff.getSitio(), tariff.getPlata()),
				tariff.getNeto()
			);
		}
	}

	@Test
	void tarifaSinPlataRestaSoloDescuentoYSitio() {
		eq("85.00", TicketMoney.neto(new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("5.00"), null));
	}

	private static BigDecimal sum(java.util.List<TicketTariffDocument> tariffs, String tipo) {
		return tariffs.stream()
			.filter(tariff -> tipo.equals(tariff.getTipo()))
			.map(TicketTariffDocument::getPrecio)
			.reduce(BigDecimal.ZERO, BigDecimal::add)
			.setScale(2);
	}

	private static void eq(String expected, BigDecimal actual) {
		assertEquals(new BigDecimal(expected), actual);
	}
}
