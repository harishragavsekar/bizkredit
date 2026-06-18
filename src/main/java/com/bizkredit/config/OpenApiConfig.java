package com.bizkredit.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Configures Swagger UI to show the "Authorize" button for JWT
// Lets you paste a token once and send it on all protected endpoints
@Configuration
public class OpenApiConfig {

    private static final String SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI bizKreditOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BizKredit API")
                        .description("SME Business Loan & Working Capital Platform")
                        .version("v1"))
                // Apply the security scheme globally
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SCHEME_NAME, new SecurityScheme()
                                .name(SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    /*
     * Team module groups, shown as a dropdown in the top-right corner of Swagger UI.
     * Each group lists EXACT sub-paths rather than a parent wildcard, because several
     * controllers share the same path prefix for different sub-resources — e.g.
     * /api/loan-applications/{appId}/** is used by BOTH SMELoanController (the
     * application itself + documents) and FinancialAnalysisController (financial
     * statements + credit proposals). A wildcard there would pull both groups'
     * endpoints into one, so each rule below is deliberately explicit.
     */

    @Bean
    public GroupedOpenApi group1AuthUsersScopeAuditApi() {
        return GroupedOpenApi.builder()
                .group("01-auth-users-scope-audit")
                .displayName("1. Auth, Users, Scope & Audit")
                .pathsToMatch(
                        "/api/auth/**",
                        "/api/users/**",
                        "/api/audit-logs/**"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi group2OriginationApi() {
        return GroupedOpenApi.builder()
                .group("02-sme-origination")
                .displayName("2. SME Onboarding & Loan Origination")
                .pathsToMatch(
                        "/api/sme-businesses/**",
                        "/api/loan-products/**",

                        "/api/loan-applications",
                        "/api/loan-applications/{id}",
                        "/api/loan-applications/{id}/submit",
                        "/api/loan-applications/{id}/assign",
                        "/api/loan-applications/{id}/status",

                        "/api/loan-applications/{appId}/documents",
                        "/api/loan-applications/{appId}/documents/{docId}",
                        "/api/loan-applications/{appId}/documents/{docId}/verify",
                        "/api/loan-applications/{appId}/documents/{docId}/flag-deficient",
                        "/api/loan-applications/{appId}/documents/{docId}/reject"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi group3CreditAnalysisApi() {
        return GroupedOpenApi.builder()
                .group("03-credit-analysis-scorecard")
                .displayName("3. Credit Analysis & Scorecard")
                .pathsToMatch(
                        "/api/scorecards/**",
                        "/api/credit-proposals/**",

                        "/api/loan-applications/{appId}/financial-statements",
                        "/api/loan-applications/{appId}/financial-statements/{id}",
                        "/api/loan-applications/{appId}/financial-statements/{id}/verify",

                        "/api/loan-applications/{appId}/credit-proposals",
                        "/api/loan-applications/{appId}/credit-proposals/{id}",
                        "/api/loan-applications/{appId}/credit-proposals/{id}/submit"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi group4FacilityRepaymentApi() {
        return GroupedOpenApi.builder()
                .group("04-facility-disbursement-repayment")
                .displayName("4. Facility, Disbursement & Repayment")
                .pathsToMatch(
                        "/api/loan-applications/{appId}/collaterals",
                        "/api/loan-applications/{appId}/collaterals/{id}",
                        "/api/loan-applications/{appId}/collaterals/{id}/status",
                        "/api/loan-applications/{appId}/collateral-coverage",
                        "/api/loan-applications/{appId}/collaterals/{id}/revalue",
                        "/api/loan-applications/{appId}/collaterals/{id}/revaluations",

                        "/api/facilities",
                        "/api/facilities/{id}",
                        "/api/facilities/business/{businessId}",
                        "/api/facilities/{id}/status",
                        "/api/facilities/expiring",
                        "/api/facilities/{facilityId}/renew",
                        "/api/facilities/{facilityId}/renewal-history",
                        "/api/facilities/{facilityId}/drawdowns",
                        "/api/facilities/{facilityId}/drawdowns/{id}/approve",
                        "/api/facilities/{facilityId}/drawdowns/{id}/disburse",
                        "/api/facilities/{facilityId}/drawdowns/{id}/repay",
                        "/api/facilities/{facilityId}/drawdowns/{id}/overdue",
                        "/api/facilities/{facilityId}/utilisation",
                        "/api/facilities/{facilityId}/utilisation/{id}",

                        "/api/repayments/**",
                        "/api/maker-checker/**"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi group5RiskPortfolioApi() {
        return GroupedOpenApi.builder()
                .group("05-risk-monitoring-portfolio")
                .displayName("5. Risk Monitoring & Portfolio")
                .pathsToMatch(
                        "/api/npa/**",
                        "/api/covenant-templates/**",

                        // These sit under /api/facilities/{facilityId}/... like Module 4's
                        // facility paths do, but are owned by CovenantNotificationController
                        // (covenants/EWS), not CollateralFacilityController — listed
                        // explicitly here so they don't get swept into Module 4 instead.
                        "/api/facilities/{facilityId}/covenants",
                        "/api/facilities/{facilityId}/covenants/{id}",
                        "/api/facilities/{facilityId}/covenants/{id}/status",
                        "/api/facilities/{facilityId}/covenants/{id}/waive",
                        "/api/covenants/{covenantId}/tracking",
                        "/api/facilities/{facilityId}/ews",
                        "/api/ews/**",
                        "/api/notifications/**",

                        "/api/portfolio/**"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi group6AllApisApi() {
        return GroupedOpenApi.builder()
                .group("06-all-apis")
                .displayName("6. All APIs")
                .pathsToMatch("/**")
                .build();
    }
}
