package com.bizkredit.entity;

import com.bizkredit.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// User entity - represents all actors in the system
// Maps to the 'users' table in MySQL
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

    // Role determines what the user can access in the system
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    // Password stored as BCrypt hash - never plain text
    @NotBlank
    private String password;

    @NotBlank
    private String phone;

    private String branchId;

    // Active = normal access, Locked = temporary block, Inactive = removed
    @Builder.Default
    private String status = "Active";
}
