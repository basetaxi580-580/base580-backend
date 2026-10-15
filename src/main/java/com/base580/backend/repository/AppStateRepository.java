package com.base580.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.base580.backend.model.AppStateDocument;

public interface AppStateRepository extends MongoRepository<AppStateDocument, String> {
}
