
---
name: testing-and-review
description: Reviews implementation quality and verifies tests, lint, builds, API behavior and regressions before committing code.
---

# Testing and Code Review

## Before Commit

1. Inspect all changed files.
2. Inspect the complete Git diff.
3. Check for unrelated changes.
4. Check for exposed credentials.
5. Check error handling.
6. Check potential regressions.

## Frontend Checks

- TypeScript compilation.
- ESLint.
- Unit tests.
- Production build.
- Responsive UI verification.

## Backend Checks

- Maven tests.
- Maven clean verify.
- API response validation.
- Database mapping verification.
- Security and authorization checks.

## Review Rules

- Do not suppress errors without justification.
- Do not remove tests to make builds pass.
- Do not modify unrelated files.
- Report failed tests honestly.

## Completion

Only mark a task complete after all required checks
have been executed and their results reported.
