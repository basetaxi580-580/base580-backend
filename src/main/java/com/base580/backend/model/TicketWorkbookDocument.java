package com.base580.backend.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Capturas de la réplica de PAGO BOLETOS 2026.xlsx (/boletos).
 * El libro (hojas, fórmulas y estilos) vive en el frontend; aquí solo se guardan
 * las celdas que el usuario cambió, compartidas entre equipos.
 */
@Document("ticket_workbook")
public class TicketWorkbookDocument {

	public static final String DEFAULT_ID = "default";

	@Id
	private String id;

	@Version
	private Long version;

	private List<Captura> capturas = new ArrayList<>();
	private List<Formato> formatos = new ArrayList<>();
	private List<PestanaPersonalizada> pestanasPersonalizadas = new ArrayList<>();
	private List<String> ordenPestanas = new ArrayList<>();
	private String estructura;
	private String actualizado;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public List<Captura> getCapturas() {
		return capturas;
	}

	public void setCapturas(List<Captura> capturas) {
		this.capturas = capturas;
	}

	public List<Formato> getFormatos() {
		return formatos;
	}

	public void setFormatos(List<Formato> formatos) {
		this.formatos = formatos;
	}

	public List<PestanaPersonalizada> getPestanasPersonalizadas() {
		return pestanasPersonalizadas;
	}

	public void setPestanasPersonalizadas(List<PestanaPersonalizada> pestanasPersonalizadas) {
		this.pestanasPersonalizadas = pestanasPersonalizadas;
	}

	public List<String> getOrdenPestanas() { return ordenPestanas; }
	public void setOrdenPestanas(List<String> ordenPestanas) { this.ordenPestanas = ordenPestanas; }

	public String getEstructura() {
		return estructura;
	}

	public void setEstructura(String estructura) {
		this.estructura = estructura;
	}

	public String getActualizado() {
		return actualizado;
	}

	public void setActualizado(String actualizado) {
		this.actualizado = actualizado;
	}

	/** Una celda capturada: `celda` = "Hoja!A1" (el nombre de hoja puede tener espacios). */
	public static class Captura {
		private String celda;
		private String valor;

		public Captura() {
		}

		public Captura(String celda, String valor) {
			this.celda = celda;
			this.valor = valor;
		}

		public String getCelda() {
			return celda;
		}

		public void setCelda(String celda) {
			this.celda = celda;
		}

		public String getValor() {
			return valor;
		}

		public void setValor(String valor) {
			this.valor = valor;
		}
	}

	/** Formato aplicado por el usuario a una celda: `celda` = "Hoja!A1", `estilo` = JSON del estilo parcial. */
	public static class Formato {
		private String celda;
		private String estilo;

		public Formato() {
		}

		public Formato(String celda, String estilo) {
			this.celda = celda;
			this.estilo = estilo;
		}

		public String getCelda() {
			return celda;
		}

		public void setCelda(String celda) {
			this.celda = celda;
		}

		public String getEstilo() {
			return estilo;
		}

		public void setEstilo(String estilo) {
			this.estilo = estilo;
		}
	}

	/** Pestaña BASE creada por el usuario; el id no cambia al renombrarla. */
	public static class PestanaPersonalizada {
		private String id;
		private String nombre;

		public PestanaPersonalizada() {
		}

		public PestanaPersonalizada(String id, String nombre) {
			this.id = id;
			this.nombre = nombre;
		}

		public String getId() { return id; }
		public void setId(String id) { this.id = id; }
		public String getNombre() { return nombre; }
		public void setNombre(String nombre) { this.nombre = nombre; }
	}
}
