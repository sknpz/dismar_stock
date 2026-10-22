package br.com.dismar.stock.movement;

import java.time.Instant;

import br.com.dismar.stock.shared.EntityRef;

public record StockMovementResponse(
        Long id,
        EntityRef product,
        MovementType type,
        int quantity,
        int previousQuantity,
        int newQuantity,
        String notes,
        EntityRef user,
        Instant createdAt) {

    static StockMovementResponse from(StockMovement movement) {
        EntityRef product = new EntityRef(movement.getProduct().getId(), movement.getProduct().getName());
        EntityRef user = movement.getUser() == null ? null
                : new EntityRef(movement.getUser().getId(), movement.getUser().getName());
        return new StockMovementResponse(movement.getId(), product, movement.getType(), movement.getQuantity(),
                movement.getPreviousQuantity(), movement.getNewQuantity(), movement.getNotes(), user,
                movement.getCreatedAt());
    }
}
