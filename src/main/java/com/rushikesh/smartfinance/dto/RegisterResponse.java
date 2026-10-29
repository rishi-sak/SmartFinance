package com.rushikesh.smartfinance.dto;

public record RegisterResponse(
        Long id,
        String name,
        String email,
        String role
) {
}
