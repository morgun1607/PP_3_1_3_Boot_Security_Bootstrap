package ru.kata.spring.boot_security.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:securitytest;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "app.admin.password=integration-admin-pass",
        "app.user.password=integration-user-pass"
})
class SecurityIntegrationTest {
    @Autowired private MockMvc mvc;

    private MockHttpSession login(String email, String password) throws Exception {
        return (MockHttpSession) mvc.perform(formLogin().user(email).password(password))
                .andExpect(status().is3xxRedirection())
                .andReturn().getRequest().getSession(false);
    }

    @Test void anonymousCannotAccessProtectedPages() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(get("/user")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test void regularUserIsRedirectedAndDeniedAdminAccess() throws Exception {
        mvc.perform(formLogin().user("user@example.com").password("integration-user-pass"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/user"));
        MockHttpSession session = login("user@example.com", "integration-user-pass");
        mvc.perform(get("/user").session(session)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("user@example.com")));
        mvc.perform(get("/admin").session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/admin/new").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/admin/1/delete").with(csrf()).session(session))
                .andExpect(status().isForbidden());
    }

    @Test void adminWithBothRolesCanSeeAdminAndOwnProfile() throws Exception {
        mvc.perform(formLogin().user("admin@example.com").password("integration-admin-pass"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin"));
        MockHttpSession session = login("admin@example.com", "integration-admin-pass");
        mvc.perform(get("/admin").session(session)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("admin@example.com")));
        mvc.perform(get("/user").session(session)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("admin@example.com")));
    }

    @Test void csrfIsRequiredForMutationsAndLogoutWorks() throws Exception {
        MockHttpSession session = login("admin@example.com", "integration-admin-pass");
        mvc.perform(post("/admin/1/delete").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/logout").with(csrf()).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
        mvc.perform(get("/admin")).andExpect(status().is3xxRedirection());
    }

    @Test void adminCanCreateAndUpdateUsers() throws Exception {
        MockHttpSession session = login("admin@example.com", "integration-admin-pass");
        mvc.perform(post("/admin/create").session(session).with(csrf())
                        .param("name", "Test").param("lastName", "Person")
                        .param("email", "new-integration@example.com")
                        .param("password", "integration-new-pass")
                        .param("roleIds", "2"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin"));
        mvc.perform(get("/admin").session(session)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("new-integration@example.com")));
    }
}
