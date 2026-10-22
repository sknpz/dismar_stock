package br.com.dismar.stock.supplier;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findAllByOrderByNameAsc();

    @Query("""
            select s from Supplier s
            where lower(s.name) like lower(concat('%', :search, '%'))
               or s.cnpj like upper(concat('%', :search, '%'))
               or lower(s.email) like lower(concat('%', :search, '%'))
            order by s.name
            """)
    List<Supplier> search(@Param("search") String search);

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, Long id);
}
