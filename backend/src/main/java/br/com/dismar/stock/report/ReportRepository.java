package br.com.dismar.stock.report;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import br.com.dismar.stock.product.Product;

/** Consultas de agregação em SQL nativo: o banco soma e agrupa, em vez de carregar tudo na memória. */
public interface ReportRepository extends Repository<Product, Long> {

    @Query(value = """
            SELECT count(*)                                                        AS "totalProducts",
                   coalesce(sum(quantity), 0)                                      AS "totalQuantity",
                   coalesce(sum(quantity * unit_price), 0)                         AS "totalValue",
                   count(*) FILTER (WHERE quantity > 0 AND quantity <= min_stock)  AS "lowStockProducts",
                   count(*) FILTER (WHERE quantity = 0)                            AS "outOfStockProducts"
            FROM product
            WHERE active
            """, nativeQuery = true)
    InventoryTotals inventoryTotals();

    @Query(value = """
            SELECT coalesce(c.name, 'Sem categoria')  AS "category",
                   sum(p.quantity)                    AS "quantity",
                   sum(p.quantity * p.unit_price)     AS "value"
            FROM product p
            LEFT JOIN category c ON c.id = p.category_id
            WHERE p.active
            GROUP BY c.name
            ORDER BY "value" DESC
            """, nativeQuery = true)
    List<CategoryTotals> valueByCategory();

    @Query(value = "SELECT count(*) FROM category", nativeQuery = true)
    long countCategories();

    @Query(value = "SELECT count(*) FROM stock_movement WHERE created_at >= :since", nativeQuery = true)
    long countMovementsSince(@Param("since") Instant since);

    interface InventoryTotals {
        Long getTotalProducts();

        Long getTotalQuantity();

        BigDecimal getTotalValue();

        Long getLowStockProducts();

        Long getOutOfStockProducts();
    }

    interface CategoryTotals {
        String getCategory();

        Long getQuantity();

        BigDecimal getValue();
    }
}
