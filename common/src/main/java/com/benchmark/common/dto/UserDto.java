package com.benchmark.common.dto;

import java.time.LocalDateTime;

public record UserDto(
    Long id,
    String username,
    String email,
    LocalDateTime createdAt
) {}
