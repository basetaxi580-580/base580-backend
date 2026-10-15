package com.base580.backend.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.base580.backend.model.AppStateDocument;
import com.base580.backend.model.AppStateDocument.AuditEntry;
import com.base580.backend.model.AppStateDocument.CatalogItem;
import com.base580.backend.model.AppStateDocument.MemberHistoryEntry;
import com.base580.backend.model.AppStateDocument.MemberProfile;
import com.base580.backend.model.AppStateDocument.PriceItem;
import com.base580.backend.service.AppStateService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
public class AppStateController {

	private final AppStateService service;

	public AppStateController(AppStateService service) {
		this.service = service;
	}

	@GetMapping("/state")
	public AppStateDocument getState() {
		return service.current();
	}

	@PutMapping("/state")
	public AppStateDocument saveState(@RequestBody AppStateDocument state) {
		return service.save(state);
	}

	@GetMapping("/members")
	public List<MemberProfile> members() {
		return service.members();
	}

	@GetMapping("/members/{id}")
	public MemberProfile member(@PathVariable String id) {
		return service.member(id);
	}

	@PutMapping("/members/{id}")
	public MemberProfile updateMember(@PathVariable String id, @RequestBody MemberProfile payload) {
		payload.setId(id);
		return service.upsertMember(payload);
	}

	@PostMapping("/members/{id}/history")
	@ResponseStatus(HttpStatus.CREATED)
	public MemberHistoryEntry appendHistory(@PathVariable String id, @RequestBody MemberHistoryEntry entry) {
		service.appendMemberHistory(id, entry);
		return entry;
	}

	@GetMapping("/members/{id}/audit")
	public List<AuditEntry> audit(@PathVariable String id) {
		return service.audit(id);
	}

	@GetMapping("/catalogs/{key}")
	public List<CatalogItem> catalogItems(@PathVariable String key) {
		return service.catalogItems(key);
	}

	@GetMapping("/catalogs/prices")
	public List<PriceItem> prices() {
		return service.priceItems();
	}

	@PutMapping("/catalogs")
	public AppStateDocument.Catalogs saveCatalogs(@RequestBody AppStateDocument.Catalogs catalogs) {
		return service.saveCatalogs(catalogs);
	}

	@PostMapping("/seed")
	public AppStateDocument seed() {
		return service.resetToSample();
	}
}
