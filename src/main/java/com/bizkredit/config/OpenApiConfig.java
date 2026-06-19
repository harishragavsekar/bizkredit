package com.bizkredit.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI bizKreditOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BizKredit API")
                        .description("SME Business Loan & Working Capital Platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BizKredit Team")
                                .email("support@bizkredit.com"))
                        .license(new License()
                                .name("Proprietary")))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SCHEME_NAME, new SecurityScheme()
                                .name(SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the JWT returned by POST /api/auth/login")));
    }

    /*
     * All APIs
     */
    @Bean
    public GroupedOpenApi allApisApi() {
        return GroupedOpenApi.builder()
                .group("00-all")
                .displayName("All APIs")
                .pathsToMatch("/api/**")
                .build();
    }

    /*
     * Module 1 — Auth, Users, Scope & Audit
     * Owner: Harish
     *
     * Controllers:
     * - AuthController
     * - UserController
     * - UserScopeController
     * - AuditLogController
     */
    @Bean
    public GroupedOpenApi group1AuthUsersScopeAuditApi() {
        return GroupedOpenApi.builder()
                .group("01-auth-users-scope-audit")
                .displayName("1. Auth, Users, Scope & Audit - Harish")
                .pathsToMatch(
                        "/api/auth",
                        "/api/auth/**",

                        "/api/users",
                        "/api/users/**",

                        "/api/user-scopes",
                        "/api/user-scopes/**",

                        "/api/audit-logs",
                        "/api/audit-logs/**"
                )
                .build();
    }


    @Bean
    public GroupedOpenApi group2OriginationApi() {
        return GroupedOpenApi.builder()
                .group("02-sme-origination")
                .displayName("2. SME Onboarding & Loan Origination - Dileep")
                .pathsToMatch(
                        "/api/sme-businesses",
                        "/api/sme-businesses/**",

                        "/api/loan-products",
                        "/api/loan-products/**",

                        "/api/loan-applications",
                        "/api/loan-applications/*",

                        "/api/loan-applications/*/submit",
                        "/api/loan-applications/*/assign",
                        "/api/loan-applications/*/status",

                        "/api/loan-applications/*/documents",
                        "/api/loan-applications/*/documents/*",
                        "/api/loan-applications/*/documents/*/verify",
                        "/api/loan-applications/*/documents/*/flag-deficient",
                        "/api/loan-applications/*/documents/*/reject"
                )
                .build();
    }


    @Bean
    public GroupedOpenApi group3CreditAnalysisApi() {
        return GroupedOpenApi.builder()
                .group("03-credit-analysis-scorecard")
                .displayName("3. Credit Analysis & Scorecard - Subishka")
                .pathsToMatch(
                        "/api/scorecards",
                        "/api/scorecards/**",

                        "/api/loan-applications/*/financial-statements",
                        "/api/loan-applications/*/financial-statements/*",
                        "/api/loan-applications/*/financial-statements/*/verify",

                        "/api/loan-applications/*/credit-proposals",
                        "/api/loan-applications/*/credit-proposals/*",
                        "/api/loan-applications/*/credit-proposals/*/submit",

                        "/api/credit-proposals",
                        "/api/credit-proposals/**"
                )
                .build();
    }

    /*
     * Module 4 — Facility, Disbursement & Repayment
     * Owner: Affrina
     *
     * Controllers:
     * - CollateralFacilityController
     * - RepaymentController
     * - MakerCheckerController
     */
    @Bean
    public GroupedOpenApi group4FacilityRepaymentApi() {
        return GroupedOpenApi.builder()
                .group("04-facility-disbursement-repayment")
                .displayName("4. Facility, Disbursement & Repayment - Affrina")
                .pathsToMatch(
                        "/api/loan-applications/*/collaterals",
                        "/api/loan-applications/*/collaterals/*",
                        "/api/loan-applications/*/collaterals/*/status",
                        "/api/loan-applications/*/collateral-coverage",
                        "/api/loan-applications/*/collaterals/*/revalue",
                        "/api/loan-applications/*/collaterals/*/revaluations",

                        "/api/facilities",
                        "/api/facilities/*",
                        "/api/facilities/business/*",
                        "/api/facilities/*/status",
                        "/api/facilities/expiring",
                        "/api/facilities/*/renew",
                        "/api/facilities/*/renewal-history",

                        "/api/facilities/*/drawdowns",
                        "/api/facilities/*/drawdowns/*/approve",
                        "/api/facilities/*/drawdowns/*/disburse",
                        "/api/facilities/*/drawdowns/*/repay",
                        "/api/facilities/*/drawdowns/*/overdue",

                        "/api/facilities/*/utilisation",
                        "/api/facilities/*/utilisation/*",

                        "/api/repayments",
                        "/api/repayments/**",

                        "/api/maker-checker",
                        "/api/maker-checker/**"
                )
                .build();
    }

    /*
     * Module 5 — Risk Monitoring & Portfolio
     * Owner: Harshit
     *
     * Controllers:
     * - NPAController
     * - CovenantTemplateController
     * - CovenantNotificationController
     * - PortfolioController
     */
    @Bean
    public GroupedOpenApi group5RiskPortfolioApi() {
        return GroupedOpenApi.builder()
                .group("05-risk-monitoring-portfolio")
                .displayName("5. Risk Monitoring & Portfolio - Harshit")
                .pathsToMatch(
                        "/api/npa",
                        "/api/npa/**",

                        "/api/covenant-templates",
                        "/api/covenant-templates/**",

                        "/api/facilities/*/covenants",
                        "/api/facilities/*/covenants/*",
                        "/api/facilities/*/covenants/*/status",
                        "/api/facilities/*/covenants/*/waive",

                        "/api/covenants/*/tracking",

                        "/api/facilities/*/ews",
                        "/api/facilities/*/ews/**",

                        "/api/ews",
                        "/api/ews/**",

                        "/api/notifications",
                        "/api/notifications/**",

                        "/api/portfolio",
                        "/api/portfolio/**"
                )
                .build();
    }
}