package br.com.dismar.stock.supplier;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.dismar.stock.product.ProductRepository;
import br.com.dismar.stock.shared.exception.ConflictException;
import br.com.dismar.stock.shared.exception.ResourceNotFoundException;
import br.com.dismar.stock.shared.validation.Cnpj;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    public SupplierService(SupplierRepository supplierRepository, ProductRepository productRepository) {
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> list(String search) {
        String term = blankToNull(search);
        Map<Long, Long> productCounts = productRepository.countActiveProductsBySupplier();
        // Sem busca, consulta separada: um parâmetro nulo no LIKE chega ao PostgreSQL sem tipo definido
        List<Supplier> suppliers = term == null
                ? supplierRepository.findAllByOrderByNameAsc()
                : supplierRepository.search(term);
        return suppliers.stream()
                .map(s -> SupplierResponse.from(s, productCounts.getOrDefault(s.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponse get(Long id) {
        Supplier supplier = findById(id);
        return SupplierResponse.from(supplier, productRepository.countBySupplierIdAndActiveTrue(id));
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        String cnpj = Cnpj.normalize(request.cnpj());
        if (cnpj != null && supplierRepository.existsByCnpj(cnpj)) {
            throw new ConflictException("Já existe um fornecedor com o CNPJ " + cnpj + ".");
        }
        Supplier supplier = new Supplier(request.name().trim(), cnpj, blankToNull(request.phone()),
                blankToNull(request.email()), blankToNull(request.address()));
        return SupplierResponse.from(supplierRepository.save(supplier), 0);
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = findById(id);
        String cnpj = Cnpj.normalize(request.cnpj());
        if (cnpj != null && supplierRepository.existsByCnpjAndIdNot(cnpj, id)) {
            throw new ConflictException("Já existe um fornecedor com o CNPJ " + cnpj + ".");
        }
        supplier.update(request.name().trim(), cnpj, blankToNull(request.phone()),
                blankToNull(request.email()), blankToNull(request.address()));
        return SupplierResponse.from(supplier, productRepository.countBySupplierIdAndActiveTrue(id));
    }

    @Transactional
    public void delete(Long id) {
        Supplier supplier = findById(id);
        // Inclui produtos inativos: eles continuam ligados ao fornecedor no histórico
        if (productRepository.existsBySupplierId(id)) {
            throw new ConflictException(
                    "O fornecedor possui produtos vinculados (inclusive excluídos, mantidos no histórico) e não pode ser excluído.");
        }
        supplierRepository.delete(supplier);
    }

    /** Usado por outros módulos para resolver o fornecedor informado em um produto. */
    @Transactional(readOnly = true)
    public Supplier findById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor", id));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
