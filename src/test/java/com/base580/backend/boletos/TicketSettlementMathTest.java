package com.base580.backend.boletos;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.base580.backend.boletos.TicketSettlementMath.Summary;

class TicketSettlementMathTest {

	@Test
	void diezSencillosColumnaIzquierdaDan590_587_537_doble484_triple431() {
		Summary summary = TicketSettlementMath.summarize(new BigDecimal("590.00"), 10, 0);

		assertEquals(10, summary.servicios());
		assertEquals(10, summary.conteo());
		eq("590.00", summary.importe());
		eq("587.00", summary.sinAhorro());
		eq("537.00", summary.sencillo());
		eq("484.00", summary.doble());
		eq("431.00", summary.triple());
		eq("378.00", summary.cuadruple());
		eq("325.00", summary.quintuple());
		eq("219.00", summary.septuple());
	}

	@Test
	void diezEnColumnaDerechaMantienen590YSinAhorro584() {
		Summary summary = TicketSettlementMath.summarize(new BigDecimal("590.00"), 0, 10);

		assertEquals(10, summary.servicios());
		assertEquals(20, summary.conteo());
		eq("590.00", summary.importe());
		eq("584.00", summary.sinAhorro());
		eq("484.00", summary.sencillo());
		eq("378.00", summary.doble());
		eq("272.00", summary.triple());
	}

	@Test
	void cantidadCeroNoRestaAhorro() {
		Summary summary = TicketSettlementMath.summarize(new BigDecimal("590.00"), 0, 0);

		eq("590.00", summary.sinAhorro());
		eq("590.00", summary.sencillo());
		eq("590.00", summary.doble());
		eq("590.00", summary.triple());
	}

	@Test
	void importeEsCantidadPorNeto() {
		eq("590.00", TicketSettlementMath.importe(10, new BigDecimal("59.00")));
		eq("0.00", TicketSettlementMath.importe(0, new BigDecimal("59.00")));
	}

	private static void eq(String expected, BigDecimal actual) {
		assertEquals(new BigDecimal(expected), actual);
	}
}
