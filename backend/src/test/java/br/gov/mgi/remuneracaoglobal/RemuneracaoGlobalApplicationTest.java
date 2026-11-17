package br.gov.mgi.remuneracaoglobal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:rmg_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.locations=classpath:db/migration,classpath:db/dev"
})
class RemuneracaoGlobalApplicationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void contextoSobeComMigracoesEDadosDeExemplo() {
        Integer empresas = jdbc.queryForObject("select count(*) from empresa", Integer.class);
        assertThat(empresas).isEqualTo(2);
    }
}
