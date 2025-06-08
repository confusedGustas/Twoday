package org.twoday.vibe.coding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class TwodayApplication {

    public static void main(String[] args) {
        SpringApplication.run(TwodayApplication.class, args);
    }

}
