package com.base580.backend.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("app_state")
public class AppStateDocument {

	@Id
	private String id = "default";
	private List<MemberProfile> members = new ArrayList<>();
	private Catalogs catalogs = new Catalogs();
	private Map<String, List<AuditEntry>> auditTrail = new LinkedHashMap<>();
	private String updatedAt;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<MemberProfile> getMembers() {
		return members;
	}

	public void setMembers(List<MemberProfile> members) {
		this.members = members;
	}

	public Catalogs getCatalogs() {
		return catalogs;
	}

	public void setCatalogs(Catalogs catalogs) {
		this.catalogs = catalogs;
	}

	public Map<String, List<AuditEntry>> getAuditTrail() {
		return auditTrail;
	}

	public void setAuditTrail(Map<String, List<AuditEntry>> auditTrail) {
		this.auditTrail = auditTrail;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public static AppStateDocument sample() {
		AppStateDocument state = new AppStateDocument();

		MemberProfile member = new MemberProfile();
		member.setId("398");
		member.setName("MARIO GONZALEZ RAMIREZ");
		member.setMemberType("Empleado 1155");
		member.setEmployee("1155");
		member.setSite("192");
		member.setPlate("A8119L");
		member.setSticker("1018");
		member.setExpiry("28/10/2025");
		member.setStatus("OK");
		member.setDayTickets(0);
		member.setNightTickets(0);
		member.setTotalTickets(0);
		member.setNightSaving("N/A");
		member.setPersonalSaving("N/A");
		member.setCardPayment("N/A");
		member.setNotes("Perfil operativo base para demostración del CRM moderno.");
		member.getHistory().add(new MemberHistoryEntry("Ficha cargada", "El sistema abrió la información principal del socio desde la búsqueda global.", "Hoy"));
		member.getHistory().add(new MemberHistoryEntry("Vigencia verificada", "Se confirmó la fecha de vencimiento y la asignación actual de engomado.", "Reciente"));
		member.getHistory().add(new MemberHistoryEntry("Sin movimientos pendientes", "Los boletos y balances están en cero para este perfil.", "Histórico"));
		state.getMembers().add(member);

		state.getCatalogs().getEngomados().add(new CatalogItem("1018", "Engomado 1018", true));
		state.getCatalogs().getEngomados().add(new CatalogItem("1042", "Engomado 1042", true));
		state.getCatalogs().getEngomados().add(new CatalogItem("1099", "Engomado 1099", false));

		state.getCatalogs().getPriceList().add(new PriceItem("P-8", "Primera / Zona 8", "8", "P", 173, true));
		state.getCatalogs().getPriceList().add(new PriceItem("T-8", "Terminal / Zona 8", "8", "T", 145, true));
		state.getCatalogs().getPriceList().add(new PriceItem("P-9", "Primera / Zona 9", "9", "P", 180, false));

		state.getCatalogs().getStatuses().add(new CatalogItem("OK", "Operativo", true));
		state.getCatalogs().getStatuses().add(new CatalogItem("PENDIENTE", "Pendiente", true));
		state.getCatalogs().getStatuses().add(new CatalogItem("INACTIVO", "Inactivo", true));
		state.getCatalogs().getStatuses().add(new CatalogItem("VENCIDO", "Vencido", true));

		state.getCatalogs().getCancelReasons().add(new CatalogItem("CAPTURA", "Error de captura", true));
		state.getCatalogs().getCancelReasons().add(new CatalogItem("DUPLICADA", "Operación duplicada", true));
		state.getCatalogs().getCancelReasons().add(new CatalogItem("USUARIO", "Solicitud del usuario", true));

		state.getCatalogs().getMemberTypes().add(new CatalogItem("EMP", "Empleado", true));
		state.getCatalogs().getMemberTypes().add(new CatalogItem("SOC", "Socio", true));
		state.getCatalogs().getMemberTypes().add(new CatalogItem("VIS", "Visitante", false));

		state.getAuditTrail().put("398", new ArrayList<>(List.of(
			new AuditEntry("socio", "Ficha cargada", "Se abrió la información principal del socio desde la búsqueda global.", "Hoy"),
			new AuditEntry("socio", "Vigencia verificada", "Se confirmó la fecha de vencimiento y la asignación actual de engomado.", "Reciente"),
			new AuditEntry("socio", "Sin movimientos pendientes", "Los boletos y balances están en cero para este perfil.", "Histórico")
		)));
		state.setUpdatedAt(OffsetDateTime.now().toString());
		return state;
	}

	public static class Catalogs {
		private List<CatalogItem> engomados = new ArrayList<>();
		private List<PriceItem> priceList = new ArrayList<>();
		private List<CatalogItem> statuses = new ArrayList<>();
		private List<CatalogItem> cancelReasons = new ArrayList<>();
		private List<CatalogItem> memberTypes = new ArrayList<>();

		public List<CatalogItem> getEngomados() {
			return engomados;
		}

		public void setEngomados(List<CatalogItem> engomados) {
			this.engomados = engomados;
		}

		public List<PriceItem> getPriceList() {
			return priceList;
		}

		public void setPriceList(List<PriceItem> priceList) {
			this.priceList = priceList;
		}

		public List<CatalogItem> getStatuses() {
			return statuses;
		}

		public void setStatuses(List<CatalogItem> statuses) {
			this.statuses = statuses;
		}

		public List<CatalogItem> getCancelReasons() {
			return cancelReasons;
		}

		public void setCancelReasons(List<CatalogItem> cancelReasons) {
			this.cancelReasons = cancelReasons;
		}

		public List<CatalogItem> getMemberTypes() {
			return memberTypes;
		}

		public void setMemberTypes(List<CatalogItem> memberTypes) {
			this.memberTypes = memberTypes;
		}
	}

	public static class CatalogItem {
		private String code;
		private String label;
		private boolean active;

		public CatalogItem() {
		}

		public CatalogItem(String code, String label, boolean active) {
			this.code = code;
			this.label = label;
			this.active = active;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}
	}

	public static class PriceItem {
		private String code;
		private String label;
		private String zone;
		private String terminal;
		private double price;
		private boolean active;

		public PriceItem() {
		}

		public PriceItem(String code, String label, String zone, String terminal, double price, boolean active) {
			this.code = code;
			this.label = label;
			this.zone = zone;
			this.terminal = terminal;
			this.price = price;
			this.active = active;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getZone() {
			return zone;
		}

		public void setZone(String zone) {
			this.zone = zone;
		}

		public String getTerminal() {
			return terminal;
		}

		public void setTerminal(String terminal) {
			this.terminal = terminal;
		}

		public double getPrice() {
			return price;
		}

		public void setPrice(double price) {
			this.price = price;
		}

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}
	}

	public static class MemberProfile {
		private String id;
		private String name;
		private String memberType;
		private String employee;
		private String site;
		private String plate;
		private String sticker;
		private String expiry;
		private String status;
		private int dayTickets;
		private int nightTickets;
		private int totalTickets;
		private String nightSaving;
		private String personalSaving;
		private String cardPayment;
		private String notes;
		private List<MemberHistoryEntry> history = new ArrayList<>();

		public String getId() { return id; }
		public void setId(String id) { this.id = id; }
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		public String getMemberType() { return memberType; }
		public void setMemberType(String memberType) { this.memberType = memberType; }
		public String getEmployee() { return employee; }
		public void setEmployee(String employee) { this.employee = employee; }
		public String getSite() { return site; }
		public void setSite(String site) { this.site = site; }
		public String getPlate() { return plate; }
		public void setPlate(String plate) { this.plate = plate; }
		public String getSticker() { return sticker; }
		public void setSticker(String sticker) { this.sticker = sticker; }
		public String getExpiry() { return expiry; }
		public void setExpiry(String expiry) { this.expiry = expiry; }
		public String getStatus() { return status; }
		public void setStatus(String status) { this.status = status; }
		public int getDayTickets() { return dayTickets; }
		public void setDayTickets(int dayTickets) { this.dayTickets = dayTickets; }
		public int getNightTickets() { return nightTickets; }
		public void setNightTickets(int nightTickets) { this.nightTickets = nightTickets; }
		public int getTotalTickets() { return totalTickets; }
		public void setTotalTickets(int totalTickets) { this.totalTickets = totalTickets; }
		public String getNightSaving() { return nightSaving; }
		public void setNightSaving(String nightSaving) { this.nightSaving = nightSaving; }
		public String getPersonalSaving() { return personalSaving; }
		public void setPersonalSaving(String personalSaving) { this.personalSaving = personalSaving; }
		public String getCardPayment() { return cardPayment; }
		public void setCardPayment(String cardPayment) { this.cardPayment = cardPayment; }
		public String getNotes() { return notes; }
		public void setNotes(String notes) { this.notes = notes; }
		public List<MemberHistoryEntry> getHistory() { return history; }
		public void setHistory(List<MemberHistoryEntry> history) { this.history = history; }
	}

	public static class MemberHistoryEntry {
		private String title;
		private String detail;
		private String date;

		public MemberHistoryEntry() {
		}

		public MemberHistoryEntry(String title, String detail, String date) {
			this.title = title;
			this.detail = detail;
			this.date = date;
		}

		public String getTitle() { return title; }
		public void setTitle(String title) { this.title = title; }
		public String getDetail() { return detail; }
		public void setDetail(String detail) { this.detail = detail; }
		public String getDate() { return date; }
		public void setDate(String date) { this.date = date; }
	}

	public static class AuditEntry {
		private String type;
		private String title;
		private String detail;
		private String date;

		public AuditEntry() {
		}

		public AuditEntry(String type, String title, String detail, String date) {
			this.type = type;
			this.title = title;
			this.detail = detail;
			this.date = date;
		}

		public String getType() { return type; }
		public void setType(String type) { this.type = type; }
		public String getTitle() { return title; }
		public void setTitle(String title) { this.title = title; }
		public String getDetail() { return detail; }
		public void setDetail(String detail) { this.detail = detail; }
		public String getDate() { return date; }
		public void setDate(String date) { this.date = date; }
	}
}
