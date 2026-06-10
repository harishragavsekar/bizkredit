package com.bizkredit.entity;

import com.bizkredit.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @NotBlank
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    // @JsonIgnore - password hash never returned in API responses (security)
    @JsonIgnore
    @NotBlank
    private String password;

    @NotBlank
    private String phone;

    private String branchId;

    @Builder.Default
    private String status = "Active";
}
