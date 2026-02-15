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
- `ui/pages/`: POM classes for Stripe dashboard interactions.
- `ui/stepdefinitions/`: Cucumber glue code mapping features to page actions.
- `runners/`: TestNG+Cucumber runner classes.
- `hooks/`: Cucumber hooks for setup/teardown.
- `config/`: Environment-specific execution properties.
- `testdata/`: Input datasets for data-driven testing.
- `schemas/`: JSON schemas for response validation.

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
