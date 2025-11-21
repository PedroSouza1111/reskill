package br.com.fiap.reskill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ReskillApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReskillApplication.class, args);
	}

}
