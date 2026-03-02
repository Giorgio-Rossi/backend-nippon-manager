package com.projectstarter.starter.Dto.Response;

import com.projectstarter.starter.Entity.User;
import lombok.Data;

@Data
public class UserResponse {
    private String username;
    private String email;
    private String role;
    private boolean superuser;

    public static UserResponse from(User user) {
        UserResponse response = new UserResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setSuperuser(user.isSuperuser());
        return response;
    }
}
