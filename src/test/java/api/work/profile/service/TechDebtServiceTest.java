package api.work.profile.service;

import api.work.profile.entity.TechDebt;
import api.work.profile.entity.User;
import api.work.profile.repository.TechDebtRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TechDebtServiceTest {

    @Mock
    private TechDebtRepository techDebtRepository;

    @InjectMocks
    private TechDebtService techDebtService;

    private User testUser;
    private TechDebt testTechDebt;

    @BeforeEach
    void setUp() {
        // Configurar usuário de teste
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        // Configurar débito técnico de teste
        testTechDebt = TechDebt.builder()
                .id(1L)
                .problema("Problema de teste")
                .descricao("Descrição de teste")
                .prioridade(2)
                .status(TechDebt.StatusDebito.TODO)
                .criadoPor("Test User")
                .dataCriacao(LocalDateTime.now())
                .user(testUser)
                .build();
    }

    @Test
    void save_NewTechDebt_ShouldSaveAndReturnTechDebt() {
        // Arrange
        TechDebt newTechDebt = TechDebt.builder()
                .problema("Novo problema")
                .descricao("Nova descrição")
                .prioridade(1)
                .status(TechDebt.StatusDebito.TODO)
                .user(testUser)
                .build();

        when(techDebtRepository.save(any(TechDebt.class))).thenReturn(newTechDebt);

        // Act
        TechDebt result = techDebtService.save(newTechDebt);

        // Assert
        assertNotNull(result);
        assertEquals("Novo problema", result.getProblema());
        assertEquals("Nova descrição", result.getDescricao());
        assertEquals(1, result.getPrioridade());
        verify(techDebtRepository, times(1)).save(any(TechDebt.class));
    }

    @Test
    void save_ExistingTechDebt_ShouldUpdateAndReturnTechDebt() {
        // Arrange
        TechDebt existingTechDebt = TechDebt.builder()
                .id(1L)
                .problema("Problema atualizado")
                .descricao("Descrição atualizada")
                .prioridade(3)
                .status(TechDebt.StatusDebito.IN_PROGRESS)
                .user(testUser)
                .build();

        when(techDebtRepository.findById(1L)).thenReturn(Optional.of(testTechDebt));
        when(techDebtRepository.save(any(TechDebt.class))).thenReturn(existingTechDebt);

        // Act
        TechDebt result = techDebtService.save(existingTechDebt);

        // Assert
        assertNotNull(result);
        assertEquals("Problema atualizado", result.getProblema());
        assertEquals("Descrição atualizada", result.getDescricao());
        assertEquals(3, result.getPrioridade());
        assertEquals(TechDebt.StatusDebito.IN_PROGRESS, result.getStatus());
        verify(techDebtRepository, times(1)).findById(1L);
        verify(techDebtRepository, times(1)).save(any(TechDebt.class));
    }

    @Test
    void findById_ExistingId_ShouldReturnTechDebt() {
        // Arrange
        when(techDebtRepository.findById(1L)).thenReturn(Optional.of(testTechDebt));

        // Act
        TechDebt result = techDebtService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Problema de teste", result.getProblema());
        verify(techDebtRepository, times(1)).findById(1L);
    }

    @Test
    void findById_NonExistingId_ShouldReturnNull() {
        // Arrange
        when(techDebtRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        TechDebt result = techDebtService.findById(99L);

        // Assert
        assertNull(result);
        verify(techDebtRepository, times(1)).findById(99L);
    }

    @Test
    void findByUser_ShouldReturnPageOfTechDebts() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<TechDebt> techDebtList = List.of(testTechDebt);
        Page<TechDebt> techDebtPage = new PageImpl<>(techDebtList, pageable, 1);

        when(techDebtRepository.findByUserOrderByDataCriacaoDesc(eq(testUser), eq(pageable)))
                .thenReturn(techDebtPage);

        // Act
        Page<TechDebt> result = techDebtService.findByUser(testUser, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testTechDebt, result.getContent().get(0));
        verify(techDebtRepository, times(1)).findByUserOrderByDataCriacaoDesc(eq(testUser), eq(pageable));
    }

    @Test
    void delete_ExistingId_ShouldDeleteTechDebt() {
        // Arrange
        when(techDebtRepository.findById(1L)).thenReturn(Optional.of(testTechDebt));
        doNothing().when(techDebtRepository).deleteById(1L);

        // Act
        techDebtService.delete(1L);

        // Assert
        verify(techDebtRepository, times(1)).findById(1L);
        verify(techDebtRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_NonExistingId_ShouldNotDeleteAnything() {
        // Arrange
        when(techDebtRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        techDebtService.delete(99L);

        // Assert
        verify(techDebtRepository, times(1)).findById(99L);
        verify(techDebtRepository, never()).deleteById(anyLong());
    }

    @Test
    void getDashboardMetrics_ShouldReturnMetricsMap() {
        // Arrange
        when(techDebtRepository.countByUser(testUser)).thenReturn(10L);
        when(techDebtRepository.countByUserAndStatus(testUser, TechDebt.StatusDebito.TODO)).thenReturn(5L);
        when(techDebtRepository.countByUserAndStatus(testUser, TechDebt.StatusDebito.IN_PROGRESS)).thenReturn(3L);
        when(techDebtRepository.countByUserAndStatus(testUser, TechDebt.StatusDebito.DONE)).thenReturn(2L);
        when(techDebtRepository.countByUserAndPrioridade(eq(testUser), anyInt())).thenReturn(2L);

        // Act
        Map<String, Object> result = techDebtService.getDashboardMetrics(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.get("total"));
        assertEquals(5L, result.get("todo"));
        assertEquals(3L, result.get("inProgress"));
        assertEquals(2L, result.get("done"));
        assertNotNull(result.get("tempoMedioPorPrioridade"));
        
        @SuppressWarnings("unchecked")
        Map<Integer, Long> prioridadeMap = (Map<Integer, Long>) result.get("tempoMedioPorPrioridade");
        assertEquals(4, prioridadeMap.size());
        assertEquals(2L, prioridadeMap.get(1));
        
        verify(techDebtRepository, times(1)).countByUser(testUser);
        verify(techDebtRepository, times(1)).countByUserAndStatus(testUser, TechDebt.StatusDebito.TODO);
        verify(techDebtRepository, times(1)).countByUserAndStatus(testUser, TechDebt.StatusDebito.IN_PROGRESS);
        verify(techDebtRepository, times(1)).countByUserAndStatus(testUser, TechDebt.StatusDebito.DONE);
        verify(techDebtRepository, times(4)).countByUserAndPrioridade(eq(testUser), anyInt());
    }
}