package io.github.sknpz.tana.user;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

/** Criação do primeiro administrador a partir de ADMIN_EMAIL/ADMIN_PASSWORD (sem banco: dependências simuladas). */
class AdminBootstrapTest {

    private final AppUserRepository userRepository = mock(AppUserRepository.class);
    private final UserService userService = mock(UserService.class);

    private AdminBootstrap bootstrap(String email, String password) {
        return new AdminBootstrap(userRepository, userService, "Administrador", email, password);
    }

    @Test
    void createsAdminWhenThereAreNoUsers() {
        when(userRepository.count()).thenReturn(0L);

        bootstrap("Admin@Loja.com", "senha-forte-123").run(null);

        verify(userService).create(new UserCreateRequest("Administrador", "Admin@Loja.com", "senha-forte-123", Role.ADMIN));
    }

    @Test
    void doesNothingWhenUsersAlreadyExist() {
        when(userRepository.count()).thenReturn(3L);

        bootstrap("admin@loja.com", "senha-forte-123").run(null);

        verify(userService, never()).create(any());
    }

    @Test
    void onlyWarnsWhenVariablesAreMissing() {
        when(userRepository.count()).thenReturn(0L);

        bootstrap("", "").run(null);
        bootstrap("admin@loja.com", "").run(null);

        verify(userService, never()).create(any());
    }

    @Test
    void refusesShortPasswordInsteadOfCreatingAWeakAdmin() {
        when(userRepository.count()).thenReturn(0L);

        assertThatThrownBy(() -> bootstrap("admin@loja.com", "1234567").run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ADMIN_PASSWORD");
        verify(userService, never()).create(any());
    }
}
