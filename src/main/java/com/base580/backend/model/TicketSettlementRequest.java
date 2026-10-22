package com.base580.backend.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TicketSettlementRequest {

	private String placas;
	private String nombre;
	private boolean manualesEditados;
	private List<Line> lineas = new ArrayList<>();

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

	public boolean isManualesEditados() {
		return manualesEditados;
	}

	public void setManualesEditados(boolean manualesEditados) {
		this.manualesEditados = manualesEditados;
	}

	public List<Line> getLineas() {
		return lineas;
	}

	public void setLineas(List<Line> lineas) {
		this.lineas = lineas;
	}

	public static class Line {
		private String base;
		private String vigencia;
		private String ambito;
		private String zona;
		private String tipo;
		private int cantidad;
		private BigDecimal precio;
		private BigDecimal descuento;
		private BigDecimal sitio;
		private BigDecimal plata;
		private BigDecimal neto;
		private BigDecimal importe;
		private boolean manual;
		private String clave;

		public Line() {
		}

		public Line(String zona, String tipo, int cantidad) {
			this.zona = zona;
			this.tipo = tipo;
			this.cantidad = cantidad;
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
	}
}
