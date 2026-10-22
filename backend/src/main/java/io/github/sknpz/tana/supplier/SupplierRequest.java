package io.github.sknpz.tana.supplier;

import io.github.sknpz.tana.shared.validation.ValidCnpj;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
        String name,

        @ValidCnpj
        String cnpj,

        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
        String phone,

        @Email(message = "E-mail inválido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        String email,

        @Size(max = 255, message = "O endereço deve ter no máximo 255 caracteres.")
        String address) {
}
