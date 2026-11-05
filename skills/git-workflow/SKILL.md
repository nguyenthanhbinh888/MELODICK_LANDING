---
name: git-workflow
description: Manages Git branches, atomic commits, Conventional Commits, safe staging and GitHub pushes.
---

# Git Workflow

## 1. Before Working

Always execute:

git status
git branch --show-current
git remote -v

- Identify the current branch.
- Never overwrite uncommitted user changes.
- Never discard unrelated changes.
- Never switch branches without considering uncommitted work.

## 2. Branch Management – Strict Naming Convention

### 2.1. Main Branches

The project must use the following main branches:

- main: Stable production-ready code.
- develop: Integration branch for completed and verified features.

Never use phase names as Git branch names.

Forbidden examples:
- phase-1
- phase-2
- phase-3
- development-phase-1
- frontend-phase-1
- backend-phase-2

A development phase is a project management concept, not a branch naming convention.

### 2.2. Feature Branches

Use the following naming conventions:

- feature/<feature-name>: New functionality.
- fix/<bug-name>: Bug fixes.
- hotfix/<issue-name>: Urgent production fixes.
- refactor/<module-name>: Code refactoring.
- docs/<document-name>: Documentation changes.
- test/<module-name>: Testing-related changes.
- chore/<task-name>: Configuration and maintenance.

### 2.3. Naming Rules

Branch names must:

- Use lowercase English.
- Use kebab-case.
- Clearly describe the feature, module or task.
- Be concise and meaningful.
- Avoid spaces, special characters and vague names.
- Never use phase numbers as the primary branch identifier.

Good examples:

feature/weekly-planner
feature/algorithm-lab
feature/english-corner
feature/vocabulary-management
feature/dashboard
fix/schedule-validation
refactor/api-service
docs/project-documentation
chore/frontend-setup

Bad examples:

phase-1
phase-2
new-branch
test-branch
update-code
frontend-work
feature-1

### 2.4. Branch Creation Rules

- Create a dedicated feature branch for each independent feature or task group.
- Create feature branches from develop.
- Merge completed and verified feature branches into develop only according to the project's approved integration workflow.
- Do not create a new branch for every individual commit.
- Multiple atomic commits may belong to the same feature branch.
- Do not create branches unnecessarily.
- Before creating a branch, inspect existing local and remote branches to avoid duplicate branches.

### 2.5. Branch Switching and Protection

- Never switch branches while uncommitted changes may be lost.
- Never delete a branch without checking its status and asking for permission when deletion could discard work.
- Never directly develop features on main.
- Never push directly to main or develop without explicit authorization.
- Never rename or overwrite existing branches without permission.

### 2.6. Branch Name Selection

Before creating a branch:

1. Identify the actual implementation task.
2. Select the appropriate branch prefix.
3. Generate a concise descriptive name.
4. Check whether the branch already exists.
5. Create the branch from the correct base branch.
6. Report the branch name to the user.

If a task belongs to an existing feature branch, continue using that branch instead of creating unnecessary branches.

## 3. Atomic Commits – Strict Commit Granularity

The primary objective is to maintain a highly detailed, traceable and meaningful Git history.

### 3.1. One commit = One independently understandable logical change

Each commit must represent ONE specific implementation task.

A commit must not contain multiple unrelated features, modules or implementation tasks.

Do not interpret an entire phase, an entire module, or an entire frontend/backend initialization as one logical change.

For example, NEVER create one large commit containing:
- React project initialization.
- Global styling and design tokens.
- Main layout.
- Sidebar and header.
- Router configuration.
- Authentication context.
- API services.
- Multiple application pages.

These must be separated into smaller commits.

### 3.2. Required Commit Granularity

Break implementation work into the smallest meaningful and independently understandable changes.

Example: Frontend initialization:

1. chore(frontend): initialize React TypeScript project with Vite
2. chore(frontend): configure Tailwind CSS and PostCSS
3. feat(frontend): define global design tokens
4. feat(frontend): establish frontend directory structure
5. feat(layout): implement application main layout
6. feat(layout): implement sidebar navigation
7. feat(layout): implement application header
8. feat(router): configure application routes
9. feat(auth): implement authentication context
10. feat(api): configure Axios HTTP client
11. feat(types): define shared TypeScript interfaces

Example: Vocabulary management:

1. feat(english): define vocabulary data types
2. feat(english): implement vocabulary list component
3. feat(english): implement vocabulary creation form
4. feat(english): implement vocabulary editing form
5. feat(english): implement vocabulary deletion
6. feat(english): add vocabulary search and filtering
7. test(english): add vocabulary management tests

These examples illustrate the expected level of granularity. Adapt them to the actual project structure.

### 3.3. File Grouping Rules

Multiple files may belong to the same commit ONLY when they directly contribute to the same logical change.

For example, a Sidebar commit may include:
- Sidebar.tsx
- Sidebar.css
- navigation.ts
- Directly related icons or helper components.

Do not include unrelated Dashboard, Authentication or API Service files.

A single file containing multiple unrelated changes must be staged selectively using git add -p or an equivalent method.

### 3.4. Commit Size Guidelines

Use these guidelines to evaluate commit scope:

- Prefer 1–5 closely related files per commit.
- A commit may contain more files when they are all necessary for one coherent change.
- Avoid commits containing dozens of files.
- Avoid commits combining multiple independent features.
- Do not artificially split trivial changes when doing so would make the project history harder to understand.

These are guidelines, not rigid numerical limits.

### 3.5. Commit Before Moving to Another Independent Task

After completing and validating an independent implementation task:

1. Inspect the changes.
2. Stage only related files.
3. Create a detailed commit.
4. Verify the commit.
5. Continue to the next independent task.

Do not wait until an entire module or development phase is completed before committing.

Do not create empty commits or meaningless commits just to increase commit count.

## 4. Conventional Commits – Detailed Commit Messages

Use the following format:

type(scope): specific description

### 4.1. Subject Requirements

The subject must:
- Identify the affected module.
- Describe the exact implementation or correction.
- Be specific enough to understand without opening the source code.
- Use clear English.
- Avoid vague descriptions.

BAD examples:

feat(frontend): initialize frontend
feat(frontend): update UI
feat(planner): improve functionality
chore: update project
fix: fix bugs

GOOD examples:

feat(planner): add weekly schedule creation form
feat(planner): implement schedule editing modal
fix(planner): prevent overlapping schedule entries
feat(english): add vocabulary search by keyword
feat(layout): implement responsive sidebar navigation
chore(frontend): configure TypeScript path aliases
test(auth): add login service unit tests

### 4.2. Mandatory Commit Body

Every commit MUST contain a detailed commit body.

The body must include these sections:

Changes:
- Describe exactly what was implemented.
- Explain the behavior or logic added or modified.

Files changed:
- List important files created, modified or deleted.
- Explain the responsibility of each file or file group.

Reason:
- Explain why this change is necessary.

Verification:
- List actual tests, lint and build commands executed.
- Report their actual results.
- Clearly identify checks that were not performed.

### 4.3. Example

Subject:

feat(planner): add weekly schedule creation form

Body:

Changes:
- Implement a form for creating weekly schedule entries.
- Add title, weekday, start time and end time fields.
- Validate required fields.
- Handle form submission and validation errors.

Files changed:
- WeeklyScheduleForm.tsx: implements the schedule creation form.
- WeeklyScheduleForm.css: defines form styling.
- schedule.types.ts: defines schedule-related TypeScript types.

Reason:
- Allow users to create personal weekly schedule entries.

Verification:
- TypeScript build: passed.
- ESLint: passed.
- Manual form validation: passed.

### 4.4. Commit Message Restrictions

- Never create a commit with only a vague one-line description.
- Never claim that tests passed without actually executing them.
- Never list files that are not included in the commit.
- Never combine unrelated tasks in one commit body.
- Do not fabricate verification results.

Use git commit with a descriptive body, for example:

git commit -m "feat(planner): add weekly schedule creation form" -m "Changes:
- Implement schedule creation form.
- Add field validation.

Files changed:
- WeeklyScheduleForm.tsx
- WeeklyScheduleForm.css

Reason:
- Allow users to create weekly schedules.

Verification:
- TypeScript build: passed."

Adapt the body to the actual changes.

## 5. Staging

- Inspect git diff before staging.
- Stage only files related to the task.
- Never use git add . blindly.
- Never commit .env files, secrets or credentials.

## 6. Validation

Before committing:

- Run relevant tests.
- Run lint.
- Run production build.
- Inspect staged diff.
- Check for unintended file changes.

## 7. Automatic GitHub Push

After a feature is fully implemented and verified:

- Push must preserve all individual atomic commits.
- Never squash multiple implementation tasks into one commit.
- Do not use git commit --amend to rewrite previously published commits.
- Do not wait until an entire module is completed before creating commits.
- Push may happen after a logical group of tasks is verified, but every task must retain its own commit.

1. Check git status.
2. Check current branch.
3. Check git remote.
4. Inspect staged changes.
5. Create atomic commits.
6. Push commits to the authorized feature branch.
7. Verify push result.

Push policy:

- Automatically push completed work to the current authorized feature branch.
- Do not push directly to main or develop.
- Never force push.
- Never overwrite remote history.
- Never push secrets or environment files.
- If the branch is protected, stop and report.
- If push fails, preserve local commits.
- Do not retry destructive operations automatically.

Report every commit hash and push result.

## 8. Final Report

After completing a task, provide a detailed Git report.

For EVERY commit, report:

1. Full commit hash.
2. Commit subject.
3. Commit body.
4. Files added.
5. Files modified.
6. Files deleted.
7. Verification results.
8. Push status.

Also provide:
- Total number of commits created.
- Current branch.
- Remote repository.
- Remote branch.
- Remaining uncommitted changes.

Do not summarize multiple commits into one generic description.

The report must allow the user to understand exactly what changed in every commit without manually inspecting the entire source code.

## 9. Command Execution Permissions (Phân quyền thực thi lệnh)

Bắt buộc tuân thủ chính sách phân quyền cho các lệnh Git sau:

| Lệnh | Quyền đề xuất | Quy định thực thi |
| :--- | :--- | :--- |
| `git status` | Allow | Tự động cho phép chạy |
| `git diff` | Allow | Tự động cho phép chạy |
| `git branch` | Allow | Tự động cho phép chạy |
| `git add` | Allow | Tự động cho phép chạy |
| `git commit` | Allow | Tự động cho phép chạy |
| `git push` | Allow sau khi kiểm tra branch | Chỉ cho phép chạy sau khi đã kiểm tra và xác nhận đúng branch |
| `git reset --hard` | Ask | Bắt buộc hỏi xác nhận từ người dùng trước khi thực thi |
| `git clean -fd` | Ask | Bắt buộc hỏi xác nhận từ người dùng trước khi thực thi |
| `git push --force` | Deny | Bị cấm tuyệt đối, không được thực thi dưới bất kỳ hình thức nào |
| `git merge` | Ask | Bắt buộc hỏi xác nhận từ người dùng trước khi thực thi |
