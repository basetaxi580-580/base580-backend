package com.base580.backend.model;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("ticket_tariffs")
@CompoundIndex(name = "ticket_tariff_identity", def = "{'base': 1, 'vigencia': 1, 'ambito': 1, 'tipo': 1, 'zona': 1}", unique = true)
public class TicketTariffDocument {

	@Id
	private String id;
	private String base;
	private String vigencia;
	private String ambito;
	private String tipo;
	private String zona;
	private BigDecimal precio;
	private BigDecimal descuento;
	private BigDecimal sitio;
	private BigDecimal plata;
	private BigDecimal neto;
	private String clave;
	private Integer orden;

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

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getZona() {
		return zona;
	}

	public void setZona(String zona) {
		this.zona = zona;
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

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public Integer getOrden() {
		return orden;
	}

	public void setOrden(Integer orden) {
		this.orden = orden;
	}
}
