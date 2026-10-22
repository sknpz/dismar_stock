package br.com.dismar.stock.supplier;

public record SupplierResponse(
        Long id,
        String name,
        String cnpj,
        String phone,
        String email,
        String address,
        long activeProducts) {

    static SupplierResponse from(Supplier supplier, long activeProducts) {
        return new SupplierResponse(supplier.getId(), supplier.getName(), supplier.getCnpj(),
                supplier.getPhone(), supplier.getEmail(), supplier.getAddress(), activeProducts);
    }
}
