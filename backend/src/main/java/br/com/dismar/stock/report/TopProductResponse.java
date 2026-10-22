package br.com.dismar.stock.report;

import java.math.BigDecimal;

public record TopProductResponse(Long id, String name, String category, int quantity, BigDecimal totalValue) {
}
