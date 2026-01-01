package org.testspringboot.unibuy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("org.testspringboot.unibuy.mapper")
public class UniBuyApplication {

	public static void main(String[] args) {
		SpringApplication.run(UniBuyApplication.class, args);
	}

}
