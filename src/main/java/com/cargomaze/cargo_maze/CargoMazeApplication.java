package com.cargomaze.cargo_maze;

import com.azure.core.annotation.Get;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class CargoMazeApplication {

	public static void main(String[] args) {
		SpringApplication.run(CargoMazeApplication.class, args);
	}
}
