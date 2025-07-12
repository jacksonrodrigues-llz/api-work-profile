package api.work.profile.service;

import api.work.profile.dto.TechDebtDTO;
import api.work.profile.entity.TechDebt;
import api.work.profile.entity.User;
import api.work.profile.repository.TechDebtRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
        return techDebtRepository.save(techDebt);
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
    
    public void delete(Long id) {
        techDebtRepository.deleteById(id);
    }
    
    public List<TechDebt> importFromJson(List<TechDebtDTO> dtos, User user) {
        List<TechDebt> debts = new ArrayList<>();
        for (TechDebtDTO dto : dtos) {
            TechDebt debt = dto.toEntity();
            debt.setUser(user);
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
                    TechDebt debt = new TechDebt();
                    debt.setProblema(fields[0].trim().replace("\"", ""));
                    debt.setDescricao(fields[1].trim().replace("\"", ""));
                    debt.setPrioridade(Integer.parseInt(fields[2].trim()));
                    debt.setCriadoPor(fields[3].trim().replace("\"", ""));
                    debt.setUser(user);
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
                    TechDebt debt = new TechDebt();
                    debt.setProblema(getCellValue(row.getCell(0))); // problema
                    debt.setDescricao(getCellValue(row.getCell(1))); // descricao
                    debt.setPrioridade((int) row.getCell(2).getNumericCellValue()); // prioridade
                    
                    // tipos (coluna 3)
                    String tiposStr = getCellValue(row.getCell(3));
                    if (tiposStr != null && !tiposStr.isEmpty()) {
                        List<TechDebt.TipoDebito> tipos = new ArrayList<>();
                        for (String tipo : tiposStr.split(",")) {
                            try {
                                tipos.add(TechDebt.TipoDebito.valueOf(tipo.trim().toUpperCase()));
                            } catch (Exception e) {
                                // Ignorar tipos inválidos
                            }
                        }
                        debt.setTipos(tipos);
                    }
                    
                    // tags (coluna 4)
                    String tagsStr = getCellValue(row.getCell(4));
                    if (tagsStr != null && !tagsStr.isEmpty()) {
                        List<String> tags = List.of(tagsStr.split(","));
                        debt.setTags(tags.stream().map(String::trim).toList());
                    }
                    
                    debt.setCriadoPor(getCellValue(row.getCell(5))); // criadoPor
                    debt.setUser(user);
                    debts.add(save(debt));
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
}