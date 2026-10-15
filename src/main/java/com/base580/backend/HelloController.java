package com.base580.backend;

import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	@GetMapping("/")
	public Map<String, Object> home() {
		return Map.of(
			"message", "Hola mundo desde Spring Boot",
			"service", "base580",
			"timestamp", OffsetDateTime.now().toString()
		);
	}
}
