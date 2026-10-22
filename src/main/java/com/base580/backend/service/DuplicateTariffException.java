package com.base580.backend.service;

public class DuplicateTariffException extends RuntimeException {

	public DuplicateTariffException() {
		super("Ya existe una tarifa con la misma base, vigencia, ámbito, tipo y zona.");
	}
}
