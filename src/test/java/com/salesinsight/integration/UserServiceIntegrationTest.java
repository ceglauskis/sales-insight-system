package com.salesinsight.integration;

import com.salesinsight.user.dto.LoginRequest;
import com.salesinsight.user.dto.LoginResponse;
import com.salesinsight.user.dto.RegisterRequest;
import com.salesinsight.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("salesinsight_test")
            .withUsername("salesinsight")
            .withPassword("salesinsight");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", () -> "localhost");
    }

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Deve registrar um novo usuário e retornar token")
    void shouldRegisterUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest(
                "Maikon Teste",
                "maikon_test@email.com",
                "12345678"
        );

        LoginResponse response = userService.register(request);

        assertNotNull(response);
        assertNotNull(response.token());
        assertFalse(response.token().isEmpty());
    }

    @Test
    @DisplayName("Não deve registrar usuário com email duplicado")
    void shouldNotRegisterDuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                "Maikon Teste 2",
                "maikon_duplicate@email.com",
                "12345678"
        );

        userService.register(request);

        assertThrows(IllegalArgumentException.class, () -> userService.register(request));
    }

    @Test
    @DisplayName("Deve fazer login com credenciais válidas")
    void shouldLoginWithValidCredentials() {
        RegisterRequest registerRequest = new RegisterRequest(
                "Maikon Login",
                "maikon_login@email.com",
                "12345678"
        );
        userService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest("maikon_login@email.com", "12345678");
        LoginResponse response = userService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.token());
    }

    @Test
    @DisplayName("Não deve fazer login com senha errada")
    void shouldNotLoginWithWrongPassword() {
        RegisterRequest registerRequest = new RegisterRequest(
                "Maikon Wrong",
                "maikon_wrong@email.com",
                "12345678"
        );
        userService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest("maikon_wrong@email.com", "senha_errada");

        assertThrows(IllegalArgumentException.class, () -> userService.login(loginRequest));
    }
}