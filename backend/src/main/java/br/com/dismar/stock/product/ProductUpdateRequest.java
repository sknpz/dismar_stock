package br.com.dismar.stock.product;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Não tem campo de quantidade: o saldo só muda por movimentações de estoque. */
public record ProductUpdateRequest(
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

        @NotNull(message = "O estoque mínimo é obrigatório.")
        @PositiveOrZero(message = "O estoque mínimo não pode ser negativo.")
        Integer minStock) {
}
