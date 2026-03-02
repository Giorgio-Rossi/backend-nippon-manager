package com.projectstarter.starter.Controller;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestController {

    @GetMapping("/all")
    public Map<String, Object> allAccess() {
        return Map.of(
                "title", "Public Content",
                "message", "This endpoint is accessible to everyone"
        );
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Map<String, Object> userAccess(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "anonymous";
        return Map.of(
                "title", "User Content",
                "message", "This endpoint is accessible to authenticated users. Welcome " + username + "!"
        );
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> adminAccess(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "anonymous";
        return Map.of(
                "title", "Admin Content",
                "message", "This endpoint is accessible only to admins. Welcome " + username + "!"
        );
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Map<String, Object> userProfile(Authentication authentication) {
        if (authentication == null) {
            throw new AccessDeniedException("Authentication required");
        }

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        return Map.of(
                "username", authentication.getName(),
                "roles", List.of(role),
                "message", "User profile information"
        );
    }
}