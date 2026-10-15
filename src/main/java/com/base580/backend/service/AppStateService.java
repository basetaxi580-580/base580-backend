package com.base580.backend.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.base580.backend.model.AppStateDocument;
import com.base580.backend.model.AppStateDocument.AuditEntry;
import com.base580.backend.model.AppStateDocument.CatalogItem;
import com.base580.backend.model.AppStateDocument.Catalogs;
import com.base580.backend.model.AppStateDocument.MemberHistoryEntry;
import com.base580.backend.model.AppStateDocument.MemberProfile;
import com.base580.backend.model.AppStateDocument.PriceItem;
import com.base580.backend.repository.AppStateRepository;

@Service
public class AppStateService {

	private final AppStateRepository repository;

	public AppStateService(AppStateRepository repository) {
		this.repository = repository;
	}

	public synchronized AppStateDocument current() {
		return repository.findById("default").orElseGet(() -> repository.save(AppStateDocument.sample()));
	}

	public synchronized AppStateDocument save(AppStateDocument state) {
		AppStateDocument next = state == null ? AppStateDocument.sample() : state;
		next.setId("default");
		next.setUpdatedAt(OffsetDateTime.now().toString());
		return repository.save(next);
	}

	public synchronized List<MemberProfile> members() {
		return new ArrayList<>(current().getMembers());
	}

	public synchronized MemberProfile member(String memberId) {
		return current().getMembers().stream().filter(member -> memberId.equals(member.getId())).findFirst().orElse(null);
	}

	public synchronized MemberProfile upsertMember(MemberProfile payload) {
		AppStateDocument state = current();
		MemberProfile existing = payload.getId() == null ? null : member(payload.getId());
		MemberProfile next = existing == null ? new MemberProfile() : cloneMember(existing);

		next.setId(payload.getId());
		next.setName(payload.getName());
		next.setMemberType(payload.getMemberType());
		next.setEmployee(payload.getEmployee());
		next.setSite(payload.getSite());
		next.setPlate(payload.getPlate());
		next.setSticker(payload.getSticker());
		next.setExpiry(payload.getExpiry());
		next.setStatus(payload.getStatus());
		next.setDayTickets(payload.getDayTickets());
		next.setNightTickets(payload.getNightTickets());
		next.setTotalTickets(payload.getTotalTickets());
		next.setNightSaving(payload.getNightSaving());
		next.setPersonalSaving(payload.getPersonalSaving());
		next.setCardPayment(payload.getCardPayment());
		next.setNotes(payload.getNotes());
		next.setHistory(payload.getHistory() == null ? new ArrayList<>() : new ArrayList<>(payload.getHistory()));

		if (existing == null) {
			state.getMembers().add(next);
			appendAudit(state, next.getId(), new AuditEntry("socio", "Ficha creada", "Se creó o registró una ficha desde el backend.", "Ahora"));
		} else {
			int index = indexOfMember(state, next.getId());
			state.getMembers().set(index, next);
			appendAudit(state, next.getId(), new AuditEntry("socio", "Ficha actualizada", "Se guardaron cambios desde captura.", "Ahora"));
		}

		state.setUpdatedAt(OffsetDateTime.now().toString());
		repository.save(state);
		return next;
	}

	public synchronized Catalogs catalogs() {
		return current().getCatalogs();
	}

	public synchronized Catalogs saveCatalogs(Catalogs catalogs) {
		AppStateDocument state = current();
		state.setCatalogs(catalogs == null ? new Catalogs() : catalogs);
		state.setUpdatedAt(OffsetDateTime.now().toString());
		repository.save(state);
		return state.getCatalogs();
	}

	public synchronized List<AuditEntry> audit(String memberId) {
		return new ArrayList<>(current().getAuditTrail().getOrDefault(memberId, List.of()));
	}

	public synchronized AuditEntry addAudit(String memberId, AuditEntry entry) {
		AppStateDocument state = current();
		appendAudit(state, memberId, entry);
		state.setUpdatedAt(OffsetDateTime.now().toString());
		repository.save(state);
		return entry;
	}

	public synchronized AppStateDocument seedIfEmpty() {
		return repository.findById("default").orElseGet(() -> repository.save(AppStateDocument.sample()));
	}

	private void appendAudit(AppStateDocument state, String memberId, AuditEntry entry) {
		state.getAuditTrail().computeIfAbsent(memberId, key -> new ArrayList<>()).add(0, entry);
		List<AuditEntry> trail = state.getAuditTrail().get(memberId);
		if (trail.size() > 20) {
			trail.subList(20, trail.size()).clear();
		}
	}

	private int indexOfMember(AppStateDocument state, String memberId) {
		for (int i = 0; i < state.getMembers().size(); i++) {
			if (memberId.equals(state.getMembers().get(i).getId())) {
				return i;
			}
		}
		return -1;
	}

	private MemberProfile cloneMember(MemberProfile source) {
		MemberProfile copy = new MemberProfile();
		copy.setId(source.getId());
		copy.setName(source.getName());
		copy.setMemberType(source.getMemberType());
		copy.setEmployee(source.getEmployee());
		copy.setSite(source.getSite());
		copy.setPlate(source.getPlate());
		copy.setSticker(source.getSticker());
		copy.setExpiry(source.getExpiry());
		copy.setStatus(source.getStatus());
		copy.setDayTickets(source.getDayTickets());
		copy.setNightTickets(source.getNightTickets());
		copy.setTotalTickets(source.getTotalTickets());
		copy.setNightSaving(source.getNightSaving());
		copy.setPersonalSaving(source.getPersonalSaving());
		copy.setCardPayment(source.getCardPayment());
		copy.setNotes(source.getNotes());
		copy.setHistory(source.getHistory() == null ? new ArrayList<>() : new ArrayList<>(source.getHistory()));
		return copy;
	}

	public synchronized AppStateDocument resetToSample() {
		return repository.save(AppStateDocument.sample());
	}

	public synchronized List<CatalogItem> catalogItems(String key) {
		Catalogs catalogs = catalogs();
		switch (key) {
			case "engomados":
				return catalogs.getEngomados();
			case "estatus":
				return catalogs.getStatuses();
			case "motivos":
				return catalogs.getCancelReasons();
			case "tipos":
				return catalogs.getMemberTypes();
			default:
				return catalogs.getEngomados();
		}
	}

	public synchronized List<PriceItem> priceItems() {
		return catalogs().getPriceList();
	}

	public synchronized void appendMemberHistory(String memberId, MemberHistoryEntry entry) {
		AppStateDocument state = current();
		MemberProfile member = member(memberId);
		if (member == null) return;
		member.getHistory().add(0, entry);
		if (member.getHistory().size() > 10) {
			member.getHistory().subList(10, member.getHistory().size()).clear();
		}
		state.setUpdatedAt(OffsetDateTime.now().toString());
		repository.save(state);
	}
}
