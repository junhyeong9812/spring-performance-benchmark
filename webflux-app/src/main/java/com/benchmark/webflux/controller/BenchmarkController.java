package com.benchmark.webflux.controller;

import com.benchmark.common.dto.ApiResponse;
import com.benchmark.common.dto.CreateUserRequest;
import com.benchmark.common.dto.UserDto;
import com.benchmark.webflux.service.BenchmarkService;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BenchmarkController {

  private final BenchmarkService benchmarkService;

  @Value("${benchmark.server-type}")
  private String serverType;

  public BenchmarkController(BenchmarkService benchmarkService) {
    this.benchmarkService = benchmarkService;
  }

  @GetMapping("/info")
  public Mono<Map<String, Object>> info() {
    return Mono.just(Map.of(
        "serverType", serverType,
        "javaVersion", System.getProperty("java.version"),
        "availableProcessors", Runtime.getRuntime().availableProcessors(),
        "maxMemory", Runtime.getRuntime().maxMemory() / 1024 / 1024 + "MB",
        "thread", Thread.currentThread().toString()
    ));
  }

  @GetMapping("/simple")
  @Timed(value = "api.simple")
  public Mono<ApiResponse<Map<String, Object>>> simple() {
    long start = System.currentTimeMillis();
    return benchmarkService.getSimpleResponse()
        .map(data -> ApiResponse.success(data, System.currentTimeMillis() - start, serverType));
  }

  @GetMapping("/delay/{ms}")
  @Timed(value = "api.delay")
  public Mono<ApiResponse<Map<String, Object>>> delay(@PathVariable int ms) {
    long start = System.currentTimeMillis();
    return benchmarkService.getDelayedResponse(ms)
        .map(data -> ApiResponse.success(data, System.currentTimeMillis() - start, serverType));
  }

  @GetMapping("/cpu/{iterations}")
  @Timed(value = "api.cpu")
  public Mono<ApiResponse<Map<String, Object>>> cpu(@PathVariable int iterations) {
    long start = System.currentTimeMillis();
    return benchmarkService.getCpuIntensiveResponse(iterations)
        .map(data -> ApiResponse.success(data, System.currentTimeMillis() - start, serverType));
  }

  @GetMapping("/db/users")
  @Timed(value = "api.db.read")
  public Mono<ApiResponse<List<UserDto>>> getUsers() {
    long start = System.currentTimeMillis();
    return benchmarkService.getAllUsers()
        .collectList()
        .map(users -> ApiResponse.success(users, System.currentTimeMillis() - start, serverType));
  }

  @PostMapping("/db/users")
  @Timed(value = "api.db.write")
  public Mono<ApiResponse<UserDto>> createUser(@RequestBody CreateUserRequest request) {
    long start = System.currentTimeMillis();
    return benchmarkService.createUser(request)
        .map(user -> ApiResponse.success(user, System.currentTimeMillis() - start, serverType));
  }

  @GetMapping("/external")
  @Timed(value = "api.external")
  public Mono<ApiResponse<Map<String, Object>>> external() {
    long start = System.currentTimeMillis();
    return benchmarkService.callExternalApi()
        .map(data -> ApiResponse.success(data, System.currentTimeMillis() - start, serverType));
  }

  @GetMapping(value = "/stream/{count}", produces = MediaType.APPLICATION_NDJSON_VALUE)
  @Timed(value = "api.stream")
  public Flux<Map<String, Object>> stream(@PathVariable int count) {
    return benchmarkService.generateLargeData(count);
  }
}