package io.github.sknpz.tana.movement;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import io.github.sknpz.tana.product.Product;
import io.github.sknpz.tana.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Registro imutável de uma entrada ou saída. Criado apenas por Product.registerMovement. */
@Entity
@Table(name = "stock_movement")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, updatable = false)
    private MovementType type;

    @Column(nullable = false, updatable = false)
    private int quantity;

    @Column(name = "previous_quantity", nullable = false, updatable = false)
    private int previousQuantity;

    @Column(name = "new_quantity", nullable = false, updatable = false)
    private int newQuantity;

    @Column(length = 255, updatable = false)
    private String notes;

    // Quem registrou a movimentação; nulo nas anteriores à autenticação
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", updatable = false)
    private AppUser user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected StockMovement() {
        // exigido pelo JPA
    }

    public StockMovement(Product product, MovementType type, int quantity, int previousQuantity, int newQuantity,
            String notes, AppUser user) {
        this.product = product;
        this.type = type;
        this.quantity = quantity;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.notes = notes;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public MovementType getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPreviousQuantity() {
        return previousQuantity;
    }

    public int getNewQuantity() {
        return newQuantity;
    }

    public String getNotes() {
        return notes;
    }

    public AppUser getUser() {
        return user;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
