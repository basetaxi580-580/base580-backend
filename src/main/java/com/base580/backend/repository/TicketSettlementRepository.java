package com.base580.backend.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.base580.backend.model.TicketSettlementDocument;

public interface TicketSettlementRepository extends MongoRepository<TicketSettlementDocument, String> {

	List<TicketSettlementDocument> findByBaseAndVigenciaAndAmbitoOrderBySavedAtDesc(String base, String vigencia, String ambito);

	List<TicketSettlementDocument> findAllByOrderBySavedAtDesc();
}
