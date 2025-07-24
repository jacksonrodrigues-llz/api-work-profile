package api.work.profile.controller;

import api.work.profile.entity.BoardColumn;
import api.work.profile.service.BoardColumnService;
import api.work.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/columns")
@RequiredArgsConstructor
public class BoardColumnController {
    
    private final BoardColumnService boardColumnService;
    private final ProfileService profileService;
    
    @PostMapping
    public ResponseEntity<BoardColumn> createColumn(@RequestBody BoardColumn column, Authentication authentication) {
        var user = profileService.getUserFromAuthentication(authentication);
        var saved = boardColumnService.saveColumn(column, user);
        return ResponseEntity.ok(saved);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<BoardColumn> updateColumn(@PathVariable Long id, @RequestBody BoardColumn column, Authentication authentication) {
        var user = profileService.getUserFromAuthentication(authentication);
        var updated = boardColumnService.updateColumn(id, column, user);
        return ResponseEntity.ok(updated);
    }
    
    @PutMapping("/positions")
    public ResponseEntity<?> updatePositions(@RequestBody Map<String, List<Long>> payload, Authentication authentication) {
        var user = profileService.getUserFromAuthentication(authentication);
        boardColumnService.updateColumnPositions(payload.get("columnIds"), user);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteColumn(@PathVariable Long id, Authentication authentication) {
        try {
            var user = profileService.getUserFromAuthentication(authentication);
            boardColumnService.deleteColumn(id, user);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}