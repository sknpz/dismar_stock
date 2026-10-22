package io.github.sknpz.tana.product;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductCreateRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
        String name,

        @Size(max = 50, message = "O SKU deve ter no máximo 50 caracteres.")
        String sku,

        Long categoryId,

        Long supplierId,

        @NotNull(message = "O preço unitário é obrigatório.")
        @DecimalMin(value = "0.00", message = "O preço unitário não pode ser negativo.")
        @Digits(integer = 10, fraction = 2, message = "O preço unitário deve ter no máximo 2 casas decimais.")
        BigDecimal unitPrice,

        @PositiveOrZero(message = "O estoque mínimo não pode ser negativo.")
        Integer minStock,

        // Registrado como uma movimentação de entrada "Estoque inicial", não gravado direto no saldo
        @PositiveOrZero(message = "A quantidade inicial não pode ser negativa.")
        @Max(value = 1_000_000, message = "A quantidade inicial deve ser de no máximo 1.000.000.")
        Integer initialQuantity) {
}
