package com.base580.backend.service;

public class TicketNotFoundException extends RuntimeException {

	public TicketNotFoundException(String id) {
		super("No se encontró el corte " + id + ".");
	}
}
