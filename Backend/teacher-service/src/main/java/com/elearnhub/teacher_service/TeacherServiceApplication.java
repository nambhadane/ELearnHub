package com.elearnhub.teacher_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@SpringBootApplication
//@EnableEurekaServer
public class TeacherServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeacherServiceApplication.class, args);
		System.out.println("Hello from teacher!");
		
		
//		public static void main(String[] args) {
		    System.out.println(new BCryptPasswordEncoder().encode("admin123"));
//		}

		
	}

}
