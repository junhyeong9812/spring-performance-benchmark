package com.benchmark.mvc.controller;

import com.benchmark.common.dto.ApiResponse;
import com.benchmark.common.dto.CreateUserRequest;
import com.benchmark.common.dto.UserDto;
import com.benchmark.mvc.service.BenchmarkService;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
  public ResponseEntity<Map<String, Object>> info() {
    return ResponseEntity.ok(Map.of(
        "serverType", serverType,
        "javaVersion", System.getProperty("java.version"),
        "availableProcessors", Runtime.getRuntime().availableProcessors(),
        "maxMemory", Runtime.getRuntime().maxMemory() / 1024 / 1024 + "MB",
        "thread", Thread.currentThread().toString()
    ));
  }

  @GetMapping("/simple")
  @Timed(value = "api.simple")
  public ResponseEntity<ApiResponse<Map<String, Object>>> simple() {
    long start = System.currentTimeMillis();
    var data = benchmarkService.getSimpleResponse();
    return ResponseEntity.ok(ApiResponse.success(data, System.currentTimeMillis() - start, serverType));
  }

  @GetMapping("/delay/{ms}")
  @Timed(value = "api.delay")
  public ResponseEntity<ApiResponse<Map<String, Object>>> delay(@PathVariable int ms) {
    long start = System.currentTimeMillis();
    var data = benchmarkService.getDelayedResponse(ms);
    return ResponseEntity.ok(ApiResponse.success(data, System.currentTimeMillis() - start, serverType));
  }

  @GetMapping("/cpu/{iterations}")
  @Timed(value = "api.cpu")
  public ResponseEntity<ApiResponse<Map<String, Object>>> cpu(@PathVariable int iterations) {
    long start = System.currentTimeMillis();
    var data = benchmarkService.getCpuIntensiveResponse(iterations);
    return ResponseEntity.ok(ApiResponse.success(data, System.currentTimeMillis() - start, serverType));
  }

  @GetMapping("/db/users")
  @Timed(value = "api.db.read")
  public ResponseEntity<ApiResponse<List<UserDto>>> getUsers() {
    long start = System.currentTimeMillis();
    var users = benchmarkService.getAllUsers();
    return ResponseEntity.ok(ApiResponse.success(users, System.currentTimeMillis() - start, serverType));
  }

  @PostMapping("/db/users")
  @Timed(value = "api.db.write")
  public ResponseEntity<ApiResponse<UserDto>> createUser(@RequestBody CreateUserRequest request) {
    long start = System.currentTimeMillis();
    var user = benchmarkService.createUser(request);
    return ResponseEntity.ok(ApiResponse.success(user, System.currentTimeMillis() - start, serverType));
  }

  @GetMapping("/external")
  @Timed(value = "api.external")
  public ResponseEntity<ApiResponse<Map<String, Object>>> external() {
    long start = System.currentTimeMillis();
    var data = benchmarkService.callExternalApi();
    return ResponseEntity.ok(ApiResponse.success(data, System.currentTimeMillis() - start, serverType));
  }

  @GetMapping("/stream/{count}")
  @Timed(value = "api.stream")
  public ResponseEntity<ApiResponse<List<Map<String, Object>>>> stream(@PathVariable int count) {
    long start = System.currentTimeMillis();
    var data = benchmarkService.generateLargeData(count);
    return ResponseEntity.ok(ApiResponse.success(data, System.currentTimeMillis() - start, serverType));
  }
}