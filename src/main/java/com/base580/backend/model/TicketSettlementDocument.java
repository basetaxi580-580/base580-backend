package com.base580.backend.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("ticket_settlements")
public class TicketSettlementDocument {

	@Id
	private String id;
	private String base;
	private String vigencia;
	private String ambito;
	private String placas = "";
	private String nombre = "";
	private List<Line> lineas = new ArrayList<>();
	private int servicios;
	private int conteo;
	private BigDecimal importe;
	private BigDecimal sinAhorro;
	private BigDecimal sencillo;
	private BigDecimal doble;
	private BigDecimal triple;
	private BigDecimal cuadruple;
	private BigDecimal quintuple;
	private BigDecimal septuple;
	private boolean manualesEditados;
	private String savedAt;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public String getVigencia() {
		return vigencia;
	}

	public void setVigencia(String vigencia) {
		this.vigencia = vigencia;
	}

	public String getAmbito() {
		return ambito;
	}

	public void setAmbito(String ambito) {
		this.ambito = ambito;
	}

	public String getPlacas() {
		return placas;
	}

	public void setPlacas(String placas) {
		this.placas = placas;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public List<Line> getLineas() {
		return lineas;
	}

	public void setLineas(List<Line> lineas) {
		this.lineas = lineas;
	}

	public int getServicios() {
		return servicios;
	}

	public void setServicios(int servicios) {
		this.servicios = servicios;
	}

	public int getConteo() {
		return conteo;
	}

	public void setConteo(int conteo) {
		this.conteo = conteo;
	}

	public BigDecimal getImporte() {
		return importe;
	}

	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}

	public BigDecimal getSinAhorro() {
		return sinAhorro;
	}

	public void setSinAhorro(BigDecimal sinAhorro) {
		this.sinAhorro = sinAhorro;
	}

	public BigDecimal getSencillo() {
		return sencillo;
	}

	public void setSencillo(BigDecimal sencillo) {
		this.sencillo = sencillo;
	}

	public BigDecimal getDoble() {
		return doble;
	}

	public void setDoble(BigDecimal doble) {
		this.doble = doble;
	}

	public BigDecimal getTriple() {
		return triple;
	}

	public void setTriple(BigDecimal triple) {
		this.triple = triple;
	}

	public BigDecimal getCuadruple() {
		return cuadruple;
	}

	public void setCuadruple(BigDecimal cuadruple) {
		this.cuadruple = cuadruple;
	}

	public BigDecimal getQuintuple() {
		return quintuple;
	}

	public void setQuintuple(BigDecimal quintuple) {
		this.quintuple = quintuple;
	}

	public BigDecimal getSeptuple() {
		return septuple;
	}

	public void setSeptuple(BigDecimal septuple) {
		this.septuple = septuple;
	}

	public boolean isManualesEditados() {
		return manualesEditados;
	}

	public void setManualesEditados(boolean manualesEditados) {
		this.manualesEditados = manualesEditados;
	}

	public String getSavedAt() {
		return savedAt;
	}

	public void setSavedAt(String savedAt) {
		this.savedAt = savedAt;
	}

	public static class Line {
		private String base;
		private String vigencia;
		private String ambito;
		private String zona;
		private String tipo;
		private int cantidad;
		private boolean manual;
		private String clave;
		private BigDecimal precio;
		private BigDecimal descuento;
		private BigDecimal sitio;
		private BigDecimal plata;
		private BigDecimal neto;
		private BigDecimal importe;

		public String getBase() {
			return base;
		}

		public void setBase(String base) {
			this.base = base;
		}

		public String getVigencia() {
			return vigencia;
		}

		public void setVigencia(String vigencia) {
			this.vigencia = vigencia;
		}

		public String getAmbito() {
			return ambito;
		}

		public void setAmbito(String ambito) {
			this.ambito = ambito;
		}

		public boolean isManual() {
			return manual;
		}

		public void setManual(boolean manual) {
			this.manual = manual;
		}

		public String getClave() {
			return clave;
		}

		public void setClave(String clave) {
			this.clave = clave;
		}

		public String getZona() {
			return zona;
		}

		public void setZona(String zona) {
			this.zona = zona;
		}

		public String getTipo() {
			return tipo;
		}

		public void setTipo(String tipo) {
			this.tipo = tipo;
		}

		public int getCantidad() {
			return cantidad;
		}

		public void setCantidad(int cantidad) {
			this.cantidad = cantidad;
		}

		public BigDecimal getPrecio() {
			return precio;
		}

		public void setPrecio(BigDecimal precio) {
			this.precio = precio;
		}

		public BigDecimal getDescuento() {
			return descuento;
		}

		public void setDescuento(BigDecimal descuento) {
			this.descuento = descuento;
		}

		public BigDecimal getSitio() {
			return sitio;
		}

		public void setSitio(BigDecimal sitio) {
			this.sitio = sitio;
		}

		public BigDecimal getPlata() {
			return plata;
		}

		public void setPlata(BigDecimal plata) {
			this.plata = plata;
		}

		public BigDecimal getNeto() {
			return neto;
		}

		public void setNeto(BigDecimal neto) {
			this.neto = neto;
		}

		public BigDecimal getImporte() {
			return importe;
		}

		public void setImporte(BigDecimal importe) {
			this.importe = importe;
		}
	}
}
