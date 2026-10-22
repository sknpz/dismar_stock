package io.github.sknpz.tana;

import org.springframework.boot.SpringApplication;

public class TestTanaStockApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(TanaStockApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
