package br.com.dismar.stock.movement;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import br.com.dismar.stock.IntegrationTest;

class StockMovementApiTests extends IntegrationTest {

    private static String movement(String type, int quantity, String notes) {
        return """
                {"type": "%s", "quantity": %d, "notes": %s}
                """.formatted(type, quantity, notes == null ? "null" : "\"" + notes + "\"");
    }

    @Test
    void entriesAndExitsUpdateTheBalance() throws Exception {
        long id = createProduct(unique("Cimento"), "35.00", 10, 2);

        apiPost(movement("ENTRY", 5, "Compra NF 123"), "/api/products/{id}/movements", id)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.previousQuantity").value(10))
                .andExpect(jsonPath("$.newQuantity").value(15))
                .andExpect(jsonPath("$.notes").value("Compra NF 123"))
                .andExpect(jsonPath("$.product.id").value(id));
        apiPost(movement("EXIT", 15, null), "/api/products/{id}/movements", id)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.newQuantity").value(0));

        apiGet("/api/products/{id}", id)
                .andExpect(jsonPath("$.quantity").value(0))
                .andExpect(jsonPath("$.stockStatus").value("OUT_OF_STOCK"));
    }

    @Test
    void exitAboveBalanceIsUnprocessableAndChangesNothing() throws Exception {
        long id = createProduct(unique("Areia"), "5.00", 3, 1);

        apiPost(movement("EXIT", 4, null), "/api/products/{id}/movements", id)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Regra de negócio violada"))
                .andExpect(jsonPath("$.detail", containsString("tem 3 unidade(s) e a saída solicitada é de 4")));

        apiGet("/api/products/{id}", id).andExpect(jsonPath("$.quantity").value(3));
        apiGet("/api/movements?productId={id}", id).andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void invalidMovementsAreFieldErrors() throws Exception {
        long id = createProduct(unique("Brita"), "5.00", 3, 1);

        apiPost("""
                {"quantity": 0}
                """, "/api/products/{id}/movements", id)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.type").value("O tipo é obrigatório (ENTRY ou EXIT)."))
                .andExpect(jsonPath("$.errors.quantity").value("A quantidade deve ser maior que zero."));
    }

    @Test
    void historyIsNewestFirstAndFiltersByType() throws Exception {
        long id = createProduct(unique("Tijolo"), "1.20", 100, 10);
        apiPost(movement("EXIT", 30, "Obra 1"), "/api/products/{id}/movements", id);
        apiPost(movement("EXIT", 20, "Obra 2"), "/api/products/{id}/movements", id);

        apiGet("/api/movements?productId={id}", id)
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].notes").value("Obra 2"))
                .andExpect(jsonPath("$.content[2].notes").value("Estoque inicial"));
        apiGet("/api/movements?productId={id}&type=EXIT", id)
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].type", everyItem(is("EXIT"))));
    }

    @Test
    void movementOnUnknownProductIsNotFound() throws Exception {
        apiPost(movement("ENTRY", 1, null), "/api/products/{id}/movements", 999999)
                .andExpect(status().isNotFound());
    }
}
