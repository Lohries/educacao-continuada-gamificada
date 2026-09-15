package br.edu.gamificacao.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import io.cucumber.spring.ScenarioScope;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
@Import(CucumberSpringConfiguration.TestBeans.class)
public class CucumberSpringConfiguration {

    /**
     * Beans usados apenas pelos testes de aceitacao (Cucumber). Registrados
     * aqui, e nao via @Component, para nao serem varridos pelo component
     * scan da aplicacao principal e vazarem para outros testes
     * @SpringBootTest do projeto (ex.: o teste padrao de contexto, que nao
     * sobe servidor web e nao possui um bean TestRestTemplate).
     */
    @TestConfiguration
    static class TestBeans {

        @Bean
        @ScenarioScope
        public CenarioContexto cenarioContexto() {
            return new CenarioContexto();
        }

        @Bean
        public ApiTestHelper apiTestHelper() {
            return new ApiTestHelper();
        }
    }
}
