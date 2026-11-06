package com.base580.backend.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** Estado del libro que viaja por el API: capturas por celda ("Hoja!A1" -> texto) y versión. */
public class TicketWorkbookState {

	private Map<String, String> capturas = new LinkedHashMap<>();
	private Map<String, String> formatos = new LinkedHashMap<>();
	private String estructura;
	private Long version;
	private String actualizado;

	public TicketWorkbookState() {
	}

	public TicketWorkbookState(Map<String, String> capturas, Long version, String actualizado) {
		this.capturas = capturas;
		this.version = version;
		this.actualizado = actualizado;
	}

	public Map<String, String> getCapturas() {
		return capturas;
	}

	public void setCapturas(Map<String, String> capturas) {
		this.capturas = capturas;
	}

	public Map<String, String> getFormatos() {
		return formatos;
	}

	public void setFormatos(Map<String, String> formatos) {
		this.formatos = formatos;
	}

	public String getEstructura() {
		return estructura;
	}

	public void setEstructura(String estructura) {
		this.estructura = estructura;
	}

	/** Versión del documento; null cuando aún no se ha guardado nada. */
	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getActualizado() {
		return actualizado;
	}

	public void setActualizado(String actualizado) {
		this.actualizado = actualizado;
	}
}
