package ru.kata.spring.boot_security.demo;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import ru.kata.spring.boot_security.demo.configs.SuccessUserHandler;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class RoleAndRedirectTest {
    @Test void userDetailsExposesEmailAndRoles() {
        User user = new User("A", "B", "test@example.com", "hash");
        user.setRoles(new HashSet<>(Collections.singleton(new Role("ROLE_USER"))));
        assertEquals("test@example.com", user.getUsername());
        assertEquals(1, user.getAuthorities().size());
        assertEquals("ROLE_USER", user.getAuthorities().iterator().next().getAuthority());
    }
    @Test void adminWinsWhenUserHasMultipleRoles() throws Exception {
        SuccessUserHandler handler = new SuccessUserHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(request, response, new UsernamePasswordAuthenticationToken(
                "admin", "hidden", Arrays.asList(new Role("ROLE_USER"), new Role("ROLE_ADMIN"))));
        assertEquals("/admin", response.getRedirectedUrl());
    }
    @Test void userRedirectsToPersonalPage() throws Exception {
        SuccessUserHandler handler = new SuccessUserHandler();
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response,
                new UsernamePasswordAuthenticationToken("user", "hidden", Collections.singleton(new Role("ROLE_USER"))));
        assertEquals("/user", response.getRedirectedUrl());
    }
}
