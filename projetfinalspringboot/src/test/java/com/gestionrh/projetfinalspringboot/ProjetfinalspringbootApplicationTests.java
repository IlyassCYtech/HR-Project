package com.gestionrh.projetfinalspringboot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class ProjetfinalspringbootApplicationTests {

	@Test
	void contextLoads() {
		// Test que le contexte Spring Boot se charge correctement
	}

}
