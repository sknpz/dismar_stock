package io.github.sknpz.tana.product;

public enum StockStatus {
    OK,
    LOW_STOCK,
    OUT_OF_STOCK;

    public static StockStatus of(int quantity, int minStock) {
        if (quantity == 0) {
            return OUT_OF_STOCK;
        }
        return quantity <= minStock ? LOW_STOCK : OK;
    }
}
