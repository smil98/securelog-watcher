package com.securelogwatcher.service;

import com.securelogwatcher.domain.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.securelogwatcher.domain.Role;
import com.securelogwatcher.repository.UserRepository;
import com.securelogwatcher.service.AuditLogService;
import com.securelogwatcher.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private UserService userService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User testUser;
    private User adminUser;
    private User superAdminUser;

    @BeforeEach
    void setup() {
        testUser = User.builder().username("testuser").enabled(true).role(Role.ROLE_USER).build();
        adminUser = User.builder().username("admin").enabled(true).role(Role.ROLE_ADMIN).build();
        superAdminUser = User.builder().username("superadmin").enabled(true).role(Role.ROLE_SUPER_ADMIN).build();

        // Mock SecurityContextHolder for methods that need the current user's info
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("admin");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void whenUserToggled_thenStatusIsChangedAndAudited() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        userService.toggleUserEnable("testuser");

        assertThat(testUser.isEnabled()).isFalse();
        verify(userRepository, times(1)).save(testUser);
        verify(auditLogService, times(1)).logUserManagementAction(eq("admin"), contains("disabled."));
    }

    @Test
    void whenAdminTriesToDisableSelf_thenAccessIsDenied() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(authentication.getName()).thenReturn("admin");

        assertThrows(AccessDeniedException.class, () -> userService.toggleUserEnable("admin"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenAdminTriesToDisableSuperAdmin_thenAccessIsDenied() {
        when(userRepository.findByUsername("superadmin")).thenReturn(Optional.of(superAdminUser));

        assertThrows(AccessDeniedException.class, () -> userService.toggleUserEnable("superadmin"));
        verify(userRepository, never()).save(any(User.class));
    }

}
