package com.benchmark.mvc.config;

import com.benchmark.mvc.entity.User;
import com.benchmark.mvc.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.IntStream;

@Configuration
public class DataInitializer {

  private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

  @Bean
  public CommandLineRunner initData(UserRepository userRepository) {
    return args -> {
      log.info("Initializing test data...");

      var users = IntStream.range(1, 101)
          .mapToObj(i -> new User("user" + i, "user" + i + "@example.com"))
          .toList();

      userRepository.saveAll(users);
      log.info("Initialized {} users", users.size());
    };
  }
}