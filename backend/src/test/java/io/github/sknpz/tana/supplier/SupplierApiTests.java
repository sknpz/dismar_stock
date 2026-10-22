package io.github.sknpz.tana.supplier;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import io.github.sknpz.tana.IntegrationTest;

class SupplierApiTests extends IntegrationTest {

    private static String formatted(String cnpj) {
        return "%s.%s.%s/%s-%s".formatted(cnpj.substring(0, 2), cnpj.substring(2, 5), cnpj.substring(5, 8),
                cnpj.substring(8, 12), cnpj.substring(12));
    }

    private long createSupplier(String name, String cnpj) throws Exception {
        return ((Number) read(apiPost("""
                {"name": "%s", "cnpj": "%s"}
                """.formatted(name, cnpj), "/api/suppliers")
                .andExpect(status().isCreated()), "$.id")).longValue();
    }

    @Test
    void cnpjIsStoredWithoutPunctuationAndBlankFieldsBecomeNull() throws Exception {
        String cnpj = randomCnpj();

        apiPost("""
                {"name": "  Ferragens Centro  ", "cnpj": "%s", "phone": " ", "email": "", "address": "Rua A, 10"}
                """.formatted(formatted(cnpj)), "/api/suppliers")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ferragens Centro"))
                .andExpect(jsonPath("$.cnpj").value(cnpj))
                .andExpect(jsonPath("$.phone").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.activeProducts").value(0));
    }

    @Test
    void alphanumericCnpjIsAccepted() throws Exception {
        // Exemplo oficial da Receita Federal (o banco do Testcontainers começa vazio a cada execução)
        apiPost("""
                {"name": "Fornecedor alfanumérico", "cnpj": "12.abc.345/01de-35"}
                """, "/api/suppliers")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cnpj").value("12ABC34501DE35"));
    }

    @Test
    void invalidCnpjAndEmailAreFieldErrors() throws Exception {
        apiPost("""
                {"name": "Inválido", "cnpj": "11.222.333/0001-82", "email": "não-é-email"}
                """, "/api/suppliers")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.cnpj").value("CNPJ inválido."))
                .andExpect(jsonPath("$.errors.email").value("E-mail inválido."));
    }

    @Test
    void sameCnpjWithDifferentPunctuationIsADuplicate() throws Exception {
        String cnpj = randomCnpj();
        createSupplier(unique("Primeiro"), cnpj);

        apiPost("""
                {"name": "Segundo", "cnpj": "%s"}
                """.formatted(formatted(cnpj)), "/api/suppliers")
                .andExpect(status().isConflict());
    }

    @Test
    void supplierWithProductsCannotBeDeletedEvenAfterProductIsDeleted() throws Exception {
        long supplierId = createSupplier(unique("Com produtos"), randomCnpj());
        Number productId = read(apiPost("""
                {"name": "%s", "unitPrice": 1, "supplierId": %d}
                """.formatted(unique("Produto"), supplierId), "/api/products")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.supplier.id").value(supplierId)), "$.id");

        apiGet("/api/suppliers/{id}", supplierId).andExpect(jsonPath("$.activeProducts").value(1));
        apiDelete("/api/suppliers/{id}", supplierId).andExpect(status().isConflict());

        // Produto excluído continua ligado ao fornecedor no histórico
        apiDelete("/api/products/{id}", productId).andExpect(status().isNoContent());
        apiGet("/api/suppliers/{id}", supplierId).andExpect(jsonPath("$.activeProducts").value(0));
        apiDelete("/api/suppliers/{id}", supplierId).andExpect(status().isConflict());
    }

    @Test
    void supplierWithoutProductsIsDeleted() throws Exception {
        long id = createSupplier(unique("Sem produtos"), randomCnpj());

        apiDelete("/api/suppliers/{id}", id).andExpect(status().isNoContent());
        apiGet("/api/suppliers/{id}", id).andExpect(status().isNotFound());
    }

    @Test
    void searchFindsByNameCnpjOrEmail() throws Exception {
        String name = unique("Busca");
        String cnpj = randomCnpj();
        String email = unique("contato") + "@fornecedor.test";
        apiPost("""
                {"name": "%s", "cnpj": "%s", "email": "%s"}
                """.formatted(name, cnpj, email), "/api/suppliers").andExpect(status().isCreated());

        apiGet("/api/suppliers?search={s}", name.toUpperCase()).andExpect(jsonPath("$[*].name", hasItem(name)));
        apiGet("/api/suppliers?search={s}", cnpj.substring(0, 10)).andExpect(jsonPath("$[*].name", hasItem(name)));
        apiGet("/api/suppliers?search={s}", email).andExpect(jsonPath("$[*].name", hasItem(name)));
    }

    @Test
    void updateKeepsCnpjUniqueness() throws Exception {
        String takenCnpj = randomCnpj();
        createSupplier(unique("Dono do CNPJ"), takenCnpj);
        long id = createSupplier(unique("Outro"), randomCnpj());

        apiPut("""
                {"name": "Outro", "cnpj": "%s"}
                """.formatted(takenCnpj), "/api/suppliers/{id}", id)
                .andExpect(status().isConflict());
        apiPut("""
                {"name": "Outro renomeado", "cnpj": ""}
                """, "/api/suppliers/{id}", id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Outro renomeado"))
                .andExpect(jsonPath("$.cnpj").doesNotExist());
    }
}
