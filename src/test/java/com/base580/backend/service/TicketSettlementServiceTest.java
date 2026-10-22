package com.base580.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.base580.backend.boletos.Norte2026CdmxSeed;
import com.base580.backend.boletos.TicketTariffCatalog;
import com.base580.backend.model.TicketSettlementDocument;
import com.base580.backend.model.TicketSettlementRequest;
import com.base580.backend.model.TicketTariffDocument;
import com.base580.backend.repository.TicketSettlementRepository;

@ExtendWith(MockitoExtension.class)
class TicketSettlementServiceTest {

	@Mock
	private TicketSettlementRepository repository;

	@Mock
	private TicketTariffService tariffs;

	private TicketSettlementService service;
	private TicketSettlementDocument stored;

	@BeforeEach
	void setUp() {
		service = new TicketSettlementService(repository, tariffs);
	}

	private void rememberSaves() {
		when(repository.save(any())).thenAnswer(invocation -> {
			stored = invocation.getArgument(0);
			if (stored.getId() == null) {
				stored.setId("corte-1");
			}
			return stored;
		});
	}

	@Test
	void diezSencillosZona1SeGuardanEn590_587_537_484_431() {
		rememberSaves();
		when(tariffs.require("sencillo", "1")).thenReturn(Norte2026CdmxSeed.find("sencillo", "1"));

		TicketSettlementRequest request = request("A8119L", "MARIO GONZALEZ",
			new TicketSettlementRequest.Line("1", "sencillo", 10),
			new TicketSettlementRequest.Line("12", "sencillo", 0));

		TicketSettlementDocument saved = service.create(request);

		assertEquals("A8119L", saved.getPlacas());
		assertEquals("MARIO GONZALEZ", saved.getNombre());
		assertEquals("NORTE", saved.getBase());
		assertEquals(1, saved.getLineas().size());
		assertEquals(10, saved.getLineas().get(0).getCantidad());
		eq("59.00", saved.getLineas().get(0).getNeto());
		eq("590.00", saved.getImporte());
		eq("587.00", saved.getSinAhorro());
		eq("537.00", saved.getSencillo());
		eq("484.00", saved.getDoble());
		eq("431.00", saved.getTriple());
		assertEquals(10, saved.getServicios());
		assertEquals(10, saved.getConteo());

		when(repository.findById("corte-1")).thenReturn(Optional.of(stored));
		TicketSettlementDocument reopened = service.get("corte-1");
		eq("590.00", reopened.getImporte());
		eq("587.00", reopened.getSinAhorro());
		eq("537.00", reopened.getSencillo());
		assertEquals(10, reopened.getLineas().get(0).getCantidad());
	}

	@Test
	void diezEnColumnaDerechaConElMismoNetoDanSinAhorro584() {
		rememberSaves();
		TicketTariffDocument sameNetOnTheRight = Norte2026CdmxSeed.find("sencillo", "1");
		sameNetOnTheRight.setTipo("doble");
		when(tariffs.require("doble", "1")).thenReturn(sameNetOnTheRight);

		TicketSettlementDocument saved = service.create(request("B123", "NORTE",
			new TicketSettlementRequest.Line("1", "doble", 10)));

		eq("590.00", saved.getImporte());
		eq("584.00", saved.getSinAhorro());
		eq("484.00", saved.getSencillo());
		eq("378.00", saved.getDoble());
		eq("272.00", saved.getTriple());
		assertEquals(20, saved.getConteo());
	}

	@Test
	void variasZonasCdmxSeReabrenConLasMismasCantidades() {
		rememberSaves();
		when(tariffs.require("sencillo", "1")).thenReturn(Norte2026CdmxSeed.find("sencillo", "1"));
		when(tariffs.require("doble", "12")).thenReturn(Norte2026CdmxSeed.find("doble", "12"));

		TicketSettlementDocument saved = service.create(request("C456", "CORTE",
			new TicketSettlementRequest.Line("1", "sencillo", 2),
			new TicketSettlementRequest.Line("12", "doble", 1)));

		assertEquals(2, saved.getLineas().size());
		eq("544.25", saved.getImporte());
		assertEquals(2, saved.getLineas().get(0).getCantidad());
		assertEquals(1, saved.getLineas().get(1).getCantidad());
		assertEquals("12", saved.getLineas().get(1).getZona());

		when(repository.findById("corte-1")).thenReturn(Optional.of(stored));
		TicketSettlementDocument reopened = service.get("corte-1");
		assertEquals(2, reopened.getLineas().get(0).getCantidad());
		assertEquals(1, reopened.getLineas().get(1).getCantidad());
		eq("544.25", reopened.getImporte());
	}

	@Test
	void unPrecioEditadoCambiaElImporte() {
		rememberSaves();
		when(tariffs.require("NORTE", "2026", "CDMX", "sencillo", "1")).thenReturn(Norte2026CdmxSeed.find("sencillo", "1"));

		TicketSettlementRequest.Line line = new TicketSettlementRequest.Line("1", "sencillo", 10);
		line.setBase("NORTE");
		line.setVigencia("2026");
		line.setAmbito("CDMX");
		line.setPrecio(new BigDecimal("88"));
		line.setDescuento(new BigDecimal("10"));
		line.setSitio(new BigDecimal("6"));
		line.setPlata(new BigDecimal("3"));

		TicketSettlementDocument saved = service.create(request("A1", "EDICION", line));

		eq("69.00", saved.getLineas().get(0).getNeto());
		eq("690.00", saved.getImporte());
		eq("687.00", saved.getSinAhorro());
		eq("637.00", saved.getSencillo());
		eq("584.00", saved.getDoble());
		eq("531.00", saved.getTriple());
		assertEquals(10, saved.getServicios());
	}

	@Test
	void cadaBaseNuevaConservaElNetoDelExcel() {
		rememberSaves();
		assertNeto("NORTE", "2025", "CDMX", "sencillo", "1", "41.94");
		assertNeto("NORTE", "2026", "EDOMEX", "sencillo", "A", "102.04");
		assertNeto("ESPECIALES", "2026", "CDMX", "sencillo", "AA", "1381.50");
		assertNeto("SUR", "2026", "CDMX", "sencillo", "1", "59.00");
		assertNeto("SUR", "2025", "CDMX", "sencillo", "1", "41.94");
		assertNeto("TAPO", "2025", "CDMX", "sencillo", "1", "45.90");
		assertNeto("PTE", "VIGENTE", "CDMX", "sencillo", "1", "61.50");
	}

	@Test
	void manualRestaCentralYOrgaAntesDeMultiplicar() {
		rememberSaves();
		TicketSettlementRequest.Line line = new TicketSettlementRequest.Line("1", "sencillo", 2);
		line.setBase("MANUAL");
		line.setManual(true);
		line.setPrecio(new BigDecimal("83"));
		line.setDescuento(new BigDecimal("16"));
		line.setSitio(new BigDecimal("5.50"));

		TicketSettlementDocument saved = service.create(request("", "", line));

		eq("61.50", saved.getLineas().get(0).getNeto());
		eq("123.00", saved.getImporte());
		assertEquals(null, saved.getLineas().get(0).getPlata());
	}

	@Test
	void nocturnoCuentaDosVeces() {
		rememberSaves();
		TicketTariffDocument nocturno = TicketTariffCatalog.find("NORTE", "2026", "EDOMEX", "nocturno", "U");
		when(tariffs.require("NORTE", "2026", "EDOMEX", "nocturno", "U")).thenReturn(nocturno);
		TicketSettlementRequest.Line line = new TicketSettlementRequest.Line("U", "nocturno", 1);
		line.setBase("NORTE");
		line.setVigencia("2026");
		line.setAmbito("EDOMEX");

		TicketSettlementDocument saved = service.create(request("", "", line));

		eq("2.36", saved.getLineas().get(0).getPlata());
		assertEquals(1, saved.getServicios());
		assertEquals(2, saved.getConteo());
	}

	@Test
	void guardaZonaNetoEImporteEditados() {
		rememberSaves();
		when(tariffs.require("NORTE", "2026", "CDMX", "sencillo", "1")).thenReturn(Norte2026CdmxSeed.find("sencillo", "1"));
		TicketSettlementRequest.Line line = new TicketSettlementRequest.Line("Centro", "sencillo", 2);
		line.setBase("NORTE");
		line.setVigencia("2026");
		line.setAmbito("CDMX");
		line.setClave("NORTE|2026|CDMX|sencillo|1");
		line.setPrecio(new BigDecimal("78"));
		line.setDescuento(new BigDecimal("10"));
		line.setSitio(new BigDecimal("6"));
		line.setPlata(new BigDecimal("3"));
		line.setNeto(new BigDecimal("50"));
		line.setImporte(new BigDecimal("80"));

		TicketSettlementDocument saved = service.create(request("", "", line));

		assertEquals("Centro", saved.getLineas().get(0).getZona());
		eq("50.00", saved.getLineas().get(0).getNeto());
		eq("80.00", saved.getLineas().get(0).getImporte());
		eq("80.00", saved.getImporte());
	}

	@Test
	void rechazaUnaZonaQueNoEsCdmx() {
		when(tariffs.require("sencillo", "A")).thenThrow(new IllegalArgumentException("No hay tarifa Norte 2026 CDMX para la zona A y el tipo sencillo."));

		assertThrows(IllegalArgumentException.class, () -> service.create(request("", "",
			new TicketSettlementRequest.Line("A", "sencillo", 1))));
	}

	private void assertNeto(String base, String vigencia, String ambito, String tipo, String zona, String expected) {
		TicketTariffDocument tariff = TicketTariffCatalog.find(base, vigencia, ambito, tipo, zona);
		when(tariffs.require(base, vigencia, ambito, tipo, zona)).thenReturn(tariff);
		TicketSettlementRequest.Line line = new TicketSettlementRequest.Line(zona, tipo, 1);
		line.setBase(base);
		line.setVigencia(vigencia);
		line.setAmbito(ambito);
		TicketSettlementDocument saved = service.create(request("", "", line));
		eq(expected, saved.getLineas().get(0).getNeto());
		eq(expected, saved.getImporte());
	}

	private static TicketSettlementRequest request(String placas, String nombre, TicketSettlementRequest.Line... lines) {
		TicketSettlementRequest request = new TicketSettlementRequest();
		request.setPlacas(placas);
		request.setNombre(nombre);
		request.setLineas(java.util.List.of(lines));
		return request;
	}

	private static void eq(String expected, BigDecimal actual) {
		assertEquals(new BigDecimal(expected), actual);
	}
}
