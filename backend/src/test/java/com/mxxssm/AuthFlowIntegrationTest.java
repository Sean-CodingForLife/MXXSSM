package com.mxxssm;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginReturnsCurrentUserAndMenus() throws Exception {
        MockHttpSession session = login("admin", "Admin@123");

        mockMvc.perform(get("/api/v1/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.roles[0]").value("ADMIN"))
                .andExpect(jsonPath("$.data.permissions").isArray())
                .andExpect(jsonPath("$.data.menus").isArray())
                .andExpect(jsonPath("$.data.menus[0].path").exists());
    }

    @Test
    void unauthenticatedApiRequestReturnsJson401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTH_REQUIRED"));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "admin",
                                "password", "wrong-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("BAD_CREDENTIALS"));
    }

    @Test
    void unsafeRequestWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "admin",
                                "password", "Admin@123"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CSRF_INVALID"));
    }

    @Test
    void standardUserCannotReadGlobalDashboard() throws Exception {
        MockHttpSession admin = login("admin", "Admin@123");
        String username = uniqueUsername("std");

        try {
            mockMvc.perform(post("/api/v1/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "username", username,
                                    "displayName", "Standard User",
                                    "password", "StdUser123",
                                    "email", username + "@example.com"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            MockHttpSession userSession = login(username, "StdUser123");
            mockMvc.perform(get("/api/v1/auth/me").session(userSession))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.roles[0]").value("USER"));

            mockMvc.perform(get("/api/v1/dashboard").session(userSession))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        } finally {
            deleteUserIfPresent(admin, username);
        }
    }

    @Test
    void filterUsersByRoleAndStatus() throws Exception {
        MockHttpSession admin = login("admin", "Admin@123");

        mockMvc.perform(get("/api/v1/admin/users")
                        .session(admin)
                        .param("role", "ADMIN")
                        .param("status", "ACTIVE")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.items[0].roles").value(org.hamcrest.Matchers.hasItem("ADMIN")));
    }

    @Test
    void createUpdateAndDeleteUser() throws Exception {
        MockHttpSession admin = login("admin", "Admin@123");
        String username = uniqueUsername("crud");
        long userId = createUser(admin, username, "USER", "CrudUser123");

        try {
            mockMvc.perform(get("/api/v1/admin/users")
                            .session(admin)
                            .param("keyword", username)
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.total").value(1))
                    .andExpect(jsonPath("$.data.items[0].username").value(username))
                    .andExpect(jsonPath("$.data.items[0].roles[0]").value("USER"));

            Map<String, Object> update = new HashMap<>();
            update.put("displayName", "Updated Crud User");
            update.put("email", "updated@example.com");
            update.put("phone", "");
            update.put("roles", new String[]{"USER"});
            update.put("status", "ACTIVE");

            mockMvc.perform(put("/api/v1/admin/users/" + userId)
                            .session(admin)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.displayName").value("Updated Crud User"))
                    .andExpect(jsonPath("$.data.email").value("updated@example.com"));

            mockMvc.perform(delete("/api/v1/admin/users/" + userId).session(admin).with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        } finally {
            deleteUserIfPresent(admin, username);
        }
    }

    @Test
    void validationRejectsWeakPassword() throws Exception {
        MockHttpSession admin = login("admin", "Admin@123");
        Map<String, Object> body = new HashMap<>();
        body.put("username", uniqueUsername("weak"));
        body.put("displayName", "Weak Password");
        body.put("password", "short");
        body.put("roles", new String[]{"USER"});
        body.put("status", "ACTIVE");

        mockMvc.perform(post("/api/v1/admin/users")
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void cannotDeleteOwnAccount() throws Exception {
        MockHttpSession admin = login("admin", "Admin@123");

        mockMvc.perform(delete("/api/v1/admin/users/1").session(admin).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SELF_OPERATION"));
    }

    @Test
    void customRoleGrantsOnlyItsAssignedPermission() throws Exception {
        MockHttpSession admin = login("admin", "Admin@123");
        String roleCode = "TEST_ROLE_" + System.nanoTime();
        String username = uniqueUsername("rbac");
        long roleId = -1;
        long permissionId = -1;

        try {
            MvcResult roleResult = mockMvc.perform(post("/api/v1/admin/roles")
                            .session(admin)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "code", roleCode,
                                    "name", "Test role",
                                    "description", "RBAC integration test role",
                                    "status", "ACTIVE"))))
                    .andExpect(status().isOk())
                    .andReturn();
            roleId = read(roleResult).path("data").path("id").asLong();

            MvcResult permissionsResult = mockMvc.perform(get("/api/v1/admin/permissions/all").session(admin))
                    .andExpect(status().isOk())
                    .andReturn();
            for (JsonNode permission : read(permissionsResult).path("data")) {
                if ("admin:user:read".equals(permission.path("code").asText())) {
                    permissionId = permission.path("id").asLong();
                }
            }
            if (permissionId < 0) {
                throw new IllegalStateException("admin:user:read permission not found");
            }

            mockMvc.perform(put("/api/v1/admin/roles/" + roleId + "/permissions")
                            .session(admin)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "permissionIds", new long[]{permissionId}))))
                    .andExpect(status().isOk());

            createUser(admin, username, roleCode, "RbacUser123");
            MockHttpSession userSession = login(username, "RbacUser123");

            mockMvc.perform(get("/api/v1/admin/users").session(userSession))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            mockMvc.perform(get("/api/v1/admin/roles").session(userSession))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        } finally {
            deleteUserIfPresent(admin, username);
            if (roleId > 0) {
                mockMvc.perform(delete("/api/v1/admin/roles/" + roleId).session(admin).with(csrf()));
            }
        }
    }

    private MockHttpSession login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        if (session == null) {
            throw new IllegalStateException("Login did not create an HTTP session");
        }
        return session;
    }

    private long createUser(MockHttpSession admin, String username, String roleCode, String password)
            throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("displayName", "Integration User");
        body.put("password", password);
        body.put("email", username + "@example.com");
        body.put("phone", "");
        body.put("roles", new String[]{roleCode});
        body.put("status", "ACTIVE");

        MvcResult result = mockMvc.perform(post("/api/v1/admin/users")
                        .session(admin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();
        return read(result).path("data").path("id").asLong();
    }

    private void deleteUserIfPresent(MockHttpSession admin, String username) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/admin/users")
                        .session(admin)
                        .param("keyword", username)
                        .param("page", "1")
                        .param("size", "10"))
                .andReturn();
        for (JsonNode user : read(result).path("data").path("items")) {
            if (username.equals(user.path("username").asText())) {
                mockMvc.perform(delete("/api/v1/admin/users/" + user.path("id").asLong())
                                .session(admin)
                                .with(csrf()))
                        .andExpect(status().isOk());
            }
        }
    }

    private JsonNode read(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(content);
    }

    private String uniqueUsername(String prefix) {
        return prefix + Long.toString(System.nanoTime(), 36);
    }
}
