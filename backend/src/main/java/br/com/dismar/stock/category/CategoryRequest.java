package br.com.dismar.stock.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 80, message = "O nome deve ter no máximo 80 caracteres.")
        String name) {
}
