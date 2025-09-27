# AGENT INSTRUCTIONS

## Project Context
- **Stack**: Spring Boot 3 (Java 21) REST API that exposes translation endpoints backed by AWS Bedrock Runtime. DTOs live under `sample_bedrock.translate.dto`, the REST controller is in `controller`, and business logic is under `service`.
- **Configuration**: OpenAPI documentation is configured via `sample_bedrock.translate.config.OpenApiConfig`. Global exception handling lives in `exception` package.
- **Testing**: Unit and integration tests run through Maven (`./mvnw test`). Use the wrapper already committed to guarantee the expected plugin versions.

## Conventions
- Keep packages organized under `sample_bedrock.translate`. Place new controllers under `controller`, services under `service`, DTOs under `dto`, and configuration classes under `config`.
- Prefer constructor injection (via Lombok is not configured; write constructors manually or use `@RequiredArgsConstructor` only if Lombok is added).
- Follow Spring Boot naming conventions for beans and request mappings. Validate request payloads with Jakarta Validation annotations when applicable.
- Use `record` for simple immutable DTOs that just carry data, aligning with existing `TranslateRequest` and `TranslateResponse` patterns.

## Development Workflow
1. Review existing test coverage under `src/test/java` before adding new features. Mirror the package structure of the main source set when creating new tests.
2. Run `./mvnw test` locally before committing changes. Add new tests whenever you add new business logic.
3. Update OpenAPI annotations (`@Operation`, `@Schema`, etc.) if you introduce new endpoints or change request/response models.

## Documentation & PR Notes
- If you modify request/response DTOs or external API behavior, update README or relevant docs (create them if they do not exist yet) to describe the change.
- Keep commit messages and PR descriptions in English.
- When referencing AWS Bedrock functionality, avoid hard-coding credentials or region defaults; rely on environment configuration.
