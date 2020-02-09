package org.mk.travelhunter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

//TODO Move configuration in travel-hunter-lib
@SpringBootApplication
@EnableConfigurationProperties
@EnableCaching
@EnableReactiveMongoRepositories(basePackages = "org.mk.travelhunter.tracker")
public class TravelHunterApplication {


	public static void main(String[] args) {
		SpringApplication.run(TravelHunterApplication.class, args);
	}
}
