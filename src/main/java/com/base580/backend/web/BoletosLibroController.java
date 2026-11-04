package com.base580.backend.web;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.base580.backend.model.TicketWorkbookState;
import com.base580.backend.service.TicketWorkbookService;

/** Capturas compartidas de la réplica del Excel de boletos (/boletos). */
@RestController
@RequestMapping("/api/boletos/libro")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
public class BoletosLibroController {

	private final TicketWorkbookService workbook;

	public BoletosLibroController(TicketWorkbookService workbook) {
		this.workbook = workbook;
	}

	@GetMapping
	public TicketWorkbookState libro() {
		return workbook.get();
	}

	@PutMapping
	public TicketWorkbookState guardar(@RequestBody TicketWorkbookState request) {
		return workbook.save(request);
	}
}
