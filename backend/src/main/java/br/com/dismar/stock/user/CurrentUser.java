package br.com.dismar.stock.user;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/** Acesso ao usuário autenticado na requisição atual, para registrar quem fez cada operação. */
@Component
public class CurrentUser {

    private final AppUserRepository userRepository;

    public CurrentUser(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Referência ao usuário logado (sem consultar o banco), ou vazio quando não há requisição autenticada,
     * como em chamadas diretas aos services nos testes.
     */
    public Optional<AppUser> reference() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken token) {
            return Optional.of(userRepository.getReferenceById(Long.valueOf(token.getToken().getSubject())));
        }
        return Optional.empty();
    }
}
