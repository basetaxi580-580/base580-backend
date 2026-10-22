package com.base580.backend.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.base580.backend.model.TicketSettlementDocument;
import com.base580.backend.model.TicketSettlementRequest;
import com.base580.backend.model.TicketTariffDocument;
import com.base580.backend.service.TicketSettlementService;
import com.base580.backend.service.TicketTariffService;

@RestController
@RequestMapping("/api/boletos")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
public class BoletosController {

	private final TicketTariffService tariffs;
	private final TicketSettlementService settlements;

	public BoletosController(TicketTariffService tariffs, TicketSettlementService settlements) {
		this.tariffs = tariffs;
		this.settlements = settlements;
	}

	@GetMapping("/tarifas")
	public List<TicketTariffDocument> tarifas(
		@RequestParam(required = false) String base,
		@RequestParam(required = false) String vigencia,
		@RequestParam(required = false) String ambito
	) {
		return tariffs.list(base, vigencia, ambito);
	}

	@PostMapping("/tarifas")
	@ResponseStatus(HttpStatus.CREATED)
	public TicketTariffDocument crearTarifa(@RequestBody TicketTariffDocument tarifa) {
		return tariffs.create(tarifa);
	}

	@GetMapping("/cortes")
	public List<TicketSettlementDocument> cortes() {
		return settlements.list();
	}

	@GetMapping("/cortes/{id}")
	public TicketSettlementDocument corte(@PathVariable String id) {
		return settlements.get(id);
	}

	@PostMapping("/cortes")
	@ResponseStatus(HttpStatus.CREATED)
	public TicketSettlementDocument crearCorte(@RequestBody TicketSettlementRequest request) {
		return settlements.create(request);
	}

	@PutMapping("/cortes/{id}")
	public TicketSettlementDocument actualizarCorte(@PathVariable String id, @RequestBody TicketSettlementRequest request) {
		return settlements.update(id, request);
	}
}
