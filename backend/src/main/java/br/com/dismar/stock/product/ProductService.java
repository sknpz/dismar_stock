package br.com.dismar.stock.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.dismar.stock.category.Category;
import br.com.dismar.stock.category.CategoryService;
import br.com.dismar.stock.movement.MovementType;
import br.com.dismar.stock.movement.StockMovementRepository;
import br.com.dismar.stock.shared.exception.ConflictException;
import br.com.dismar.stock.shared.exception.ResourceNotFoundException;
import br.com.dismar.stock.supplier.Supplier;
import br.com.dismar.stock.supplier.SupplierService;

@Service
public class ProductService {

    private static final int DEFAULT_MIN_STOCK = 10;

    private final ProductRepository productRepository;
    private final StockMovementRepository movementRepository;
    private final CategoryService categoryService;
    private final SupplierService supplierService;

    public ProductService(ProductRepository productRepository, StockMovementRepository movementRepository,
            CategoryService categoryService, SupplierService supplierService) {
        this.productRepository = productRepository;
        this.movementRepository = movementRepository;
        this.categoryService = categoryService;
        this.supplierService = supplierService;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> list(String search, StockStatus status, Long categoryId, Long supplierId,
            Pageable pageable) {
        return productRepository
                .findAll(ProductSpecifications.matching(search, status, categoryId, supplierId), pageable)
                .map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductResponse.from(findActive(id));
    }

    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        String sku = normalizeSku(request.sku());
        if (sku != null && productRepository.existsBySku(sku)) {
            throw new ConflictException("Já existe um produto com o SKU " + sku + ".");
        }

        Product product = new Product(request.name().trim(), sku, resolveCategory(request.categoryId()),
                resolveSupplier(request.supplierId()), request.unitPrice(),
                request.minStock() != null ? request.minStock() : DEFAULT_MIN_STOCK);
        productRepository.save(product);

        int initialQuantity = request.initialQuantity() != null ? request.initialQuantity() : 0;
        if (initialQuantity > 0) {
            movementRepository.save(product.registerMovement(MovementType.ENTRY, initialQuantity, "Estoque inicial"));
        }
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = findActive(id);
        String sku = normalizeSku(request.sku());
        if (sku != null && productRepository.existsBySkuAndIdNot(sku, id)) {
            throw new ConflictException("Já existe um produto com o SKU " + sku + ".");
        }
        product.update(request.name().trim(), sku, resolveCategory(request.categoryId()),
                resolveSupplier(request.supplierId()), request.unitPrice(), request.minStock());
        return ProductResponse.from(product);
    }

    /** Exclusão lógica: o produto sai das listagens, mas o histórico de movimentações é preservado. */
    @Transactional
    public void delete(Long id) {
        findActive(id).deactivate();
    }

    private Product findActive(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", id));
    }

    private Category resolveCategory(Long categoryId) {
        return categoryId == null ? null : categoryService.findById(categoryId);
    }

    private Supplier resolveSupplier(Long supplierId) {
        return supplierId == null ? null : supplierService.findById(supplierId);
    }

    private static String normalizeSku(String sku) {
        return sku == null || sku.isBlank() ? null : sku.trim().toUpperCase();
    }
}
