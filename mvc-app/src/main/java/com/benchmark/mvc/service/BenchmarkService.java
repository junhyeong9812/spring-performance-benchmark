package com.benchmark.mvc.service;

import com.benchmark.common.dto.CreateUserRequest;
import com.benchmark.common.dto.UserDto;
import com.benchmark.common.util.CpuIntensiveUtil;
import com.benchmark.mvc.entity.User;
import com.benchmark.mvc.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
public class BenchmarkService {

  private final UserRepository userRepository;
  private final RestClient restClient;

  public BenchmarkService(UserRepository userRepository, RestClient.Builder restClientBuilder) {
    this.userRepository = userRepository;
    this.restClient = restClientBuilder.baseUrl("https://httpbin.org").build();
  }

  public Map<String, Object> getSimpleResponse() {
     return Map.of(
       "message", "Hello from MVC!",
       "timestamp", System.currentTimeMillis(),
       "thread", Thread.currentThread().toString()
     );
  }

  public Map<String, Object> getDelayedResponse(int delayMs) {
    try {
      TimeUnit.MICROSECONDS.sleep(delayMs);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }

    return Map.of(
        "delayMs", delayMs,
        "thread", Thread.currentThread().toString()
    );
  }

  public Map<String, Object> getCpuIntensiveResponse(int iterations) {
    long startTime = System.nanoTime();
    String hash = CpuIntensiveUtil.hashIterations(iterations);
    long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

    return Map.of(
        "iterations", iterations,
        "hasResult", hash.substring(0, 16) + "...",
        "computeTimeMs", duration,
        "thread", Thread.currentThread().toString()
    );
  }

  @Transactional
  public List<UserDto> getAllUsers() {
    return userRepository.findAllOrderByCreatedAtDesc().stream()
        .map(user -> new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getCreatedAt()))
        .toList();
  }

  @Transactional
  public UserDto createUser(CreateUserRequest request) {
    String uniqueUsername = request.username() + "_" + System.nanoTime();
    User user = new User(uniqueUsername, request.email());
    User saved = userRepository.save(user);
    return new UserDto(saved.getId(), saved.getUsername(), saved.getEmail(), saved.getCreatedAt());
  }

  public Map<String, Object> callExternalApi() {
    long startTime = System.nanoTime();

    String response = restClient.get()
        .uri("/get")
        .retrieve()
        .body(String.class);

    long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

    return Map.of(
        "externalCallTimeMs", duration,
        "responseLength", response != null ? response.length() : 0,
        "thread", Thread.currentThread().toString()
    );
  }

  public List<Map<String, Object>> generateLargeData(int count) {
    return IntStream.range(0, count)
        .mapToObj(i -> Map.<String, Object>of(
            "id", i,
            "data", "Item-" + i + "-" + "x".repeat(100),
            "timestamp", System.currentTimeMillis()
        ))
        .toList();
  }
}
