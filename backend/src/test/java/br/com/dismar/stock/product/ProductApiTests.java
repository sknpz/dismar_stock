package br.com.dismar.stock.product;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import br.com.dismar.stock.IntegrationTest;

class ProductApiTests extends IntegrationTest {

    @Test
    void createRecordsInitialQuantityAsEntryMovement() throws Exception {
        String sku = unique("sku");

        Number id = read(apiPost("""
                {"name": "Martelo", "sku": "%s", "unitPrice": 39.90, "initialQuantity": 12, "minStock": 4}
                """.formatted(sku.toLowerCase()), "/api/products")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/products/")))
                .andExpect(jsonPath("$.sku").value(sku.toUpperCase()))
                .andExpect(jsonPath("$.quantity").value(12))
                .andExpect(jsonPath("$.totalValue").value(478.80))
                .andExpect(jsonPath("$.stockStatus").value("OK")), "$.id");

        apiGet("/api/movements?productId={id}", id)
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].type").value("ENTRY"))
                .andExpect(jsonPath("$.content[0].quantity").value(12))
                .andExpect(jsonPath("$.content[0].notes").value("Estoque inicial"));
    }

    @Test
    void createWithoutInitialQuantityStartsEmptyWithDefaultMinimum() throws Exception {
        Number id = read(apiPost("""
                {"name": "Trena 5m", "unitPrice": 25}
                """, "/api/products")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(0))
                .andExpect(jsonPath("$.minStock").value(10))
                .andExpect(jsonPath("$.stockStatus").value("OUT_OF_STOCK")), "$.id");

        apiGet("/api/movements?productId={id}", id).andExpect(jsonPath("$.page.totalElements").value(0));
    }

    @Test
    void invalidPayloadReturnsOneMessagePerField() throws Exception {
        apiPost("""
                {"name": " ", "unitPrice": -1, "minStock": -2}
                """, "/api/products")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.errors.name").value("O nome é obrigatório."))
                .andExpect(jsonPath("$.errors.unitPrice").value("O preço unitário não pode ser negativo."))
                .andExpect(jsonPath("$.errors.minStock").value("O estoque mínimo não pode ser negativo."));
    }

    @Test
    void duplicateSkuIsRejectedIgnoringCase() throws Exception {
        String sku = unique("dup").toUpperCase();
        apiPost("""
                {"name": "Original", "sku": "%s", "unitPrice": 1}
                """.formatted(sku), "/api/products").andExpect(status().isCreated());

        apiPost("""
                {"name": "Cópia", "sku": " %s ", "unitPrice": 1}
                """.formatted(sku.toLowerCase()), "/api/products")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString(sku)));
    }

    @Test
    void updateChangesDataButNeverTheQuantity() throws Exception {
        long id = createProduct(unique("Serra"), "50.00", 7, 2);

        // "quantity" no corpo é ignorado: o saldo só muda por movimentação
        apiPut("""
                {"name": "Serra tico-tico", "unitPrice": 55.50, "minStock": 3, "quantity": 999}
                """, "/api/products/{id}", id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Serra tico-tico"))
                .andExpect(jsonPath("$.unitPrice").value(55.50))
                .andExpect(jsonPath("$.minStock").value(3))
                .andExpect(jsonPath("$.quantity").value(7));
    }

    @Test
    void deleteHidesProductButKeepsItsHistory() throws Exception {
        long id = createProduct(unique("Alicate"), "30", 4, 1);

        apiDelete("/api/products/{id}", id).andExpect(status().isNoContent());

        apiGet("/api/products/{id}", id)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Produto com id " + id + " não existe."));
        apiGet("/api/movements?productId={id}", id).andExpect(jsonPath("$.page.totalElements").value(1));
        apiPost("""
                {"type": "ENTRY", "quantity": 1}
                """, "/api/products/{id}/movements", id).andExpect(status().isNotFound());
    }

    @Test
    void listFiltersBySearchAndStockStatus() throws Exception {
        String prefix = unique("Filtro");
        createProduct(prefix + " ok", "1", 20, 5);
        createProduct(prefix + " baixo", "1", 3, 5);
        createProduct(prefix + " zerado", "1", 0, 5);

        apiGet("/api/products?search={search}", prefix.toLowerCase())
                .andExpect(jsonPath("$.page.totalElements").value(3));
        apiGet("/api/products?search={search}&status=LOW_STOCK", prefix)
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value(prefix + " baixo"));
        apiGet("/api/products?search={search}&status=OUT_OF_STOCK", prefix)
                .andExpect(jsonPath("$.content[0].name").value(prefix + " zerado"));
        apiGet("/api/products?search={search}&status=OK", prefix)
                .andExpect(jsonPath("$.content[0].name").value(prefix + " ok"));
    }

    @Test
    void listIsPaginatedAndSortable() throws Exception {
        String prefix = unique("Pagina");
        createProduct(prefix + " A", "1", 0, 0);
        createProduct(prefix + " B", "1", 0, 0);
        createProduct(prefix + " C", "1", 0, 0);

        apiGet("/api/products?search={search}&size=2&sort=name,desc", prefix)
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value(prefix + " C"));
        apiGet("/api/products?search={search}&size=2&page=1&sort=name,desc", prefix)
                .andExpect(jsonPath("$.content[0].name").value(prefix + " A"));
    }

    @Test
    void sortingByUnknownFieldIsABadRequest() throws Exception {
        apiGet("/api/products?sort=inexistente")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Não é possível ordenar por 'inexistente'."));
    }

    @Test
    void unknownCategoryOrSupplierIsNotFound() throws Exception {
        apiPost("""
                {"name": "Sem categoria", "unitPrice": 1, "categoryId": 999999}
                """, "/api/products")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Categoria com id 999999 não existe."));
        apiPost("""
                {"name": "Sem fornecedor", "unitPrice": 1, "supplierId": 999999}
                """, "/api/products")
                .andExpect(status().isNotFound());
    }
}
