package com.bizkredit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;

    private Long userId;

    @Column(nullable = false)
    private String action;         // e.g. CREATE, UPDATE, DELETE, LOGIN

    private String entityType;     // e.g. LoanApplication, User

    private String recordId;       // ID of the affected record

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
