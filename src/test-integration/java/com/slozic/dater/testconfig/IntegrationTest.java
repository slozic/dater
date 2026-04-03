package com.slozic.dater.testconfig;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@ContextConfiguration(initializers = {TestPostgreSQLContainer.class})
@AutoConfigureMockMvc
public abstract class IntegrationTest {
}
