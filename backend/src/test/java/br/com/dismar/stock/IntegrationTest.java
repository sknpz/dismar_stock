package br.com.dismar.stock;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.jayway.jsonpath.JsonPath;

import br.com.dismar.stock.user.Role;
import br.com.dismar.stock.user.UserCreateRequest;
import br.com.dismar.stock.user.UserResponse;
import br.com.dismar.stock.user.UserService;

/**
 * Base dos testes de integração: sobe a aplicação inteira com um PostgreSQL do Testcontainers e
 * faz as requisições pela camada HTTP (MockMvc), passando pelo Spring Security.
 *
 * Todas as subclasses compartilham o mesmo contexto Spring e o mesmo banco. Por isso cada teste
 * cria os próprios dados com nomes únicos (ver {@link #unique}) e nunca conta com um banco vazio.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTest {

    protected static final String PASSWORD = "senha-segura-123";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserService userService;

    private String userToken;

    /** Texto único por chamada, para nomes, SKUs e e-mails não colidirem entre testes. */
    protected static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /** CNPJ numérico válido e aleatório (14 dígitos, sem pontuação). */
    protected static String randomCnpj() {
        int[] digits = new int[14];
        for (int i = 0; i < 12; i++) {
            digits[i] = ThreadLocalRandom.current().nextInt(10);
        }
        digits[12] = checkDigit(digits, new int[] {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
        digits[13] = checkDigit(digits, new int[] {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
        StringBuilder cnpj = new StringBuilder();
        for (int digit : digits) {
            cnpj.append(digit);
        }
        return cnpj.toString();
    }

    private static int checkDigit(int[] digits, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += digits[i] * weights[i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    protected UserResponse createUser(Role role) {
        String email = unique(role.name().toLowerCase()) + "@dismar.test";
        return userService.create(new UserCreateRequest("Usuário " + role, email, PASSWORD, role));
    }

    protected String tokenFor(String email) throws Exception {
        return read(mockMvc.perform(login(email, PASSWORD)).andExpect(status().isOk()), "$.accessToken");
    }

    /** Token de um usuário comum (perfil USER), criado na primeira vez que o teste precisa dele. */
    protected String userToken() throws Exception {
        if (userToken == null) {
            userToken = tokenFor(createUser(Role.USER).email());
        }
        return userToken;
    }

    protected static MockHttpServletRequestBuilder login(String email, String password) {
        return MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(email, password));
    }

    // Atalhos para requisições autenticadas como usuário comum

    protected ResultActions apiGet(String url, Object... uriVariables) throws Exception {
        return mockMvc.perform(authenticated(MockMvcRequestBuilders.get(url, uriVariables)));
    }

    protected ResultActions apiPost(String json, String url, Object... uriVariables) throws Exception {
        return mockMvc.perform(authenticated(MockMvcRequestBuilders.post(url, uriVariables)).content(json));
    }

    protected ResultActions apiPut(String json, String url, Object... uriVariables) throws Exception {
        return mockMvc.perform(authenticated(MockMvcRequestBuilders.put(url, uriVariables)).content(json));
    }

    protected ResultActions apiDelete(String url, Object... uriVariables) throws Exception {
        return mockMvc.perform(authenticated(MockMvcRequestBuilders.delete(url, uriVariables)));
    }

    private MockHttpServletRequestBuilder authenticated(MockHttpServletRequestBuilder request) throws Exception {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken())
                .contentType(MediaType.APPLICATION_JSON);
    }

    /** Lê um valor do JSON da resposta, ex.: read(result, "$.id"). */
    protected static <T> T read(ResultActions result, String jsonPath) throws UnsupportedEncodingException {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), jsonPath);
    }

    /** Cria um produto e devolve o id. */
    protected long createProduct(String name, String price, int initialQuantity, int minStock) throws Exception {
        ResultActions result = apiPost("""
                {"name": "%s", "unitPrice": %s, "initialQuantity": %d, "minStock": %d}
                """.formatted(name, price, initialQuantity, minStock), "/api/products")
                .andExpect(status().isCreated());
        return ((Number) read(result, "$.id")).longValue();
    }
}
