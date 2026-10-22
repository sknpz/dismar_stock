package io.github.sknpz.tana.auth;

import java.time.Instant;

import io.github.sknpz.tana.user.UserResponse;

/** O cliente envia accessToken no header "Authorization: Bearer ..." até expiresAt. */
public record LoginResponse(String accessToken, String tokenType, Instant expiresAt, UserResponse user) {
}
