package br.com.dismar.stock.category;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import br.com.dismar.stock.IntegrationTest;

class CategoryApiTests extends IntegrationTest {

    private long createCategory(String name) throws Exception {
        return ((Number) read(apiPost("""
                {"name": "%s"}
                """.formatted(name), "/api/categories")
                .andExpect(status().isCreated()), "$.id")).longValue();
    }

    @Test
    void createdCategoryAppearsInTheList() throws Exception {
        String name = unique("Pintura");
        createCategory(name);

        apiGet("/api/categories").andExpect(jsonPath("$[*].name", hasItem(name)));
    }

    @Test
    void namesAreUniqueIgnoringCase() throws Exception {
        String name = unique("Jardinagem");
        createCategory(name);

        apiPost("""
                {"name": "  %s  "}
                """.formatted(name.toUpperCase()), "/api/categories")
                .andExpect(status().isConflict());
    }

    @Test
    void renameCannotTakeAnotherCategorysName() throws Exception {
        String taken = unique("Elétrica");
        createCategory(taken);
        long id = createCategory(unique("Hidráulica"));

        apiPut("""
                {"name": "%s"}
                """.formatted(taken.toLowerCase()), "/api/categories/{id}", id)
                .andExpect(status().isConflict());
        apiPut("""
                {"name": "Hidráulica e Gás"}
                """, "/api/categories/{id}", id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hidráulica e Gás"));
    }

    @Test
    void deletingCategoryKeepsItsProductsWithoutCategory() throws Exception {
        long categoryId = createCategory(unique("Temporária"));
        Number productId = read(apiPost("""
                {"name": "%s", "unitPrice": 1, "categoryId": %d}
                """.formatted(unique("Produto"), categoryId), "/api/products")
                .andExpect(status().isCreated()), "$.id");

        apiDelete("/api/categories/{id}", categoryId).andExpect(status().isNoContent());

        apiGet("/api/products/{id}", productId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").doesNotExist());
    }

    @Test
    void blankNameIsRejected() throws Exception {
        apiPost("""
                {"name": "   "}
                """, "/api/categories")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("O nome é obrigatório."));
    }
}
