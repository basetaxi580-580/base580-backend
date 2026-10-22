package com.base580.backend.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.base580.backend.service.DuplicateTariffException;
import com.base580.backend.service.TicketNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(DuplicateTariffException.class)
	public ResponseEntity<Map<String, String>> duplicate(DuplicateTariffException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", exception.getMessage()));
	}

	@ExceptionHandler(TicketNotFoundException.class)
	public ResponseEntity<Map<String, String>> missing(TicketNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", exception.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> invalid(IllegalArgumentException exception) {
		String message = exception.getMessage() == null ? "Solicitud inválida." : exception.getMessage();
		return ResponseEntity.badRequest().body(Map.of("error", message));
	}
}
