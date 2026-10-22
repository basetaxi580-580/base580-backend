package com.base580.backend.boletos;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Importe = cantidad × neto.
 * La columna izquierda cuenta una vez y la derecha otra vez más:
 * conteo = izquierda + derecha × 2.
 * Sin ahorro, sencillo, doble y triple restan 0.30, 5.30, 10.60 y 15.90 por boleto contado.
 * Cuádruple, quíntuple y séptuple salen del importe triple restando los servicios
 * (cada boleto una vez) por 5.30, 10.60 y 21.20. No hay séxtuple.
 */
public final class TicketSettlementMath {

	public static final BigDecimal SIN_AHORRO = new BigDecimal("0.30");
	public static final BigDecimal SENCILLO = new BigDecimal("5.30");
	public static final BigDecimal DOBLE = new BigDecimal("10.60");
	public static final BigDecimal TRIPLE = new BigDecimal("15.90");
	public static final BigDecimal CUADRUPLE = new BigDecimal("5.30");
	public static final BigDecimal QUINTUPLE = new BigDecimal("10.60");
	public static final BigDecimal SEPTUPLE = new BigDecimal("21.20");

	private TicketSettlementMath() {
	}

	public static BigDecimal importe(int cantidad, BigDecimal neto) {
		if (cantidad <= 0 || neto == null) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}
		return neto.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
	}

	public static Summary summarize(BigDecimal importe, int izquierda, int derecha) {
		int left = Math.max(izquierda, 0);
		int right = Math.max(derecha, 0);
		int servicios = left + right;
		int conteo = left + right * 2;
		BigDecimal total = (importe == null ? BigDecimal.ZERO : importe).setScale(2, RoundingMode.HALF_UP);
		BigDecimal triple = apply(total, conteo, TRIPLE);
		return new Summary(
			servicios,
			total,
			apply(total, conteo, SIN_AHORRO),
			apply(total, conteo, SENCILLO),
			apply(total, conteo, DOBLE),
			triple,
			conteo,
			desdeTriple(triple, servicios, CUADRUPLE),
			desdeTriple(triple, servicios, QUINTUPLE),
			desdeTriple(triple, servicios, SEPTUPLE)
		);
	}

	public static BigDecimal desdeTriple(BigDecimal triple, int servicios, BigDecimal rate) {
		BigDecimal base = (triple == null ? BigDecimal.ZERO : triple).setScale(2, RoundingMode.HALF_UP);
		if (servicios <= 0) {
			return base;
		}
		return base.subtract(rate.multiply(BigDecimal.valueOf(servicios))).setScale(2, RoundingMode.HALF_UP);
	}

	private static BigDecimal apply(BigDecimal importe, int conteo, BigDecimal rate) {
		if (conteo <= 0) {
			return importe;
		}
		return importe.subtract(rate.multiply(BigDecimal.valueOf(conteo))).setScale(2, RoundingMode.HALF_UP);
	}

	public record Summary(
		int servicios,
		BigDecimal importe,
		BigDecimal sinAhorro,
		BigDecimal sencillo,
		BigDecimal doble,
		BigDecimal triple,
		int conteo,
		BigDecimal cuadruple,
		BigDecimal quintuple,
		BigDecimal septuple
	) {
	}
}
