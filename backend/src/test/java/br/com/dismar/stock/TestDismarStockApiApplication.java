package br.com.dismar.stock;

import org.springframework.boot.SpringApplication;

public class TestDismarStockApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(DismarStockApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
