package com.benchmark.common.dto;

import java.time.Instant;

public record ApiResponse<T>(
    String status,
    T data,
    long processingTimeMs,
    Instant timestamp,
    String serverType
) {
  public static <T> ApiResponse<T> success(T data, long processingTimeMs, String serverType) {
    return new ApiResponse<>("success", data, processingTimeMs, Instant.now(), serverType);
  }

  public static <T> ApiResponse<T> error(String message, String serverType) {
    return new ApiResponse<>("error", null, 0, Instant.now(), serverType);
  }
}