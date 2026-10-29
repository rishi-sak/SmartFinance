package com.rushikesh.smartfinance.dto;

public record ApiErrorResponse(
        int status,
        String error,
        String message
) {
}