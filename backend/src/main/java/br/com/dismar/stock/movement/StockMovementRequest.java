package br.com.dismar.stock.movement;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record StockMovementRequest(
        @NotNull(message = "O tipo é obrigatório (ENTRY ou EXIT).")
        MovementType type,

        @NotNull(message = "A quantidade é obrigatória.")
        @Positive(message = "A quantidade deve ser maior que zero.")
        @Max(value = 1_000_000, message = "A quantidade deve ser de no máximo 1.000.000.")
        Integer quantity,

        @Size(max = 255, message = "As observações devem ter no máximo 255 caracteres.")
        String notes) {
}
