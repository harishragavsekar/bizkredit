package com.bizkredit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "group_company")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_business_id", nullable = false)
    private SMEBusiness parentBusiness;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subsidiary_business_id", nullable = false)
    private SMEBusiness subsidiaryBusiness;

    private String relationship;        // Subsidiary / Associate / JV

    @Builder.Default
    private Boolean consolidationRequired = false;
}
