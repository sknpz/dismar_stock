package br.com.dismar.stock.auth;

import java.time.Instant;

import br.com.dismar.stock.user.UserResponse;

/** O cliente envia accessToken no header "Authorization: Bearer ..." até expiresAt. */
public record LoginResponse(String accessToken, String tokenType, Instant expiresAt, UserResponse user) {
}
