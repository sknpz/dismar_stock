package br.com.dismar.stock.user;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import br.com.dismar.stock.IntegrationTest;

/** Gestão de usuários pela API, como administrador. */
class UserApiTests extends IntegrationTest {

    private String adminToken;

    @BeforeEach
    void loginAsAdmin() throws Exception {
        adminToken = tokenFor(createUser(Role.ADMIN).email());
    }

    private ResultActions asAdmin(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void adminListsAndReadsUsersWithoutPasswordHashes() throws Exception {
        UserResponse user = createUser(Role.USER);

        asAdmin(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", hasItem(user.email())))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
        asAdmin(get("/api/users/{id}", user.id()))
                .andExpect(jsonPath("$.name").value(user.name()))
                .andExpect(jsonPath("$.active").value(true));
        asAdmin(get("/api/users/{id}", 999999)).andExpect(status().isNotFound());
    }

    @Test
    void newUserNeedsValidEmailAndPasswordOfAtLeastEightCharacters() throws Exception {
        asAdmin(post("/api/users").content("""
                {"name": "Fraco", "email": "sem-arroba", "password": "1234567", "role": "USER"}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("E-mail inválido."))
                .andExpect(jsonPath("$.errors.password").value("A senha deve ter entre 8 e 72 caracteres."));
    }

    @Test
    void emailIsUniqueIgnoringCase() throws Exception {
        UserResponse existing = createUser(Role.USER);

        asAdmin(post("/api/users").content("""
                {"name": "Repetido", "email": "%s", "password": "%s", "role": "USER"}
                """.formatted(existing.email().toUpperCase(), PASSWORD)))
                .andExpect(status().isConflict());
    }

    @Test
    void adminPromotesUserAndResetsPassword() throws Exception {
        UserResponse user = createUser(Role.USER);

        asAdmin(put("/api/users/{id}", user.id()).content("""
                {"name": "Gerente", "role": "ADMIN", "active": true, "password": "senha-redefinida-1"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gerente"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mockMvc.perform(login(user.email(), PASSWORD)).andExpect(status().isUnauthorized());
        String newToken = read(mockMvc.perform(login(user.email(), "senha-redefinida-1")).andExpect(status().isOk()),
                "$.accessToken");
        // A promoção vale na hora: o novo administrador já acessa a gestão de usuários
        mockMvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + newToken))
                .andExpect(status().isOk());
    }

    @Test
    void updateWithoutPasswordKeepsTheCurrentOne() throws Exception {
        UserResponse user = createUser(Role.USER);

        asAdmin(put("/api/users/{id}", user.id()).content("""
                {"name": "Só o nome", "role": "USER", "active": true}
                """)).andExpect(status().isOk());

        mockMvc.perform(login(user.email(), PASSWORD)).andExpect(status().isOk());
    }
}
