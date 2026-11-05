
---
name: java-spring-architecture
description: Enforces clean Java and Spring Boot backend architecture, layered responsibilities, REST API conventions, security, validation and testing.
---

# Java Spring Boot Architecture

## 1. General Rules

- Follow the existing Java version.
- Follow Spring Boot conventions.
- Use meaningful class and method names.
- Apply SOLID principles where appropriate.
- Avoid unnecessary abstractions.

## 2. Layered Architecture

controller/
  Handle HTTP requests and responses.

service/
  Implement business logic.

repository/
  Handle database operations.

entity/
  Represent database entities.

dto/
  Define API request and response models.

config/
  Application configurations.

exception/
  Centralized exception handling.

security/
  Authentication and authorization.

## 3. Responsibilities

Controller:
- Validate and receive requests.
- Call service methods.
- Return appropriate HTTP responses.
- Do not contain business logic.

Service:
- Handle business rules.
- Coordinate repositories.
- Manage transactions where necessary.

Repository:
- Handle database queries.
- Use Spring Data JPA appropriately.

DTO:
- Never expose sensitive entity fields.
- Separate request and response models.
- Validate incoming data.

## 4. Database

- Use JPA mappings correctly.
- Avoid unnecessary eager loading.
- Consider transaction boundaries.
- Avoid N+1 query problems.
- Never modify production data without approval.

## 5. Security

- Enforce authorization on protected endpoints.
- Never expose passwords or secrets.
- Validate input.
- Follow existing authentication architecture.
- Do not disable security to make tests pass.

## 6. Error Handling

- Use centralized exception handling.
- Return consistent API response structures.
- Use appropriate HTTP status codes.
- Do not expose internal stack traces.

## 7. Testing

- Write unit tests for business logic.
- Use Mockito when appropriate.
- Test controller behavior.
- Run Maven tests and build.
- Never claim tests passed without execution.
