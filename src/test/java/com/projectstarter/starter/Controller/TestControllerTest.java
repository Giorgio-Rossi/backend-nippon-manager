package com.projectstarter.starter.Controller;

import com.projectstarter.starter.Config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;



    @Test
    @DisplayName("GET /test/all - should return public content")
    void testPublicAccess() throws Exception {
        mockMvc.perform(get("/test/all"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Public Content"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("GET /test/user - should allow USER")
    void testUserAccessWithUserRole() throws Exception {
        mockMvc.perform(get("/test/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("User Content"))
                .andExpect(jsonPath("$.message").value("This endpoint is accessible to authenticated users. Welcome user1!"));
    }

    @Test
    @WithMockUser(username = "admin1", roles = {"ADMIN"})
    @DisplayName("GET /test/admin - should allow ADMIN")
    void testAdminAccessWithAdminRole() throws Exception {
        mockMvc.perform(get("/test/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Admin Content"))
                .andExpect(jsonPath("$.message").value("This endpoint is accessible only to admins. Welcome admin1!"));
    }

    @Test
    @WithMockUser(username = "user2", roles = {"USER"})
    @DisplayName("GET /test/profile - should return user profile")
    void testUserProfile() throws Exception {
        mockMvc.perform(get("/test/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user2"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.message").value("User profile information"));
    }
}