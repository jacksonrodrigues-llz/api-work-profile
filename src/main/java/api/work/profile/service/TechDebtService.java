package api.work.profile.service;

import api.work.profile.dto.TechDebtDTO;
import api.work.profile.entity.TechDebt;
import api.work.profile.entity.User;
import api.work.profile.repository.TechDebtRepository;
import api.work.profile.util.TechDebtQueryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TechDebtService {
    
    private final TechDebtRepository techDebtRepository;
    
    public TechDebt save(TechDebt techDebt) {
        var isNew = techDebt.getId() == null;
        var toSave = techDebt;
        
        if (!isNew) {
            var existing = techDebtRepository.findById(techDebt.getId()).orElse(null);
            if (existing != null) {
                toSave = techDebt.toBuilder()
                    .criadoPor(existing.getCriadoPor())
                    .criadoPorId(existing.getCriadoPorId())
                    .dataCriacao(existing.getDataCriacao())
                    .build();
            }
        }
        
        var saved = techDebtRepository.save(toSave);
        
        if (isNew) {
            log.info("[TECH_DEBT] Criado: {} (ID: {}, P{})", saved.getProblema(), saved.getId(), saved.getPrioridade());
        } else {
            log.info("[TECH_DEBT] Atualizado: {} (ID: {})", saved.getProblema(), saved.getId());
        }
        
        return saved;
    }
    
    public Page<TechDebt> findByUser(User user, Pageable pageable) {
        return techDebtRepository.findByUserOrderByDataCriacaoDesc(user, pageable);
    }
    
    public Page<TechDebt> findByUserWithFilters(User user, TechDebt.StatusDebito status, 
                                               Integer prioridade, TechDebt.TipoDebito tipo, 
                                               Pageable pageable) {
        return techDebtRepository.findByUserWithFilters(user, status, prioridade, tipo, pageable);
    }
    
    public TechDebt findById(Long id) {
        return techDebtRepository.findById(id).orElse(null);
    }
    
    public List<TechDebt> findAll() {
        return techDebtRepository.findAll();
    }
    
    public List<TechDebt> findAllByUser(User user) {
        return techDebtRepository.findByUserOrderByDataCriacaoDesc(user, PageRequest.of(0, 100)).getContent();
    }
    
    public void delete(Long id) {
        TechDebt debt = techDebtRepository.findById(id).orElse(null);
        if (debt != null) {
            log.info("[TECH_DEBT] Excluído: {} (ID: {})", debt.getProblema(), id);
            techDebtRepository.deleteById(id);
        } else {
            log.warn("[TECH_DEBT] Tentativa de excluir inexistente: ID {}", id);
        }
    }
    
    public List<TechDebt> importFromJson(List<TechDebtDTO> dtos, User user) {
        var debts = new ArrayList<TechDebt>();
        for (var dto : dtos) {
            var debt = dto.toEntity().toBuilder()
                .user(user)
                .build();
            debts.add(save(debt));
        }
        return debts;
    }
    
    public List<TechDebt> importFromCsv(MultipartFile file, User user) throws Exception {
        List<TechDebt> debts = new ArrayList<>();
        String filename = file.getOriginalFilename();
        
        if (filename != null && (filename.endsWith(".xlsx") || filename.endsWith(".xls"))) {
            return importFromExcel(file, user);
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] fields = line.split(",");
                if (fields.length >= 4) {
                    var debt = TechDebt.builder()
                        .problema(fields[0].trim().replace("\"", ""))
                        .descricao(fields[1].trim().replace("\"", ""))
                        .prioridade(Integer.parseInt(fields[2].trim()))
                        .criadoPor(fields[3].trim().replace("\"", ""))
                        .user(user)
                        .build();
                    debts.add(save(debt));
                }
            }
        }
        return debts;
    }
    
    private List<TechDebt> importFromExcel(MultipartFile file, User user) throws Exception {
        List<TechDebt> debts = new ArrayList<>();
        
        try (Workbook workbook = file.getOriginalFilename().endsWith(".xlsx") ? 
                new XSSFWorkbook(file.getInputStream()) : 
                new HSSFWorkbook(file.getInputStream())) {
            
            Sheet sheet = workbook.getSheetAt(0);
            boolean isHeader = true;
            
            for (Row row : sheet) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                if (row.getPhysicalNumberOfCells() >= 6) {
                    var builder = TechDebt.builder()
                        .problema(getCellValue(row.getCell(0)))
                        .descricao(getCellValue(row.getCell(1)))
                        .prioridade((int) row.getCell(2).getNumericCellValue())
                        .criadoPor(getCellValue(row.getCell(5)))
                        .user(user);
                    
                    // tipos (coluna 3)
                    var tiposStr = getCellValue(row.getCell(3));
                    if (tiposStr != null && !tiposStr.isEmpty()) {
                        var tipos = new ArrayList<TechDebt.TipoDebito>();
                        for (var tipo : tiposStr.split(",")) {
                            try {
                                tipos.add(TechDebt.TipoDebito.valueOf(tipo.trim().toUpperCase()));
                            } catch (Exception e) {
                                // Ignorar tipos inválidos
                            }
                        }
                        builder.tipos(tipos);
                    }
                    
                    // tags (coluna 4)
                    var tagsStr = getCellValue(row.getCell(4));
                    if (tagsStr != null && !tagsStr.isEmpty()) {
                        var tags = List.of(tagsStr.split(","));
                        builder.tags(tags.stream().map(String::trim).toList());
                    }
                    
                    debts.add(save(builder.build()));
                }
            }
        }
        return debts;
    }
    
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
    
    public Long countByUserAndStatus(User user, TechDebt.StatusDebito status) {
        return techDebtRepository.countByUserAndStatus(user, status);
    }
    
    public Long countByUser(User user) {
        return techDebtRepository.countByUser(user);
    }
    
    public Page<TechDebt> findAllWithFilters(TechDebt.StatusDebito status, 
                                            Integer prioridade, TechDebt.TipoDebito tipo,
                                            String search, String taskNumber, String periodo,
                                            Pageable pageable) {
        java.time.LocalDateTime periodoDate = null;
        if (periodo != null && !periodo.isEmpty()) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            switch (periodo) {
                case "semana" -> periodoDate = now.minusWeeks(1);
                case "15dias" -> periodoDate = now.minusDays(15);
                case "30dias" -> periodoDate = now.minusDays(30);
                case "3meses" -> periodoDate = now.minusMonths(3);
                case "6meses" -> periodoDate = now.minusMonths(6);
                case "1ano" -> periodoDate = now.minusYears(1);
            }
        }
        return techDebtRepository.findAll(
            TechDebtQueryBuilder.buildSpecification(status, prioridade, tipo, search, taskNumber, periodoDate),
            pageable
        );
    }
    
    public Long countByStatus(TechDebt.StatusDebito status) {
        return techDebtRepository.countByStatus(status);
    }
    
    public Long countAll() {
        return techDebtRepository.countAll();
    }
    
    public Map<String, Object> getAllDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        metrics.put("total", techDebtRepository.countAll());
        metrics.put("todo", techDebtRepository.countByStatus(TechDebt.StatusDebito.TODO));
        metrics.put("inProgress", techDebtRepository.countByStatus(TechDebt.StatusDebito.IN_PROGRESS));
        metrics.put("done", techDebtRepository.countByStatus(TechDebt.StatusDebito.DONE));
        
        return metrics;
    }
    
    public Map<String, Object> getDashboardMetrics(User user) {
        Map<String, Object> metrics = new HashMap<>();
        
        metrics.put("total", techDebtRepository.countByUser(user));
        metrics.put("todo", techDebtRepository.countByUserAndStatus(user, TechDebt.StatusDebito.TODO));
        metrics.put("inProgress", techDebtRepository.countByUserAndStatus(user, TechDebt.StatusDebito.IN_PROGRESS));
        metrics.put("done", techDebtRepository.countByUserAndStatus(user, TechDebt.StatusDebito.DONE));
        
        // Contagem por prioridade
        Map<Integer, Long> contadorPrioridade = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            Long count = techDebtRepository.countByUserAndPrioridade(user, i);
            contadorPrioridade.put(i, count != null ? count : 0L);
        }
        metrics.put("tempoMedioPorPrioridade", contadorPrioridade);
        
        return metrics;
    }
    
    public List<api.work.profile.dto.TechDebtSearchDTO> searchByText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }
        return techDebtRepository.findByProblemaOrDescricaoContainingIgnoreCase(
            text.trim(), PageRequest.of(0, 10));
    }
    
    public List<api.work.profile.dto.TechDebtSearchDTO> searchByTaskNumber(String taskNumber) {
        if (taskNumber == null || taskNumber.trim().isEmpty()) {
            return List.of();
        }
        return techDebtRepository.findByTaskNumberExact(
            taskNumber.trim(), PageRequest.of(0, 10));
    }
}