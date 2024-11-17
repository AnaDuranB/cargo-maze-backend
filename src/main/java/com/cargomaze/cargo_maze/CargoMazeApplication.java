package com.cargomaze.cargo_maze;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.cargomaze.cargo_maze"})
public class CargoMazeApplication {
	public static void main(String[] args) {
		SpringApplication.run(CargoMazeApplication.class, args);
	}
}
