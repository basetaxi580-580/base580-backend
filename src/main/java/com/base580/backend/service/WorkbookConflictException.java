package com.base580.backend.service;

import com.base580.backend.model.TicketWorkbookState;

/** Otro equipo guardó el libro antes; lleva el estado actual para que el cliente lo fusione. */
public class WorkbookConflictException extends RuntimeException {

	private final transient TicketWorkbookState actual;

	public WorkbookConflictException(TicketWorkbookState actual) {
		super("El libro cambió en otro equipo.");
		this.actual = actual;
	}

	public TicketWorkbookState getActual() {
		return actual;
	}
}
