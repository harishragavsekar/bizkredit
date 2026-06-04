package com.bizkredit.entity;

import com.bizkredit.enums.DocumentType;
import com.bizkredit.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// ApplicationDocument - documents uploaded against a loan application
// Each application can have multiple documents of different types
@Entity
@Table(name = "application_document")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long docId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication application;

    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    private String financialYear;   // e.g. "2023-24" for financial docs

    private String filePath;        // Path where file is stored

    @Builder.Default
    private LocalDate uploadedDate = LocalDate.now();

    // Starts as PENDING, analyst updates to VERIFIED or DEFICIENT
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
}
