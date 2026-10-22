package io.github.sknpz.tana.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** O e-mail não muda (é o login). A senha é opcional: quando informada, o administrador redefine a senha do usuário. */
public record UserUpdateRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
        String name,

        @NotNull(message = "O perfil é obrigatório (ADMIN ou USER).")
        Role role,

        @NotNull(message = "Informe se o usuário está ativo.")
        Boolean active,

        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
        String password) {
}
