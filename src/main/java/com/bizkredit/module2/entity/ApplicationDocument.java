package com.bizkredit.module2.entity;

import com.bizkredit.common.enums.DocumentType;
import com.bizkredit.common.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "application_document")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "application")
public class ApplicationDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long docId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication application;

    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    private String financialYear;
    private String filePath;

    @Builder.Default
    private LocalDate uploadedDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
}
