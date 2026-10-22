package com.base580.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.base580.backend.model.TicketTariffDocument;

public interface TicketTariffRepository extends MongoRepository<TicketTariffDocument, String> {

	List<TicketTariffDocument> findByBaseAndVigenciaAndAmbito(String base, String vigencia, String ambito);

	Optional<TicketTariffDocument> findByBaseAndVigenciaAndAmbitoAndTipoAndZona(
		String base,
		String vigencia,
		String ambito,
		String tipo,
		String zona
	);

	boolean existsByBaseAndVigenciaAndAmbitoAndTipoAndZona(
		String base,
		String vigencia,
		String ambito,
		String tipo,
		String zona
	);

	long countByBaseAndVigenciaAndAmbito(String base, String vigencia, String ambito);
}
