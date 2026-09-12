package com.bidyatra.staff.dto;

public record OtpVerifyRequest(
        String username,
        String code
) {}