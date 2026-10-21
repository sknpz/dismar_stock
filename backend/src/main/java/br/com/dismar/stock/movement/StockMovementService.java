package br.com.dismar.stock.movement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.dismar.stock.product.Product;
import br.com.dismar.stock.product.ProductRepository;
import br.com.dismar.stock.shared.exception.ResourceNotFoundException;

@Service
public class StockMovementService {

    private final StockMovementRepository movementRepository;
    private final ProductRepository productRepository;

    public StockMovementService(StockMovementRepository movementRepository, ProductRepository productRepository) {
        this.movementRepository = movementRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public StockMovementResponse register(Long productId, StockMovementRequest request) {
        Product product = productRepository.findActiveByIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", productId));

        String notes = request.notes() == null || request.notes().isBlank() ? null : request.notes().trim();
        StockMovement movement = product.registerMovement(request.type(), request.quantity(), notes);
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
