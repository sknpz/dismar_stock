package br.com.dismar.stock.product;

import org.springframework.data.jpa.domain.Specification;

/** Filtros opcionais da listagem de produtos, combinados conforme os parâmetros recebidos. */
final class ProductSpecifications {

    private ProductSpecifications() {
    }

    static Specification<Product> matching(String search, StockStatus status, Long categoryId, Long supplierId) {
        Specification<Product> spec = (root, query, cb) -> cb.isTrue(root.get("active"));

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("sku")), pattern)));
        }
        if (status != null) {
            spec = spec.and(hasStatus(status));
        }
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }
        if (supplierId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("supplier").get("id"), supplierId));
        }
        return spec;
    }

    // Mesma regra de StockStatus.of, traduzida para SQL
    private static Specification<Product> hasStatus(StockStatus status) {
        return (root, query, cb) -> switch (status) {
            case OUT_OF_STOCK -> cb.equal(root.get("quantity"), 0);
            case LOW_STOCK -> cb.and(
                    cb.greaterThan(root.get("quantity"), 0),
                    cb.lessThanOrEqualTo(root.<Integer>get("quantity"), root.get("minStock")));
            case OK -> cb.greaterThan(root.<Integer>get("quantity"), root.get("minStock"));
        };
    }
}
