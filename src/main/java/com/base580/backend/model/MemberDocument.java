package com.base580.backend.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("members")
public class MemberDocument {

	@Id
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
	private List<MemberAuditEntry> auditTrail = new ArrayList<>();

	public MemberDocument() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMemberType() {
		return memberType;
	}

	public void setMemberType(String memberType) {
		this.memberType = memberType;
	}

	public String getEmployee() {
		return employee;
	}

	public void setEmployee(String employee) {
		this.employee = employee;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getPlate() {
		return plate;
	}

	public void setPlate(String plate) {
		this.plate = plate;
	}

	public String getSticker() {
		return sticker;
	}

	public void setSticker(String sticker) {
		this.sticker = sticker;
	}

	public String getExpiry() {
		return expiry;
	}

	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getDayTickets() {
		return dayTickets;
	}

	public void setDayTickets(int dayTickets) {
		this.dayTickets = dayTickets;
	}

	public int getNightTickets() {
		return nightTickets;
	}

	public void setNightTickets(int nightTickets) {
		this.nightTickets = nightTickets;
	}

	public int getTotalTickets() {
		return totalTickets;
	}

	public void setTotalTickets(int totalTickets) {
		this.totalTickets = totalTickets;
	}

	public String getNightSaving() {
		return nightSaving;
	}

	public void setNightSaving(String nightSaving) {
		this.nightSaving = nightSaving;
	}

	public String getPersonalSaving() {
		return personalSaving;
	}

	public void setPersonalSaving(String personalSaving) {
		this.personalSaving = personalSaving;
	}

	public String getCardPayment() {
		return cardPayment;
	}

	public void setCardPayment(String cardPayment) {
		this.cardPayment = cardPayment;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public List<MemberHistoryEntry> getHistory() {
		return history;
	}

	public void setHistory(List<MemberHistoryEntry> history) {
		this.history = history;
	}

	public List<MemberAuditEntry> getAuditTrail() {
		return auditTrail;
	}

	public void setAuditTrail(List<MemberAuditEntry> auditTrail) {
		this.auditTrail = auditTrail;
	}

	public MemberDocument copy() {
		MemberDocument copy = new MemberDocument();
		copy.setId(id);
		copy.setName(name);
		copy.setMemberType(memberType);
		copy.setEmployee(employee);
		copy.setSite(site);
		copy.setPlate(plate);
		copy.setSticker(sticker);
		copy.setExpiry(expiry);
		copy.setStatus(status);
		copy.setDayTickets(dayTickets);
		copy.setNightTickets(nightTickets);
		copy.setTotalTickets(totalTickets);
		copy.setNightSaving(nightSaving);
		copy.setPersonalSaving(personalSaving);
		copy.setCardPayment(cardPayment);
		copy.setNotes(notes);
		copy.setHistory(history == null ? new ArrayList<>() : new ArrayList<>(history));
		copy.setAuditTrail(auditTrail == null ? new ArrayList<>() : new ArrayList<>(auditTrail));
		return copy;
	}

	public static MemberDocument sample() {
		MemberDocument member = new MemberDocument();
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
		member.getAuditTrail().add(new MemberAuditEntry("socio", "Ficha cargada", "Se abrió la información principal del socio desde la búsqueda global.", "Hoy"));
		member.getAuditTrail().add(new MemberAuditEntry("socio", "Vigencia verificada", "Se confirmó la fecha de vencimiento y la asignación actual de engomado.", "Reciente"));
		member.getAuditTrail().add(new MemberAuditEntry("socio", "Sin movimientos pendientes", "Los boletos y balances están en cero para este perfil.", "Histórico"));
		return member;
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

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDetail() {
			return detail;
		}

		public void setDetail(String detail) {
			this.detail = detail;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}
	}

	public static class MemberAuditEntry {
		private String type;
		private String title;
		private String detail;
		private String date;

		public MemberAuditEntry() {
		}

		public MemberAuditEntry(String type, String title, String detail, String date) {
			this.type = type;
			this.title = title;
			this.detail = detail;
			this.date = date;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDetail() {
			return detail;
		}

		public void setDetail(String detail) {
			this.detail = detail;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}
	}
}
