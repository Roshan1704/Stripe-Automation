# Stripe Automation Framework (Java + Selenium + REST Assured + Cucumber + TestNG)

## Modules
- **UI Automation**: Stripe Dashboard UI workflows with Selenium + Cucumber + POM
- **API Automation**: Stripe PaymentIntent and Refund flows with REST Assured
- **Webhook Validation**: Local Java webhook receiver for signature, duplicate, and retry checks

## Project Structure
```text
stripe-automation-framework/
├── .github/
│   └── workflows/
│       └── ci.yml                         # GitHub Actions pipeline
├── src/
│   ├── main/
│   │   ├── java/com/stripe/automation/
│   │   │   ├── api/
│   │   │   │   ├── client/
│   │   │   │   │   └── StripeApiClient.java
│   │   │   │   ├── models/
│   │   │   │   │   ├── PaymentIntentRequest.java
│   │   │   │   │   └── RefundRequest.java
│   │   │   │   └── spec/
│   │   │   │       └── ApiSpecifications.java
│   │   │   ├── base/
│   │   │   │   └── BaseUiPage.java
│   │   │   ├── config/
│   │   │   │   └── ConfigManager.java
│   │   │   ├── drivers/
│   │   │   │   └── WebDriverFactory.java
│   │   │   ├── ui/pages/
│   │   │   │   ├── LoginPage.java
│   │   │   │   └── PaymentsPage.java
│   │   │   ├── utils/
│   │   │   │   ├── CorrelationId.java
│   │   │   │   ├── ScreenshotUtils.java
│   │   │   │   ├── TestDataLoader.java
│   │   │   │   └── WaitUtils.java
│   │   │   └── webhook/
│   │   │       └── WebhookReceiverServer.java
│   │   └── resources/
│   │       └── config/
│   │           ├── dev.properties
│   │           ├── test.properties
│   │           └── stage.properties
│   └── test/
│       ├── java/com/stripe/automation/
│       │   ├── api/tests/
│       │   │   ├── StripeApiTests.java
│       │   │   └── WebhookValidationTests.java
│       │   ├── base/
│       │   │   └── BaseUiTest.java
│       │   ├── listeners/
│       │   │   ├── RetryAnalyzer.java
│       │   │   └── TestListener.java
│       │   ├── hooks/
│       │   │   └── CucumberHooks.java
│       │   ├── runners/
│       │   │   └── CucumberUiTestRunner.java
│       │   └── ui/stepdefinitions/
│       │       └── StripeDashboardSteps.java
│       └── resources/
│           ├── config/
│           │   ├── dev.properties
│           │   ├── test.properties
│           │   └── stage.properties
│           ├── features/
│           │   └── stripe_dashboard.feature
│           ├── schemas/
│           │   └── payment-intent-schema.json
│           ├── testdata/
│           │   └── payment-data.json
│           └── logback-test.xml
├── Dockerfile
├── pom.xml
├── testng.xml
└── README.md
```

## Directory Responsibilities
- `base/`: Shared abstractions for UI pages/tests.
- `drivers/`: Browser lifecycle and cross-browser driver configuration.
- `utils/`: Waits, screenshots, test data loading, correlation IDs.
- `api/`: API models, request specification builders, and Stripe API client.
- `ui/pages/`: POM classes for Stripe dashboard interactions (Login, DashboardHome, Payments, Customers, Refunds, Disputes).
- `ui/stepdefinitions/`: Cucumber glue code mapping features to page actions.
- `runners/`: TestNG+Cucumber runner classes.
- `hooks/`: Cucumber hooks for setup/teardown.
- `config/`: Environment-specific execution properties.
- `testdata/`: Input datasets for data-driven testing.
- `schemas/`: JSON schemas for response validation.

## Eclipse / STS Import (Fix for Maven nesting error)
If Eclipse shows errors like:
- `Cannot nest .../src/main/resources inside .../src`

the project was previously imported as a plain Java project. Use Maven import instead:

1. Delete old Eclipse metadata (`.project`, `.classpath`, `.settings`) if present.
2. In Eclipse: **File → Import → Maven → Existing Maven Projects**.
3. Select repository root (the folder containing `pom.xml`) and finish import.
4. Run **Right click project → Maven → Update Project**.
5. Ensure Project SDK/JRE is Java 17.

This repository no longer tracks legacy Eclipse Java-project metadata, so m2e can generate the correct Maven source folders (`src/main/java`, `src/test/java`, `src/main/resources`, `src/test/resources`).

## Setup
1. Java 17+, Maven 3.9+
2. Export secrets:
```bash
export STRIPE_SECRET_KEY=sk_test_xxx
export STRIPE_DASHBOARD_EMAIL=your-email
export STRIPE_DASHBOARD_PASSWORD=your-password
export STRIPE_WEBHOOK_SECRET=whsec_xxx
```

## Run tests
```bash
mvn clean test -Ptest
mvn test -Denv=stage -Dbrowser=firefox -Dheadless=true
```

## Stripe CLI (Webhook)
```bash
stripe login
stripe listen --forward-to localhost:9090/stripe/webhook
stripe trigger payment_intent.succeeded
stripe events resend evt_xxx --webhook-endpoint=we_xxx
```

## Containerized Execution (Docker + Compose)

### Build image
```bash
docker build -t stripe-automation:latest .
```

### Run deterministic unit test suite (recommended first gate)
```bash
docker compose run --rm qa-unit
```

### Run full API/Webhook/UI suite (requires Stripe secrets)
```bash
docker compose run --rm qa-api-webhook
```

### Run UI against Selenium Grid container
```bash
docker compose --profile ui up --build --abort-on-container-exit qa-ui
```

## QA Architecture Upgrades
- Added a **300+ API validation matrix** test (`StripeRequestValidationMatrixTests`) through TestNG DataProvider permutations for production-style negative/positive pre-flight validation.
- Added explicit **UI testcase class** (`StripeDashboardUiTestCases`) that exercises Login + Dashboard modules using page objects.
- Added a **container-first workflow** with reusable image layers and suite-based execution.
- Added **remote Selenium support** via `SELENIUM_REMOTE_URL` for scalable UI runs in CI/CD.
- Added a **unit-quality gate** (`testng-unit.xml`) to validate framework internals before expensive integration tests.
- Added **10+ focused unit-level assertions** across config resolution, webhook signature integrity, duplicate event handling, JSON data loading, and correlation ID lifecycle.

## Coverage
- Payment intent create/confirm/success/failure
- Full + partial + duplicate + over-refund checks
- Idempotency + invalid/expired API key + rate-limit behavior
- Webhook signature tampering and duplicate event handling
- UI: login, payments validation, search, refund status, filter & pagination

## Reporting
- Allure results: `target/allure-results`
- Allure report: `mvn allure:report`
- Failure screenshots: `target/screenshots`

## CI/CD
GitHub Actions workflow builds, runs API/Webhook/UI, generates Allure, and archives artifacts.

## Troubleshooting CI Compilation
If CI reports errors like `package org.testng does not exist` from `src/main/java/com/stripe/automation/listeners/*`:
- Ensure listener classes (`RetryAnalyzer`, `TestListener`) are only under `src/test/java/com/stripe/automation/listeners`.
- Do not place TestNG/Allure listener implementations in `src/main/java`.
- The CI workflow now includes a guard step that checks tracked `src/main/java/com/stripe/automation/listeners/*.java` files and fails fast if found.
