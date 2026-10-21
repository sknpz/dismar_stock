package br.com.dismar.stock.product;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import br.com.dismar.stock.category.Category;
import br.com.dismar.stock.movement.MovementType;
import br.com.dismar.stock.movement.StockMovement;
import br.com.dismar.stock.shared.exception.BusinessException;
import br.com.dismar.stock.supplier.Supplier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 50, unique = true)
    private String sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    // Só muda através de registerMovement, para que todo ajuste fique no histórico
    @Column(nullable = false)
    private int quantity;

    @Column(name = "min_stock", nullable = false)
    private int minStock;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Product() {
        // exigido pelo JPA
    }

    public Product(String name, String sku, Category category, Supplier supplier, BigDecimal unitPrice, int minStock) {
        update(name, sku, category, supplier, unitPrice, minStock);
    }

    public void update(String name, String sku, Category category, Supplier supplier, BigDecimal unitPrice, int minStock) {
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.supplier = supplier;
        this.unitPrice = unitPrice;
        this.minStock = minStock;
    }

    /** Aplica uma entrada ou saída ao saldo e devolve a movimentação correspondente para ser salva. */
    public StockMovement registerMovement(MovementType type, int amount, String notes) {
        int previousQuantity = quantity;
        if (type == MovementType.EXIT && amount > quantity) {
            throw new BusinessException("Estoque insuficiente: '" + name + "' tem " + quantity
                    + " unidade(s) e a saída solicitada é de " + amount + ".");
        }
        quantity = type == MovementType.ENTRY ? quantity + amount : quantity - amount;
        return new StockMovement(this, type, amount, previousQuantity, quantity, notes);
    }

    public void deactivate() {
        this.active = false;
    }

    public StockStatus getStockStatus() {
        return StockStatus.of(quantity, minStock);
    }

    public BigDecimal getTotalValue() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public Category getCategory() {
        return category;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getMinStock() {
        return minStock;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
