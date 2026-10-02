package com.manh.openbanking.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.manh.openbanking")
public class Application {
    public static void main(String[] args) { SpringApplication.run(Application.class, args); }
}
