package io.github.sknpz.tana.product;

import java.math.BigDecimal;
import java.time.Instant;

import io.github.sknpz.tana.shared.EntityRef;

public record ProductResponse(
        Long id,
        String name,
        String sku,
        EntityRef category,
        EntityRef supplier,
        BigDecimal unitPrice,
        int quantity,
        int minStock,
        BigDecimal totalValue,
        StockStatus stockStatus,
        Instant createdAt,
        Instant updatedAt) {

    static ProductResponse from(Product product) {
        EntityRef category = product.getCategory() == null ? null
                : new EntityRef(product.getCategory().getId(), product.getCategory().getName());
        EntityRef supplier = product.getSupplier() == null ? null
                : new EntityRef(product.getSupplier().getId(), product.getSupplier().getName());

        return new ProductResponse(product.getId(), product.getName(), product.getSku(), category, supplier,
                product.getUnitPrice(), product.getQuantity(), product.getMinStock(), product.getTotalValue(),
                product.getStockStatus(), product.getCreatedAt(), product.getUpdatedAt());
    }
}
