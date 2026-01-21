package com.dresstyle.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Data
@Builder
@Setter
@Getter
public class AuthResponse {
    private String token;
    private String name;
    private String type = "Bearer";
    private String userId;
    private String email;
    private Set<String> roles;

}