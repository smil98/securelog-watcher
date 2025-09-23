package com.securelogwatcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securelogwatcher.dto.ApiResponseDto;
import com.securelogwatcher.domain.Role;
import com.securelogwatcher.domain.User;
import com.securelogwatcher.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // A test user that will be affected by admin actions
    private static final String TARGET_USERNAME = "testuser_to_toggle";

    @BeforeEach
    void setup() {
        if (!userRepository.existsByUsername(TARGET_USERNAME)) {
            userRepository.save(User.builder()
                    .username(TARGET_USERNAME)
                    .password("password")
                    .email("test@example.com")
                    .role(Role.ROLE_USER)
                    .enabled(true)
                    .build());
        }
    }

    // --- Positive Scenarios (Authorized Access) ---

    @Test
    void whenAdminAccessesUserList_thenReturnsOk() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .with(user("admin").roles("ADMIN"))) // Simulate a user with ROLE_ADMIN
                .andExpect(status().isOk());
    }

    @Test
    void whenAuditorAccessesUserList_thenReturnsOk() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .with(user("auditor").roles("AUDITOR"))) // Simulate a user with ROLE_AUDITOR
                .andExpect(status().isOk());
    }

    @Test
    void whenSecurityManagerTogglesUserEnable_thenReturnsOk() throws Exception {
        mockMvc.perform(post("/api/admin/users/{username}/toggle-enable", TARGET_USERNAME)
                .with(user("security_manager").roles("SECURITY_MANAGER")))
                .andExpect(status().isOk());
        // You could add a repository check here to confirm the user's 'enabled' status
        // changed
    }

    // --- Negative Scenarios (Unauthorized Access) ---

    @Test
    void whenRegularUserAccessesUserList_thenReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .with(user("regular_user").roles("USER"))) // Simulate a user with ROLE_USER
                .andExpect(status().isForbidden()); // Expect a 403 Forbidden
    }

    @Test
    void whenRegularUserTogglesUserEnable_thenReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/admin/users/{username}/toggle-enable", TARGET_USERNAME)
                .with(user("regular_user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    // --- Specific Business Logic & Security Scenarios ---

    @Test
    void whenAdminChangesUserRoleToAdmin_thenReturnsOk() throws Exception {
        String usernameToChange = "testuser_to_promote";
        if (!userRepository.existsByUsername(usernameToChange)) {
            userRepository.save(User.builder()
                    .username(usernameToChange)
                    .password("password")
                    .email("change@example.com")
                    .role(Role.ROLE_USER)
                    .enabled(true)
                    .build());
        }

        mockMvc.perform(post("/api/admin/users/{username}/change-role", usernameToChange)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString("ROLE_AUDITOR"))) // Change to a valid role
                .andExpect(status().isOk());
    }

    @Test
    void whenNonSuperAdminTriesToAssignSuperAdminRole_thenReturnsForbidden() throws Exception {
        String usernameToChange = "testuser_to_promote_to_super";
        if (!userRepository.existsByUsername(usernameToChange)) {
            userRepository.save(User.builder()
                    .username(usernameToChange)
                    .password("password")
                    .email("change@example.com")
                    .role(Role.ROLE_USER)
                    .enabled(true)
                    .build());
        }

        mockMvc.perform(post("/api/admin/users/{username}/change-role", usernameToChange)
                .with(user("admin").roles("ADMIN")) // Not a SUPER_ADMIN
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString("ROLE_SUPER_ADMIN"))) // Try to assign SUPER_ADMIN
                .andExpect(status().isForbidden()); // Expect a 403 Forbidden
    }

    @Test
    void whenUserTriesToChangeTheirOwnRole_thenReturnsForbidden() throws Exception {
        String usernameToChange = "self_change_user";
        if (!userRepository.existsByUsername(usernameToChange)) {
            userRepository.save(User.builder()
                    .username(usernameToChange)
                    .password("password")
                    .email("change@example.com")
                    .role(Role.ROLE_USER)
                    .enabled(true)
                    .build());
        }

        mockMvc.perform(post("/api/admin/users/{username}/change-role", usernameToChange)
                .with(user(usernameToChange).roles("ADMIN")) // The user is logged in as themself
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString("ROLE_AUDITOR")))
                .andExpect(status().isForbidden());
    }
}
