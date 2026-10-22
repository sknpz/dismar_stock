package br.com.dismar.stock.auth;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.jayway.jsonpath.JsonPath;

import br.com.dismar.stock.TestcontainersConfiguration;
import br.com.dismar.stock.user.Role;
import br.com.dismar.stock.user.UserCreateRequest;
import br.com.dismar.stock.user.UserResponse;
import br.com.dismar.stock.user.UserService;
import br.com.dismar.stock.user.UserUpdateRequest;

/** Fluxo completo pela camada HTTP: login, token, perfis e respostas 401/403 no formato Problem Details. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationTests {

    private static final String PASSWORD = "senha-segura-123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    void requestWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.title").value("Não autenticado"));
    }

    @Test
    void invalidTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/products").header(HttpHeaders.AUTHORIZATION, "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail", containsString("sessão expirou")));
    }

    @Test
    void loginWithWrongPasswordIsRejected() throws Exception {
        UserResponse user = createUser(Role.USER);

        mockMvc.perform(login(user.email(), "senha-errada"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos."));
    }

    @Test
    void loginWithUnknownEmailGivesTheSameAnswer() throws Exception {
        mockMvc.perform(login("ninguem-" + UUID.randomUUID() + "@dismar.test", PASSWORD))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos."));
    }

    @Test
    void tokenFromLoginAuthorizesApiCalls() throws Exception {
        UserResponse user = createUser(Role.USER);
        String token = tokenFor(user.email());

        mockMvc.perform(get("/api/products").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.email()))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void emailIsCaseInsensitiveOnLogin() throws Exception {
        UserResponse user = createUser(Role.USER);

        mockMvc.perform(login(user.email().toUpperCase(), PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void regularUserCannotManageUsers() throws Exception {
        UserResponse user = createUser(Role.USER);

        mockMvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(user.email())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Acesso negado"));
    }

    @Test
    void adminCreatesUserWhoCanThenLogIn() throws Exception {
        UserResponse admin = createUser(Role.ADMIN);
        String newEmail = "novo-" + UUID.randomUUID() + "@dismar.test";

        mockMvc.perform(post("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFor(admin.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Novo Usuário", "email": "%s", "password": "%s", "role": "USER"}
                                """.formatted(newEmail, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mockMvc.perform(login(newEmail, PASSWORD)).andExpect(status().isOk());
    }

    @Test
    void deactivatingUserRevokesAccessImmediately() throws Exception {
        UserResponse admin = createUser(Role.ADMIN);
        UserResponse user = createUser(Role.USER);
        String userToken = tokenFor(user.email());

        userService.update(user.id(), new UserUpdateRequest(user.name(), Role.USER, false, null), admin.id());

        // O token ainda não expirou, mas a API confere a situação do usuário a cada requisição
        mockMvc.perform(get("/api/products").header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(login(user.email(), PASSWORD))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail", containsString("desativado")));
    }

    @Test
    void adminCannotDeactivateOrDemoteThemselves() throws Exception {
        UserResponse admin = createUser(Role.ADMIN);
        String token = tokenFor(admin.email());

        mockMvc.perform(put("/api/users/{id}", admin.id())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Admin", "role": "USER", "active": true}
                                """))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void movementRecordsWhoRegisteredIt() throws Exception {
        UserResponse user = createUser(Role.USER);
        String token = tokenFor(user.email());

        String product = mockMvc.perform(post("/api/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Produto com autor", "unitPrice": 5.00, "initialQuantity": 2}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer productId = JsonPath.read(product, "$.id");

        mockMvc.perform(post("/api/products/{id}/movements", productId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "EXIT", "quantity": 1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.name").value(user.name()));

        // A entrada "Estoque inicial" também fica com o autor
        mockMvc.perform(get("/api/movements").param("productId", productId.toString())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(jsonPath("$.content[*].user.name").value(everyItem(equalTo(user.name()))));
    }

    @Test
    void changingPasswordRequiresTheCurrentOne() throws Exception {
        UserResponse user = createUser(Role.USER);
        String token = tokenFor(user.email());

        mockMvc.perform(put("/api/auth/password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "senha-errada", "newPassword": "nova-senha-456"}
                                """))
                .andExpect(status().isUnprocessableContent());

        mockMvc.perform(put("/api/auth/password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "%s", "newPassword": "nova-senha-456"}
                                """.formatted(PASSWORD)))
                .andExpect(status().isNoContent());

        mockMvc.perform(login(user.email(), "nova-senha-456")).andExpect(status().isOk());
    }

    private UserResponse createUser(Role role) {
        String email = role.name().toLowerCase() + "-" + UUID.randomUUID() + "@dismar.test";
        return userService.create(new UserCreateRequest("Usuário " + role, email, PASSWORD, role));
    }

    private String tokenFor(String email) throws Exception {
        String response = mockMvc.perform(login(email, PASSWORD))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.accessToken");
    }

    private static MockHttpServletRequestBuilder login(String email, String password) {
        return post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(email, password));
    }
}
