package io.github.sknpz.tana.auth;

import java.util.Optional;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.sknpz.tana.auth.TokenService.IssuedToken;
import io.github.sknpz.tana.user.AppUser;
import io.github.sknpz.tana.user.AppUserRepository;
import io.github.sknpz.tana.user.UserResponse;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final String dummyPasswordHash;

    public AuthService(AppUserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.dummyPasswordHash = passwordEncoder.encode("senha-ficticia-para-comparacao");
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Optional<AppUser> user = userRepository.findByEmailIgnoreCase(request.email().trim());

        // Compara a senha mesmo quando o e-mail não existe: o tempo de resposta igual
        // não revela quais e-mails estão cadastrados
        String hash = user.map(AppUser::getPasswordHash).orElse(dummyPasswordHash);
        boolean passwordMatches = passwordEncoder.matches(request.password(), hash);

        if (user.isEmpty() || !passwordMatches) {
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        }
        if (!user.get().isActive()) {
            throw new DisabledException("Usuário desativado. Procure um administrador.");
        }

        IssuedToken token = tokenService.issue(user.get());
        return new LoginResponse(token.value(), "Bearer", token.expiresAt(), UserResponse.from(user.get()));
    }
}
