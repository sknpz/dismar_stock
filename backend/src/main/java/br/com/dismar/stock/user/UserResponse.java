package br.com.dismar.stock.user;

import java.time.Instant;

public record UserResponse(Long id, String name, String email, Role role, boolean active, Instant createdAt) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isActive(),
                user.getCreatedAt());
    }
}
