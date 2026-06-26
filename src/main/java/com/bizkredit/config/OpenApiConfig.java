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

// Configuration class for Swagger OpenAPI documentation
@Configuration
public class OpenApiConfig {

    // Name used to define JWT Bearer authentication scheme in Swagger
    private static final String SCHEME_NAME = "bearerAuth";

    // Global OpenAPI configuration
    @Bean
    public OpenAPI bizKreditOpenAPI() {
        return new OpenAPI()

                // Basic API metadata shown in Swagger UI
                .info(new Info()
                        .title("BizKredit API")
                        .description("SME Business Loan & Working Capital Platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BizKredit Team")
                                .email("support@bizkredit.com"))
                        .license(new License()
                                .name("Proprietary")))

                // Apply security (JWT Bearer) globally for all APIs
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME))

                // Define security scheme (Authorization header with Bearer JWT)
                .components(new Components()
                        .addSecuritySchemes(SCHEME_NAME, new SecurityScheme()
                                .name(SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the JWT returned by POST /api/auth/login")));
    }

    /*
     * Group 00: All APIs (used for full list view)
     */
    @Bean
    public GroupedOpenApi allApisApi() {
        return GroupedOpenApi.builder()
                .group("00-all")
                .displayName("All APIs")
                .pathsToMatch("/api/**") // includes all endpoints
                .build();
    }

    /*
     * Group 01: Authentication, User Management, Scope, and Audit APIs
     * Owner: Harish
     */
    @Bean
    public GroupedOpenApi group1AuthUsersScopeAuditApi() {
        return GroupedOpenApi.builder()
                .group("01-auth-users-scope-audit")
                .displayName("1. Auth, Users, Scope & Audit - Harish")
                .pathsToMatch(
                        // Auth APIs
                        "/api/auth",
                        "/api/auth/**",

                        // User management APIs
                        "/api/users",
                        "/api/users/**",

                        // User scope/permission APIs
                        "/api/user-scopes",
                        "/api/user-scopes/**",

                        // Audit log APIs
                        "/api/audit-logs",
                        "/api/audit-logs/**"
                )
                .build();
    }

    /*
     * Group 02: SME onboarding and loan origination APIs
     * Owner: Dileep
     */
    @Bean
    public GroupedOpenApi group2OriginationApi() {
        return GroupedOpenApi.builder()
                .group("02-sme-origination")
                .displayName("2. SME Onboarding & Loan Origination - Dileep")
                .pathsToMatch(
                        // SME business APIs
                        "/api/sme-businesses",
                        "/api/sme-businesses/**",

                        // Loan product APIs
                        "/api/loan-products",
                        "/api/loan-products/**",

                        // Loan application APIs
                        "/api/loan-applications",
                        "/api/loan-applications/*",

                        // Application lifecycle actions
                        "/api/loan-applications/*/submit",
                        "/api/loan-applications/*/assign",
                        "/api/loan-applications/*/status",

                        // Document management APIs
                        "/api/loan-applications/*/documents",
                        "/api/loan-applications/*/documents/*",

                        // Document actions
                        "/api/loan-applications/*/documents/*/verify",
                        "/api/loan-applications/*/documents/*/flag-deficient",
                        "/api/loan-applications/*/documents/*/reject"
                )
                .build();
    }

    /*
     * Group 03: Credit analysis and scoring APIs
     * Owner: Subishka
     */
    @Bean
    public GroupedOpenApi group3CreditAnalysisApi() {
        return GroupedOpenApi.builder()
                .group("03-credit-analysis-scorecard")
                .displayName("3. Credit Analysis & Scorecard - Subishka")
                .pathsToMatch(
                        // Scorecard APIs
                        "/api/scorecards",
                        "/api/scorecards/**",

                        // Financial statement APIs
                        "/api/loan-applications/*/financial-statements",
                        "/api/loan-applications/*/financial-statements/*",
                        "/api/loan-applications/*/financial-statements/*/verify",

                        // Credit proposal APIs
                        "/api/loan-applications/*/credit-proposals",
                        "/api/loan-applications/*/credit-proposals/*",
                        "/api/loan-applications/*/credit-proposals/*/submit",

                        // Global proposal APIs
                        "/api/credit-proposals",
                        "/api/credit-proposals/**"
                )
                .build();
    }

    /*
     * Group 04: Facility management, disbursement, and repayment APIs
     * Owner: Affrina
     */
    @Bean
    public GroupedOpenApi group4FacilityRepaymentApi() {
        return GroupedOpenApi.builder()
                .group("04-facility-disbursement-repayment")
                .displayName("4. Facility, Disbursement & Repayment - Affrina")
                .pathsToMatch(
                        // Collateral APIs
                        "/api/loan-applications/*/collaterals",
                        "/api/loan-applications/*/collaterals/*",
                        "/api/loan-applications/*/collaterals/*/status",
                        "/api/loan-applications/*/collateral-coverage",
                        "/api/loan-applications/*/collaterals/*/revalue",
                        "/api/loan-applications/*/collaterals/*/revaluations",

                        // Facility APIs
                        "/api/facilities",
                        "/api/facilities/*",
                        "/api/facilities/business/*",
                        "/api/facilities/*/status",
                        "/api/facilities/expiring",
                        "/api/facilities/*/renew",
                        "/api/facilities/*/renewal-history",

                        // Drawdown APIs
                        "/api/facilities/*/drawdowns",
                        "/api/facilities/*/drawdowns/*/approve",
                        "/api/facilities/*/drawdowns/*/disburse",
                        "/api/facilities/*/drawdowns/*/repay",
                        "/api/facilities/*/drawdowns/*/overdue",

                        // Utilisation APIs
                        "/api/facilities/*/utilisation",
                        "/api/facilities/*/utilisation/*",

                        // Repayment APIs
                        "/api/repayments",
                        "/api/repayments/**",

                        // Maker-checker workflow APIs
                        "/api/maker-checker",
                        "/api/maker-checker/**"
                )
                .build();
    }

    /*
     * Group 05: Risk monitoring and portfolio management APIs
     * Owner: Harshit
     */
    @Bean
    public GroupedOpenApi group5RiskPortfolioApi() {
        return GroupedOpenApi.builder()
                .group("05-risk-monitoring-portfolio")
                .displayName("5. Risk Monitoring & Portfolio - Harshit")
                .pathsToMatch(
                        // NPA (Non-Performing Assets) APIs
                        "/api/npa",
                        "/api/npa/**",

                        // Covenant template APIs
                        "/api/covenant-templates",
                        "/api/covenant-templates/**",

                        // Covenant management APIs
                        "/api/facilities/*/covenants",
                        "/api/facilities/*/covenants/*",
                        "/api/facilities/*/covenants/*/status",
                        "/api/facilities/*/covenants/*/waive",

                        // Covenant tracking APIs
                        "/api/covenants/*/tracking",

                        // Early Warning Signals (EWS) APIs
                        "/api/facilities/*/ews",
                        "/api/facilities/*/ews/**",
                        "/api/ews",
                        "/api/ews/**",

                        // Notification APIs
                        "/api/notifications",
                        "/api/notifications/**",

                        // Portfolio APIs
                        "/api/portfolio",
                        "/api/portfolio/**"
                )
                .build();
    }
}