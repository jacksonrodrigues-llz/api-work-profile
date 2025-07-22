package api.work.profile.service;

import api.work.profile.entity.User;
import api.work.profile.enums.UserRole;
import api.work.profile.repository.UserRepository;
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
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User adminUser;
    private OAuth2User oAuth2User;

    @BeforeEach
    void setUp() {
        // Configurar usuário de teste
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .githubUsername("testuser")
                .avatar("https://avatar.url/test")
                .role(UserRole.USER)
                .enabled(true)
                .build();

        // Configurar usuário admin
        adminUser = User.builder()
                .id(2L)
                .name("Admin User")
                .email("admin@example.com")
                .githubUsername("adminuser")
                .avatar("https://avatar.url/admin")
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();

        // Configurar OAuth2User
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@example.com");
        attributes.put("name", "Test User");
        attributes.put("login", "testuser");
        attributes.put("avatar_url", "https://avatar.url/test");
        
        oAuth2User = new DefaultOAuth2User(
                List.of(() -> "ROLE_USER"),
                attributes,
                "name"
        );
    }

    @Test
    void findOrCreateUser_ExistingUser_ShouldReturnExistingUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.findOrCreateUser(oAuth2User);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getName());
        assertEquals("testuser", result.getGithubUsername());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findOrCreateUser_ExistingUserWithUpdatedInfo_ShouldUpdateAndReturnUser() {
        // Arrange
        User existingUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .githubUsername("oldusername")
                .avatar("https://old.avatar.url")
                .role(UserRole.USER)
                .enabled(true)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.findOrCreateUser(oAuth2User);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("testuser", result.getGithubUsername());
        assertEquals("https://avatar.url/test", result.getAvatar());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void findOrCreateUser_NewUser_ShouldCreateAndReturnUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.count()).thenReturn(5L); // Não é o primeiro usuário
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.findOrCreateUser(oAuth2User);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getName());
        assertEquals("testuser", result.getGithubUsername());
        assertEquals(UserRole.USER, result.getRole());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, times(1)).count();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void findOrCreateUser_FirstUser_ShouldCreateAdminAndReturnUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.count()).thenReturn(0L); // É o primeiro usuário
        
        User adminTestUser = testUser.toBuilder().role(UserRole.ADMIN).build();
        when(userRepository.save(any(User.class))).thenReturn(adminTestUser);

        // Act
        User result = userService.findOrCreateUser(oAuth2User);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals(UserRole.ADMIN, result.getRole());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, times(1)).count();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserRole_AsAdmin_ShouldUpdateRole() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        User updatedUser = testUser.toBuilder().role(UserRole.ADMIN).build();
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        User result = userService.updateUserRole(1L, UserRole.ADMIN, adminUser);

        // Assert
        assertNotNull(result);
        assertEquals(UserRole.ADMIN, result.getRole());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserRole_AsNonAdmin_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserRole(1L, UserRole.ADMIN, testUser);
        });
        
        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserRole_NonExistingUser_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserRole(99L, UserRole.ADMIN, adminUser);
        });
        
        verify(userRepository, times(1)).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByEmail_ExistingEmail_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.findByEmail("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void findByEmail_NonExistingEmail_ShouldReturnNull() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        User result = userService.findByEmail("nonexistent@example.com");

        // Assert
        assertNull(result);
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void findAll_ShouldReturnPageOfUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<User> userList = List.of(testUser, adminUser);
        Page<User> userPage = new PageImpl<>(userList, pageable, 2);

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // Act
        Page<User> result = userService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(userList, result.getContent());
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    void findById_ExistingId_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void findById_NonExistingId_ShouldReturnNull() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        User result = userService.findById(99L);

        // Assert
        assertNull(result);
        verify(userRepository, times(1)).findById(99L);
    }
}