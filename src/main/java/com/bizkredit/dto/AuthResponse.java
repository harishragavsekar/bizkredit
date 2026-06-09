package com.bizkredit.dto;

import com.bizkredit.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tokenType;
    private Long userId;
    private String name;
    private String email;
    private Role role;
}
