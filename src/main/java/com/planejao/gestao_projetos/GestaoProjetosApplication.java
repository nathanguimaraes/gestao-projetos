package com.planejao.gestao_projetos;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@OpenAPIDefinition
@SpringBootApplication
public class GestaoProjetosApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestaoProjetosApplication.class, args);
		System.out.println("Hello Wooorld!");
	}
}
