package com.base580.backend.boletos;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class TicketMoney {

	private static final int SCALE = 2;

	private TicketMoney() {
	}

	public static BigDecimal money(String value) {
		return new BigDecimal(value.trim()).setScale(SCALE, RoundingMode.HALF_UP);
	}

	public static BigDecimal scale(BigDecimal value) {
		return value.setScale(SCALE, RoundingMode.HALF_UP);
	}

	/**
	 * Norte y Sur restan plata. Si plata es nula, el neto es precio − descuento − sitio.
	 */
	public static BigDecimal neto(BigDecimal precio, BigDecimal descuento, BigDecimal sitio, BigDecimal plata) {
		if (precio == null) {
			throw new IllegalArgumentException("El precio es obligatorio.");
		}
		BigDecimal result = scale(precio).subtract(zero(descuento)).subtract(zero(sitio));
		if (plata != null) {
			result = result.subtract(scale(plata));
		}
		return result.setScale(SCALE, RoundingMode.HALF_UP);
	}

	private static BigDecimal zero(BigDecimal value) {
		return value == null ? BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP) : scale(value);
	}
}
