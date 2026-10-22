package io.github.sknpz.tana.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import io.github.sknpz.tana.movement.MovementType;
import io.github.sknpz.tana.movement.StockMovement;
import io.github.sknpz.tana.shared.exception.BusinessException;

/** Regra de saldo do produto, sem banco: o saldo só muda por movimentação e nunca fica negativo. */
class ProductTest {

    private Product productWithStock(int quantity) {
        Product product = new Product("Parafuso", "PAR-001", null, null, new BigDecimal("2.50"), 10);
        if (quantity > 0) {
            product.registerMovement(MovementType.ENTRY, quantity, "Estoque inicial", null);
        }
        return product;
    }

    @Test
    void entryIncreasesBalanceAndRecordsBothBalances() {
        Product product = productWithStock(5);

        StockMovement movement = product.registerMovement(MovementType.ENTRY, 3, "Compra", null);

        assertThat(product.getQuantity()).isEqualTo(8);
        assertThat(movement.getPreviousQuantity()).isEqualTo(5);
        assertThat(movement.getNewQuantity()).isEqualTo(8);
        assertThat(movement.getQuantity()).isEqualTo(3);
        assertThat(movement.getType()).isEqualTo(MovementType.ENTRY);
        assertThat(movement.getNotes()).isEqualTo("Compra");
    }

    @Test
    void exitDecreasesBalance() {
        Product product = productWithStock(5);

        StockMovement movement = product.registerMovement(MovementType.EXIT, 2, null, null);

        assertThat(product.getQuantity()).isEqualTo(3);
        assertThat(movement.getPreviousQuantity()).isEqualTo(5);
        assertThat(movement.getNewQuantity()).isEqualTo(3);
    }

    @Test
    void exitCanTakeBalanceToExactlyZero() {
        Product product = productWithStock(5);

        product.registerMovement(MovementType.EXIT, 5, null, null);

        assertThat(product.getQuantity()).isZero();
        assertThat(product.getStockStatus()).isEqualTo(StockStatus.OUT_OF_STOCK);
    }

    @Test
    void exitAboveBalanceIsRejectedAndBalanceIsKept() {
        Product product = productWithStock(5);

        assertThatThrownBy(() -> product.registerMovement(MovementType.EXIT, 6, null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Estoque insuficiente")
                .hasMessageContaining("tem 5 unidade(s)");
        assertThat(product.getQuantity()).isEqualTo(5);
    }

    @Test
    void totalValueIsPriceTimesQuantity() {
        Product product = productWithStock(4);

        assertThat(product.getTotalValue()).isEqualByComparingTo("10.00");
    }

    @Test
    void newProductStartsActiveAndEmpty() {
        Product product = productWithStock(0);

        assertThat(product.isActive()).isTrue();
        assertThat(product.getQuantity()).isZero();

        product.deactivate();
        assertThat(product.isActive()).isFalse();
    }
}
