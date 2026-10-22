package io.github.sknpz.tana.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Cria o primeiro administrador a partir de ADMIN_EMAIL e ADMIN_PASSWORD quando o banco ainda não tem
 * nenhum usuário. Depois disso as variáveis são ignoradas: os demais usuários são criados pela API.
 */
@Component
class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final AppUserRepository userRepository;
    private final UserService userService;
    private final String name;
    private final String email;
    private final String password;

    AdminBootstrap(AppUserRepository userRepository, UserService userService,
            @Value("${app.admin.name}") String name,
            @Value("${app.admin.email}") String email,
            @Value("${app.admin.password}") String password) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        if (email.isBlank() || password.isBlank()) {
            log.warn("Nenhum usuário cadastrado. Defina ADMIN_EMAIL e ADMIN_PASSWORD para criar o primeiro administrador.");
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException("ADMIN_PASSWORD deve ter pelo menos " + MIN_PASSWORD_LENGTH + " caracteres.");
        }

        userService.create(new UserCreateRequest(name, email, password, Role.ADMIN));
        log.info("Administrador inicial criado: {}", UserService.normalizeEmail(email));
    }
}
