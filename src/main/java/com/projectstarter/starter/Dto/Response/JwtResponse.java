package com.projectstarter.starter.Dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private String token;
    @JsonProperty("refreshToken")
    private String refreshToken;
    private String type = "Bearer";
    private String username;

    public JwtResponse(String token, String refreshToken, String username) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.username = username;
    }
}
