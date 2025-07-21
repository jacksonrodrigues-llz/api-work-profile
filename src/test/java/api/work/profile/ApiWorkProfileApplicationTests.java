package api.work.profile;

import api.work.profile.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Teste básico para verificar se o contexto da aplicação carrega corretamente.
 * Este teste não depende de banco de dados, pois usa o perfil "test" que desabilita JPA e Liquibase.
 */
@SpringBootTest(classes = {ApiWorkProfileApplication.class, TestConfig.class})
@ActiveProfiles("test")
class ApiWorkProfileApplicationTests {

    @Test
    void contextLoads() {
        // Este teste verifica apenas se o contexto da aplicação carrega corretamente
    }
}