package com.base580.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.base580.backend.boletos.Norte2026CdmxSeed;
import com.base580.backend.boletos.TicketMoney;
import com.base580.backend.boletos.TicketSettlementMath;
import com.base580.backend.boletos.TicketSettlementMath.Summary;
import com.base580.backend.model.TicketSettlementDocument;
import com.base580.backend.model.TicketSettlementDocument.Line;
import com.base580.backend.model.TicketSettlementRequest;
import com.base580.backend.model.TicketTariffDocument;
import com.base580.backend.repository.TicketSettlementRepository;

@Service
public class TicketSettlementService {

	private final TicketSettlementRepository repository;
	private final TicketTariffService tariffs;

	public TicketSettlementService(TicketSettlementRepository repository, TicketTariffService tariffs) {
		this.repository = repository;
		this.tariffs = tariffs;
	}

	public List<TicketSettlementDocument> list() {
		return repository.findAllByOrderBySavedAtDesc();
	}

	public TicketSettlementDocument get(String id) {
		return repository.findById(id).orElseThrow(() -> new TicketNotFoundException(id));
	}

	public TicketSettlementDocument create(TicketSettlementRequest request) {
		TicketSettlementDocument document = build(request);
		document.setSavedAt(OffsetDateTime.now().toString());
		return repository.save(document);
	}

	public TicketSettlementDocument update(String id, TicketSettlementRequest request) {
		if (!repository.existsById(id)) {
			throw new TicketNotFoundException(id);
		}
		TicketSettlementDocument document = build(request);
		document.setId(id);
		document.setSavedAt(OffsetDateTime.now().toString());
		return repository.save(document);
	}

	private TicketSettlementDocument build(TicketSettlementRequest request) {
		TicketSettlementDocument document = new TicketSettlementDocument();
		document.setBase(Norte2026CdmxSeed.BASE);
		document.setVigencia(Norte2026CdmxSeed.VIGENCIA);
		document.setAmbito(Norte2026CdmxSeed.AMBITO);
		document.setPlacas(text(request == null ? null : request.getPlacas()));
		document.setNombre(text(request == null ? null : request.getNombre()));
		document.setManualesEditados(request != null && request.isManualesEditados());

		List<Line> lines = new ArrayList<>();
		int left = 0;
		int right = 0;
		BigDecimal importe = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		List<TicketSettlementRequest.Line> incoming = request == null || request.getLineas() == null
			? List.of()
			: request.getLineas();

		for (TicketSettlementRequest.Line incomingLine : incoming) {
			if (incomingLine == null) {
				continue;
			}
			if (incomingLine.getCantidad() < 0) {
				throw new IllegalArgumentException("La cantidad no puede ser negativa.");
			}
			boolean manual = isManual(incomingLine);
			boolean edited = hasMoney(incomingLine);
			if (!manual && incomingLine.getCantidad() == 0 && !edited) {
				continue;
			}

			BigDecimal precio;
			BigDecimal descuento;
			BigDecimal sitio;
			BigDecimal plata;
			BigDecimal neto;
			String base;
			String vigencia;
			String ambito;
			String tipo;
			String zona;
			if (manual) {
				base = "MANUAL";
				vigencia = blankTo(incomingLine.getVigencia(), "2026");
				ambito = blankTo(incomingLine.getAmbito(), "CDMX");
				tipo = TicketTariffService.normalizeTipo(blankTo(incomingLine.getTipo(), "sencillo"));
				zona = incomingLine.getZona() == null ? "" : incomingLine.getZona().trim();
				precio = scale(incomingLine.getPrecio());
				descuento = scale(incomingLine.getDescuento());
				sitio = scale(incomingLine.getSitio());
				plata = null;
				neto = TicketMoney.neto(precio, descuento, sitio, null);
			} else if (edited || hasIdentity(incomingLine)) {
				String zonaCatalogo = zonaDeClave(incomingLine.getClave());
				TicketTariffDocument tariff = tariffs.require(
					incomingLine.getBase(),
					incomingLine.getVigencia(),
					incomingLine.getAmbito(),
					incomingLine.getTipo(),
					zonaCatalogo != null ? zonaCatalogo : incomingLine.getZona()
				);
				Money money = moneyOf(incomingLine, tariff);
				base = tariff.getBase();
				vigencia = tariff.getVigencia();
				ambito = tariff.getAmbito();
				tipo = tariff.getTipo();
				zona = tariff.getZona();
				precio = money.precio;
				descuento = money.descuento;
				sitio = money.sitio;
				plata = money.plata;
				neto = money.neto;
				String zonaEscrita = text(incomingLine.getZona());
				if (!zonaEscrita.isBlank()) {
					zona = zonaEscrita;
				}
			} else {
				TicketTariffDocument tariff = tariffs.require(incomingLine.getTipo(), incomingLine.getZona());
				base = tariff.getBase();
				vigencia = tariff.getVigencia();
				ambito = tariff.getAmbito();
				tipo = tariff.getTipo();
				zona = tariff.getZona();
				precio = tariff.getPrecio();
				descuento = tariff.getDescuento();
				sitio = tariff.getSitio();
				plata = tariff.getPlata();
				neto = tariff.getNeto();
			}

			if (incomingLine.getNeto() != null) {
				neto = scale(incomingLine.getNeto());
			}
			BigDecimal lineImporte = incomingLine.getImporte() != null
				? scale(incomingLine.getImporte())
				: TicketSettlementMath.importe(incomingLine.getCantidad(), neto);
			Line line = new Line();
			line.setBase(base);
			line.setVigencia(vigencia);
			line.setAmbito(ambito);
			line.setZona(zona);
			line.setTipo(tipo);
			line.setCantidad(incomingLine.getCantidad());
			line.setPrecio(precio);
			line.setDescuento(descuento);
			line.setSitio(sitio);
			line.setPlata(plata);
			line.setNeto(neto);
			line.setImporte(lineImporte);
			line.setManual(manual);
			line.setClave(text(incomingLine.getClave()));
			lines.add(line);
			importe = importe.add(lineImporte);
			if (incomingLine.getCantidad() > 0 && derecha(tipo)) {
				right += incomingLine.getCantidad();
			} else if (incomingLine.getCantidad() > 0) {
				left += incomingLine.getCantidad();
			}
		}

		Summary summary = TicketSettlementMath.summarize(importe, left, right);
		document.setLineas(lines);
		document.setServicios(summary.servicios());
		document.setConteo(summary.conteo());
		document.setImporte(summary.importe());
		document.setSinAhorro(summary.sinAhorro());
		document.setSencillo(summary.sencillo());
		document.setDoble(summary.doble());
		document.setTriple(summary.triple());
		document.setCuadruple(summary.cuadruple());
		document.setQuintuple(summary.quintuple());
		document.setSeptuple(summary.septuple());
		return document;
	}

	private static Money moneyOf(TicketSettlementRequest.Line line, TicketTariffDocument tariff) {
		boolean override = hasMoney(line);
		BigDecimal precio = override && line.getPrecio() != null ? line.getPrecio() : tariff.getPrecio();
		BigDecimal descuento = override && line.getDescuento() != null ? line.getDescuento() : tariff.getDescuento();
		BigDecimal sitio = override && line.getSitio() != null ? line.getSitio() : tariff.getSitio();
		BigDecimal plata = tariff.getPlata() == null
			? null
			: (override && line.getPlata() != null ? line.getPlata() : tariff.getPlata());
		return new Money(precio, descuento, sitio, plata, TicketMoney.neto(precio, descuento, sitio, plata));
	}

	private static boolean hasIdentity(TicketSettlementRequest.Line line) {
		return line.getBase() != null && !line.getBase().isBlank();
	}

	private static boolean hasMoney(TicketSettlementRequest.Line line) {
		return line.getPrecio() != null || line.getDescuento() != null || line.getSitio() != null || line.getPlata() != null;
	}

	private static boolean isManual(TicketSettlementRequest.Line line) {
		if (line.isManual()) {
			return true;
		}
		return line.getBase() != null && "MANUAL".equalsIgnoreCase(line.getBase().trim());
	}

	private static String zonaDeClave(String clave) {
		if (clave == null || clave.isBlank()) {
			return null;
		}
		String[] partes = clave.split("\\|", 5);
		if (partes.length != 5 || partes[4].isBlank()) {
			return null;
		}
		return partes[4];
	}

	private static boolean derecha(String tipo) {
		return "doble".equals(tipo) || "nocturno".equals(tipo);
	}

	private static BigDecimal scale(BigDecimal value) {
		return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : TicketMoney.scale(value);
	}

	private static String blankTo(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value.trim();
	}

	private static String text(String value) {
		return value == null ? "" : value.trim();
	}

	private record Money(BigDecimal precio, BigDecimal descuento, BigDecimal sitio, BigDecimal plata, BigDecimal neto) {
	}
}
