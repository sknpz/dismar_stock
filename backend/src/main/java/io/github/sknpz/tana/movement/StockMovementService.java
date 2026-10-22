package io.github.sknpz.tana.movement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.sknpz.tana.product.Product;
import io.github.sknpz.tana.product.ProductRepository;
import io.github.sknpz.tana.shared.exception.ResourceNotFoundException;
import io.github.sknpz.tana.user.CurrentUser;

@Service
public class StockMovementService {

    private final StockMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final CurrentUser currentUser;

    public StockMovementService(StockMovementRepository movementRepository, ProductRepository productRepository,
            CurrentUser currentUser) {
        this.movementRepository = movementRepository;
        this.productRepository = productRepository;
        this.currentUser = currentUser;
    }

    @Transactional
    public StockMovementResponse register(Long productId, StockMovementRequest request) {
        Product product = productRepository.findActiveByIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", productId));

        String notes = request.notes() == null || request.notes().isBlank() ? null : request.notes().trim();
        StockMovement movement = product.registerMovement(request.type(), request.quantity(), notes,
                currentUser.reference().orElse(null));
        return StockMovementResponse.from(movementRepository.save(movement));
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> list(MovementType type, Long productId, Pageable pageable) {
        Specification<StockMovement> spec = Specification.unrestricted();
        if (type != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type));
        }
        if (productId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("product").get("id"), productId));
        }
        return movementRepository.findAll(spec, pageable).map(StockMovementResponse::from);
    }
}
