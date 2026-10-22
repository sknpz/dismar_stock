package io.github.sknpz.tana.report;

import java.math.BigDecimal;

public record TopProductResponse(Long id, String name, String category, int quantity, BigDecimal totalValue) {
}
