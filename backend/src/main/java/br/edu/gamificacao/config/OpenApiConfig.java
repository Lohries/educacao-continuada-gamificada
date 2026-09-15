package br.edu.gamificacao.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI().info(new Info()
                .title("API - Educacao Continuada Gamificada")
                .version("v1")
                .description("Plataforma de educacao continuada com gamificacao (XP, niveis e badges). "
                        + "Projeto academico desenvolvido com TDD/BDD (ATDD) em Spring Boot.")
                .contact(new Contact()
                        .name("Mathues de Luzia Souza, Emanuel Ronaldo Gomes de Souza, Luis Henrique Telo Ladeira Mota")));
    }
}
