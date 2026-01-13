package org.example.tuitui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing; // [新增]

@SpringBootApplication
@EnableJpaAuditing
public class TuituiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TuituiApplication.class, args);
    }

}
