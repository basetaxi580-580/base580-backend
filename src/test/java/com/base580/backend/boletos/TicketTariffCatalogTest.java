package com.base580.backend.boletos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.base580.backend.model.TicketTariffDocument;

class TicketTariffCatalogTest {

	@Test
	void norte2026CdmxSigueEn59_355Y35() {
		TicketTariffDocument zona1 = TicketTariffCatalog.find("NORTE", "2026", "CDMX", "sencillo", "1");
		eq("78.00", zona1.getPrecio());
		eq("10.00", zona1.getDescuento());
		eq("6.00", zona1.getSitio());
		eq("3.00", zona1.getPlata());
		eq("59.00", zona1.getNeto());
		eq("355.00", TicketTariffCatalog.find("NORTE", "2026", "CDMX", "sencillo", "12").getNeto());
		eq("35.00", TicketTariffCatalog.find("NORTE", "2026", "CDMX", "sencillo", "SAL").getNeto());
		assertEquals(26, TicketTariffCatalog.tariffs().stream()
			.filter(tariff -> "NORTE".equals(tariff.getBase())
				&& "2026".equals(tariff.getVigencia())
				&& "CDMX".equals(tariff.getAmbito()))
			.count());
	}

	@Test
	void cadaBaseNuevaTraeElNetoDelExcel() {
		eq("41.94", TicketTariffCatalog.find("NORTE", "2025", "CDMX", "sencillo", "1").getNeto());
		eq("102.04", TicketTariffCatalog.find("NORTE", "2026", "EDOMEX", "sencillo", "A").getNeto());
		TicketTariffDocument aa = TicketTariffCatalog.find("ESPECIALES", "2026", "CDMX", "sencillo", "AA");
		eq("1381.50", aa.getNeto());
		assertNull(aa.getPlata());
		TicketTariffDocument ppp = TicketTariffCatalog.find("ESPECIALES", "2026", "CDMX", "sencillo", "PPP");
		eq("378.48", ppp.getDescuento());
		eq("4511.14", ppp.getNeto());
		eq("59.00", TicketTariffCatalog.find("SUR", "2026", "CDMX", "sencillo", "1").getNeto());
		eq("41.94", TicketTariffCatalog.find("SUR", "2025", "CDMX", "sencillo", "1").getNeto());
		TicketTariffDocument tapo = TicketTariffCatalog.find("TAPO", "2025", "CDMX", "sencillo", "1");
		eq("45.90", tapo.getNeto());
		assertNull(tapo.getPlata());
		TicketTariffDocument poniente = TicketTariffCatalog.find("PTE", "VIGENTE", "CDMX", "sencillo", "1");
		eq("61.50", poniente.getNeto());
		assertNull(poniente.getPlata());
		TicketTariffDocument manual = TicketTariffCatalog.find("MANUAL", "2026", "CDMX", "sencillo", "1");
		eq("61.50", manual.getNeto());
		assertNull(manual.getPlata());
	}

	@Test
	void nocturnoUzDeNorte2026ConservaPlata236() {
		TicketTariffDocument nocturno = TicketTariffCatalog.find("NORTE", "2026", "EDOMEX", "nocturno", "U");
		eq("1038.00", nocturno.getPrecio());
		eq("2.36", nocturno.getPlata());
		eq("901.18", nocturno.getNeto());
	}

	@Test
	void cadaFilaCumpleLaFormulaDelNeto() {
		assertEquals(861, TicketTariffCatalog.tariffs().size());
		for (TicketTariffDocument tariff : TicketTariffCatalog.tariffs()) {
			assertEquals(
				TicketMoney.neto(tariff.getPrecio(), tariff.getDescuento(), tariff.getSitio(), tariff.getPlata()),
				tariff.getNeto()
			);
		}
	}

	private static void eq(String expected, BigDecimal actual) {
		assertEquals(new BigDecimal(expected), actual);
	}
}
