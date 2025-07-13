package api.work.profile.repository;

import api.work.profile.entity.TechDebt;
import api.work.profile.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TechDebtRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TechDebtRepository techDebtRepository;

    @Test
    void testFindAllWithFilters() {
        // Given
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        entityManager.persistAndFlush(user);

        TechDebt techDebt = new TechDebt();
        techDebt.setProblema("Test Problem");
        techDebt.setDescricao("Test Description");
        techDebt.setPrioridade(1);
        techDebt.setStatus(TechDebt.StatusDebito.TODO);
        techDebt.setUser(user);
        entityManager.persistAndFlush(techDebt);

        // When
        Page<TechDebt> result = techDebtRepository.findAll(
            api.work.profile.util.TechDebtQueryBuilder.buildSpecification(
                TechDebt.StatusDebito.TODO, 1, null, "Test", null, null
            ), PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProblema()).isEqualTo("Test Problem");
    }
}