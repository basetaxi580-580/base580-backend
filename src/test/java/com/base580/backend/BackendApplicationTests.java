package com.base580.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.MongoDatabaseFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "MONGODB_URI=mongodb://127.0.0.1:27019/base580_configuration_test")
class BackendApplicationTests {

	@Autowired
	private MongoDatabaseFactory databaseFactory;

	@Test
	void contextLoads() {
	}

	@Test
	void usesConfiguredMongoDatabase() {
		// Getting the database handle does not read or modify any data.
		assertEquals("base580_configuration_test", databaseFactory.getMongoDatabase().getName());
	}

}
