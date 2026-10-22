package br.com.dismar.stock.user;

import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.dismar.stock.shared.exception.BusinessException;
import br.com.dismar.stock.shared.exception.ConflictException;
import br.com.dismar.stock.shared.exception.ResourceNotFoundException;

@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return userRepository.findAllByOrderByNameAsc().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return UserResponse.from(findById(id));
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Já existe um usuário com o e-mail " + email + ".");
        }
        AppUser user = new AppUser(request.name().trim(), email, passwordEncoder.encode(request.password()),
                request.role());
        return UserResponse.from(userRepository.save(user));
    }

    /**
     * Um administrador não pode se desativar nem tirar o próprio perfil de ADMIN. Como só administradores
     * alteram usuários, isso garante que sempre reste pelo menos um administrador ativo.
     */
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request, Long actingUserId) {
        AppUser user = findById(id);
        if (id.equals(actingUserId) && (!request.active() || request.role() != Role.ADMIN)) {
            throw new BusinessException("Você não pode desativar o próprio usuário nem remover o seu perfil de administrador.");
        }

        user.update(request.name().trim(), request.role(), request.active());
        if (request.password() != null && !request.password().isBlank()) {
            user.changePassword(passwordEncoder.encode(request.password()));
        }
        return UserResponse.from(user);
    }

    @Transactional
    public void changeOwnPassword(Long userId, String currentPassword, String newPassword) {
        AppUser user = findById(userId);
        // 422 e não 401: a sessão continua válida, só a senha informada está errada
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException("A senha atual está incorreta.");
        }
        user.changePassword(passwordEncoder.encode(newPassword));
    }

    @Transactional(readOnly = true)
    public AppUser findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
    }

    static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
