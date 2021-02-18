package com.example.springBootDemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SpringBootDemoApplication {

	private static ApplicationContext applicationContext;

	public static void main(String[] args) {
		applicationContext =  SpringApplication.run(SpringBootDemoApplication.class, args);
	}

}
