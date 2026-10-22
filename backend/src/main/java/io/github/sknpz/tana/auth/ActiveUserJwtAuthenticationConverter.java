package io.github.sknpz.tana.auth;

import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import io.github.sknpz.tana.user.AppUser;
import io.github.sknpz.tana.user.AppUserRepository;

/**
 * Transforma um JWT válido em usuário autenticado, conferindo o banco a cada requisição: desativar um
 * usuário ou mudar o seu perfil vale na hora, sem esperar o token expirar. Custa uma consulta pela
 * chave primária por requisição.
 */
@Component
public class ActiveUserJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final AppUserRepository userRepository;

    public ActiveUserJwtAuthenticationConverter(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        AppUser user = userRepository.findById(Long.valueOf(jwt.getSubject()))
                .filter(AppUser::isActive)
                .orElseThrow(() -> new DisabledException("Usuário desativado. Procure um administrador."));

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return new JwtAuthenticationToken(jwt, authorities, user.getEmail());
    }
}
