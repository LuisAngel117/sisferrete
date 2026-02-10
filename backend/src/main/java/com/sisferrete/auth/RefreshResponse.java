package com.sisferrete.auth;

public record RefreshResponse(
    String accessToken,
    long expiresInSeconds
) {}