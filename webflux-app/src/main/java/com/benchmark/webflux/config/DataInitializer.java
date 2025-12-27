package com.benchmark.webflux.config;

import com.benchmark.webflux.entity.User;
import com.benchmark.webflux.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

@Configuration
public class DataInitializer {

  private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

  @Bean
  public ApplicationRunner initData(UserRepository userRepository) {
    return args -> {
      log.info("Initializing test data...");

      Flux.range(1, 100)
          .map(i -> new User("user" + i, "user" + i + "@example.com"))
          .flatMap(userRepository::save)
          .doOnComplete(() -> log.info("Initialized 100 users"))
          .subscribe();
    };
  }
}