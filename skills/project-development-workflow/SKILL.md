
---
name: project-development-workflow
description: Coordinates the complete software development lifecycle from project analysis and planning to implementation, testing, atomic commits and GitHub pushes.
---

# Project Development Workflow

This skill coordinates the development process.

## Phase 1: Analyze

- Read project documentation.
- Inspect the source code.
- Understand existing architecture.
- Inspect Git branch and status.
- Identify relevant skills.

Do not modify files during initial analysis.

## Phase 2: Plan

- Break work into small independent tasks.
- Identify files to create or modify.
- Define acceptance criteria.
- Identify required tests.

## Phase 3: Design

For frontend tasks:
- Follow ui-design.
- Follow react-architecture.

For backend tasks:
- Follow java-spring-architecture.

## Phase 4: Implement

- Implement one logical task at a time.
- Reuse existing code when appropriate.
- Avoid unrelated refactoring.
- Preserve existing functionality.

## Phase 5: Verify

Follow testing-and-review.

Do not continue to commit if required checks fail.

## Phase 6: Commit

Follow git-workflow.

- Stage only relevant files.
- Create atomic commits.
- Use Conventional Commits.

## Phase 7: Push

- Verify repository and branch.
- Push to the authorized feature branch.
- Never force push.
- Never push directly to main without authorization.

If push fails, preserve local commits and report the error.

## Phase 8: Report

Summarize:
- Completed tasks.
- Files changed.
- Tests and results.
- Commit hashes.
- GitHub push status.
- Remaining work.
