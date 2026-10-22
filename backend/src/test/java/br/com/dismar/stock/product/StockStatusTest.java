package br.com.dismar.stock.product;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StockStatusTest {

    @ParameterizedTest(name = "quantidade {0}, mínimo {1} -> {2}")
    @CsvSource({
            "0, 5, OUT_OF_STOCK",
            "0, 0, OUT_OF_STOCK",   // zerado é "sem estoque" mesmo com mínimo 0
            "1, 5, LOW_STOCK",
            "5, 5, LOW_STOCK",      // no limite do mínimo ainda é estoque baixo
            "6, 5, OK",
            "1, 0, OK"
    })
    void classifiesStockLevel(int quantity, int minStock, StockStatus expected) {
        assertThat(StockStatus.of(quantity, minStock)).isEqualTo(expected);
    }
}
