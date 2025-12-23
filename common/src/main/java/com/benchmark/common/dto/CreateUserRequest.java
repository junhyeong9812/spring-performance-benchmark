package com.benchmark.common.dto;

public record CreateUserRequest(
    String username,
    String email
) {}
