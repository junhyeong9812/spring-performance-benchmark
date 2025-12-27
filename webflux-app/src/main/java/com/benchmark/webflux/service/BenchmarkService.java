package com.benchmark.webflux.service;

import com.benchmark.common.dto.CreateUserRequest;
import com.benchmark.common.dto.UserDto;
import com.benchmark.common.util.CpuIntensiveUtil;
import com.benchmark.webflux.entity.User;
import com.benchmark.webflux.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class BenchmarkService {

  private final UserRepository userRepository;
  private final WebClient webClient;

  public BenchmarkService(UserRepository userRepository, WebClient.Builder webClientBuilder) {
    this.userRepository = userRepository;
    this.webClient = webClientBuilder.baseUrl("https://httpbin.org").build();
  }

  public Mono<Map<String, Object>> getSimpleResponse() {
    return Mono.just(Map.of(
        "message", "Hello from WebFlux!",
        "timestamp", System.currentTimeMillis(),
        "thread", Thread.currentThread().toString()
    ));
  }

  public Mono<Map<String, Object>> getDelayedResponse(int delayMs) {
    return Mono.delay(Duration.ofMillis(delayMs))
        .map(tick -> Map.<String, Object>of(
            "delayMs", delayMs,
            "thread", Thread.currentThread().toString()
        ));
  }

  public Mono<Map<String, Object>> getCpuIntensiveResponse(int iterations) {
    return Mono.fromCallable(() -> {
      long startTime = System.nanoTime();
      String hash = CpuIntensiveUtil.hashIterations(iterations);
      long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

      return Map.<String, Object>of(
          "iterations", iterations,
          "hashResult", hash.substring(0, 16) + "...",
          "computeTimeMs", duration,
          "thread", Thread.currentThread().toString()
      );
    }).subscribeOn(Schedulers.boundedElastic());
  }

  public Flux<UserDto> getAllUsers() {
    return userRepository.findAllOrderByCreatedAtDesc()
        .map(user -> new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getCreatedAt()));
  }

  public Mono<UserDto> createUser(CreateUserRequest request) {
    String uniqueUsername = request.username() + "_" + System.nanoTime();
    User user = new User(uniqueUsername, request.email());

    return userRepository.save(user)
        .map(saved -> new UserDto(saved.getId(), saved.getUsername(), saved.getEmail(), saved.getCreatedAt()));
  }

  public Mono<Map<String, Object>> callExternalApi() {
    long startTime = System.nanoTime();

    return webClient.get()
        .uri("/get")
        .retrieve()
        .bodyToMono(String.class)
        .map(response -> {
          long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
          return Map.<String, Object>of(
              "externalCallTimeMs", duration,
              "responseLength", response != null ? response.length() : 0,
              "thread", Thread.currentThread().toString()
          );
        });
  }

  public Flux<Map<String, Object>> generateLargeData(int count) {
    return Flux.range(0, count)
        .map(i -> Map.<String, Object>of(
            "id", i,
            "data", "Item-" + i + "-" + "x".repeat(100),
            "timestamp", System.currentTimeMillis()
        ));
  }
}