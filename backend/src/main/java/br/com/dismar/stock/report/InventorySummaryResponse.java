package br.com.dismar.stock.report;

import java.math.BigDecimal;
import java.util.List;

public record InventorySummaryResponse(
        long totalProducts,
        long totalQuantity,
        BigDecimal totalValue,
        BigDecimal averageValuePerProduct,
        long lowStockProducts,
        long outOfStockProducts,
        long categories,
        long movementsThisMonth,
        List<CategoryValue> valueByCategory) {

    public record CategoryValue(String category, long quantity, BigDecimal value, BigDecimal percentage) {
    }
}
