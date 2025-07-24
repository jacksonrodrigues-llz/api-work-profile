package api.work.profile.service;

import api.work.profile.entity.Activity;
import api.work.profile.entity.BoardColumn;
import api.work.profile.entity.User;
import api.work.profile.repository.ActivityRepository;
import api.work.profile.repository.BoardColumnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardColumnService {
    
    private final BoardColumnRepository boardColumnRepository;
    private final ActivityRepository activityRepository;
    
    public List<BoardColumn> getUserColumns(User user) {
        var columns = boardColumnRepository.findByUserOrderByPosition(user);
        if (columns.isEmpty()) {
            return createDefaultColumns(user);
        }
        return columns;
    }
    
    private List<BoardColumn> createDefaultColumns(User user) {
        var defaultColumns = List.of(
            BoardColumn.builder().name("A FAZER").status("TODO").position(0).iconClass("fas fa-circle text-secondary").colorClass("bg-secondary").user(user).deletable(false).build(),
            BoardColumn.builder().name("EM PROGRESSO").status("IN_PROGRESS").position(1).iconClass("fas fa-circle text-warning").colorClass("bg-warning").user(user).deletable(false).build(),
            BoardColumn.builder().name("DEV").status("DEV").position(2).iconClass("fas fa-circle text-info").colorClass("bg-info").user(user).deletable(true).build(),
            BoardColumn.builder().name("UAT").status("UAT").position(3).iconClass("fas fa-circle text-warning").colorClass("bg-warning").user(user).deletable(true).build(),
            BoardColumn.builder().name("HML").status("HML").position(4).iconClass("fas fa-circle text-primary").colorClass("bg-primary").user(user).deletable(true).build(),
            BoardColumn.builder().name("PRD").status("PRD").position(5).iconClass("fas fa-circle text-danger").colorClass("bg-danger").user(user).deletable(true).build(),
            BoardColumn.builder().name("DEPLOY").status("DEPLOY").position(6).iconClass("fas fa-circle").colorClass("").user(user).deletable(true).build(),
            BoardColumn.builder().name("CONCLUÍDO").status("DONE").position(7).iconClass("fas fa-circle text-success").colorClass("bg-success").user(user).deletable(false).build()
        );
        return boardColumnRepository.saveAll(defaultColumns);
    }
    
    public BoardColumn saveColumn(BoardColumn column, User user) {
        var maxPosition = boardColumnRepository.findByUserOrderByPosition(user).stream()
            .mapToInt(BoardColumn::getPosition)
            .max()
            .orElse(-1);
        
        return boardColumnRepository.save(column.toBuilder()
            .user(user)
            .position(maxPosition + 1)
            .build());
    }
    
    public BoardColumn updateColumn(Long id, BoardColumn column, User user) {
        var existing = boardColumnRepository.findById(id)
            .filter(c -> c.getUser().equals(user))
            .orElseThrow(() -> new IllegalArgumentException("Coluna não encontrada"));
        
        return boardColumnRepository.save(existing.toBuilder()
            .name(column.getName())
            .iconClass(column.getIconClass())
            .colorClass(column.getColorClass())
            .build());
    }
    
    @Transactional
    public void updateColumnPositions(List<Long> columnIds, User user) {
        for (int i = 0; i < columnIds.size(); i++) {
            var column = boardColumnRepository.findById(columnIds.get(i))
                .filter(c -> c.getUser().equals(user))
                .orElseThrow(() -> new IllegalArgumentException("Coluna não encontrada"));
            
            boardColumnRepository.save(column.toBuilder().position(i).build());
        }
    }
    
    public void deleteColumn(Long id, User user) {
        var column = boardColumnRepository.findById(id)
            .filter(c -> c.getUser().equals(user))
            .orElseThrow(() -> new IllegalArgumentException("Coluna não encontrada"));
        
        if (!column.getDeletable()) {
            throw new IllegalArgumentException("Esta coluna não pode ser excluída");
        }
        
        var hasActivities = activityRepository.existsByUserAndStatus(user, column.getStatus());
        if (hasActivities) {
            throw new IllegalArgumentException("Não é possível excluir coluna com atividades");
        }
        
        boardColumnRepository.delete(column);
    }
}