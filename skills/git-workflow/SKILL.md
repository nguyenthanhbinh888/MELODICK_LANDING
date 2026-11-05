---
name: git-workflow
description: >
  Enforces a safe, traceable Git Flow for the MELODICK RECS. website.
  Manages feature branches, small atomic commits, detailed Conventional Commits,
  validation, and safe GitHub pushes across the React/TypeScript frontend,
  Java Spring Boot backend, and MySQL database.
---

# Git Workflow — MELODICK RECS.

## 1. Purpose and Project Context

This skill defines how Git must be used while developing **MELODICK RECS.**, a music-label / artist-discovery landing website with a separate administration area.

The project architecture is expected to contain:

- **Public website:** React, TypeScript, HTML5, and CSS3.
- **Administration website:** a separate Admin interface with its own routes and protected pages.
- **Backend:** Java and Spring Boot REST API, when the backend is part of the repository.
- **Database:** MySQL.
- **Main public features:** Home / Hero, Artists, artist profile links, Submit Demo, social links, footer, and editable website settings.
- **Main Admin features:** authentication, dashboard, artist management, artist platform links, website settings, and other approved CMS functions.

The actual repository structure may differ from these examples. Always inspect the repository before assuming directory names, package managers, build tools, or module boundaries.

### Core objectives

1. Keep the Git history detailed and easy to audit.
2. Make one commit represent one coherent, understandable change.
3. Keep public website, Admin, API, and database work organized.
4. Protect existing user changes and repository history.
5. Push completed work to the correct feature branch on GitHub.
6. Never claim a test, build, commit, or push succeeded unless it was actually executed and verified.

This skill governs Git operations. It does not authorize changing product requirements, redesigning the website, or modifying unrelated code.

---

## 2. Mandatory Pre-work Inspection

Before modifying files or running Git operations that change repository state, execute:

```bash
git status --short --branch
git branch --show-current
git branch --list
git branch --remotes
git remote -v
git log -5 --oneline --decorate
```

Then inspect the project structure and relevant configuration files. Identify, when present:

- Frontend package manager and scripts (`package.json`, lockfile).
- TypeScript configuration (`tsconfig.json`).
- Backend build tool (`pom.xml` or Gradle files).
- Spring Boot application configuration.
- Database migration configuration (Flyway, Liquibase, or the existing migration mechanism).
- Existing `.gitignore` and environment-variable conventions.

### Safety rules

- Identify the current branch before doing any work.
- Never overwrite, discard, reset, or silently reformat uncommitted user changes.
- Never include unrelated user changes in a commit.
- Never switch branches while uncommitted changes could be lost.
- Do not assume the working tree is clean.
- Do not run destructive commands to make the repository appear clean.
- If the repository state is ambiguous, stop the affected Git operation and report the exact issue.
- Do not initialize a new Git repository if one already exists.
- Do not change the configured remote URL without explicit user authorization.

If uncommitted changes exist, inspect `git status` and `git diff` first. Treat changes that were not created for the current task as user-owned. Do not stage or commit them.

---

## 3. Branch Strategy

Use a conventional Git Flow with two long-lived branches:

| Branch | Responsibility | Direct development |
|---|---|---|
| `main` | Stable, production-ready version of MELODICK RECS. | Forbidden |
| `develop` | Integration branch for completed and verified features. | Forbidden for feature development |

Feature work must take place on a dedicated task branch created from the latest appropriate `develop` branch.

If the repository does not contain `develop`, do not silently invent a branch strategy or change the remote default branch. Inspect the available branches and report the situation before creating or pushing an integration branch.

### 3.1. Allowed branch prefixes

Use these prefixes:

| Prefix | Use |
|---|---|
| `feature/` | New website, Admin, API, or database functionality |
| `fix/` | A defect in existing functionality |
| `hotfix/` | An urgent production correction |
| `refactor/` | Internal restructuring without intended behavior changes |
| `docs/` | Documentation changes |
| `test/` | Tests and test infrastructure |
| `chore/` | Tooling, dependencies, configuration, and maintenance |
| `build/` | Build system or packaging changes |
| `perf/` | Performance improvements |

### 3.2. MELODICK RECS. branch examples

Good examples:

```text
feature/public-landing-page
feature/admin-dashboard
feature/admin-authentication
feature/artist-management
feature/artist-social-links
feature/site-settings
feature/artist-api
feature/mysql-schema
feature/demo-submission-management

fix/artist-card-layout
fix/admin-login-validation
fix/artist-link-navigation
fix/responsive-navigation

refactor/artist-service
refactor/frontend-api-client

docs/local-development-setup
test/artist-management
chore/frontend-tooling
chore/backend-dependencies
```

These are examples, not a list of branches that must all be created. Choose a branch only when the corresponding task is actually being implemented.

### 3.3. Forbidden branch names

Never use project phases, vague names, or temporary labels as branch names.

Forbidden examples:

```text
phase-1
phase-2
development-phase-1
frontend-phase-1
backend-phase-2
new-branch
update-code
feature-1
test-branch
```

A development phase is a planning milestone, not a Git branch name.

### 3.4. Branch naming rules

Every branch name must:

- Use lowercase English.
- Use kebab-case.
- Start with one allowed prefix.
- Describe the actual feature, fix, or task.
- Be concise but unambiguous.
- Avoid spaces, personal names, dates, and phase numbers.

### 3.5. Creating and reusing branches

Before creating a branch:

1. Identify the exact task.
2. Check the current branch and working tree.
3. Check local and remote branches for an existing matching branch.
4. Decide whether the current branch already belongs to the task.
5. If a new branch is required, create it from the latest appropriate `develop`.
6. Verify the new branch with `git branch --show-current`.
7. Report the selected branch.

Rules:

- Reuse an existing task branch when it is clearly the correct branch.
- Do not create a new branch for every commit.
- One feature branch may contain multiple atomic commits for the same feature.
- Do not create unnecessary branches for tiny, directly related changes.
- Do not rename, delete, or overwrite an existing branch without authorization.
- Never work directly on `main` or `develop`.

### 3.6. Switching branches

Before switching:

- Check `git status`.
- Inspect all modified and untracked files.
- Confirm that the switch will not lose or misplace work.

Do not automatically stash, reset, clean, or discard changes. Ask the user before using a procedure that moves or temporarily hides their uncommitted work.

---

## 4. Project-aware Commit Organization

A commit must represent one logical change within MELODICK RECS.

Use scopes that describe the affected project area. Recommended scopes include:

| Scope | Responsibility |
|---|---|
| `landing` | Public landing page and Home / Hero sections |
| `layout` | Shared public layout, navigation, header, and footer |
| `artists` | Public artist listing and artist presentation |
| `artist-links` | Spotify, SoundCloud, YouTube, Instagram, TikTok, and other artist links |
| `submit` | Submit Demo link or demo-submission functionality |
| `admin` | Admin layout, dashboard, and shared Admin pages |
| `auth` | Admin login, JWT handling, and authorization |
| `admin-artists` | Artist CRUD screens and Admin artist workflows |
| `admin-settings` | Website settings management in Admin |
| `api` | REST API integration and HTTP client |
| `backend` | Spring Boot application infrastructure |
| `database` | MySQL schema, migrations, and database configuration |
| `security` | Authentication, authorization, and security configuration |
| `config` | Application and environment configuration |
| `test` | Project tests and test utilities |
| `docs` | Project and developer documentation |

Use the most specific meaningful scope. Do not create arbitrary scopes that duplicate existing ones.

### 4.1. Keep layers and responsibilities clear

Avoid one large commit that mixes unrelated changes from:

- Public landing page.
- Admin dashboard.
- Admin authentication.
- Artist CRUD.
- Artist social links.
- Website settings.
- Backend API.
- Database schema.
- Build configuration.

Separate these into coherent commits.

However, do not split a single inseparable change artificially. A feature that requires a database migration, backend endpoint, and frontend integration may use several commits on the same feature branch, with each commit describing one layer or independently understandable step.

### 4.2. Example: introducing the application structure

Use a sequence such as:

```text
chore(frontend): initialize React TypeScript application with Vite
chore(frontend): configure linting and TypeScript compiler options
chore(frontend): establish public and admin source directories
feat(layout): add shared public website layout components
feat(router): configure public landing and admin route groups
chore(backend): initialize Spring Boot REST API module
chore(database): configure MySQL environment-based connection settings
```

Do not combine all frontend, backend, and database initialization into one large commit.

### 4.3. Example: public artist listing

A suitable sequence may be:

```text
feat(artists): define artist and platform link TypeScript types
feat(artists): implement reusable artist card component
feat(artists): render artist cards in responsive artist grid
feat(artist-links): add platform link overlay to artist cards
feat(api): add public artist list API client
feat(artists): load published artists from backend API
test(artists): cover artist list rendering and empty state
```

If the project is still using static data, do not claim that the cards load from the API. Describe the actual implementation.

### 4.4. Example: Admin artist management

A suitable sequence may be:

```text
feat(database): add artist table migration
feat(backend): implement artist entity and repository
feat(backend): add admin endpoint for creating artists
feat(admin-artists): define artist form validation types
feat(admin-artists): implement artist creation form
feat(admin-artists): add artist list with search and status filters
feat(admin-artists): implement artist editing workflow
feat(admin-artists): add artist deletion confirmation
test(admin-artists): cover artist creation and update validation
```

Adapt the sequence to the actual architecture and existing code.

### 4.5. Example: Admin authentication

```text
feat(security): configure Spring Security for admin API routes
feat(auth): implement admin login request and response DTOs
feat(auth): issue JWT access token after valid admin login
feat(auth): configure frontend authenticated session handling
feat(auth): protect admin routes from unauthenticated access
fix(auth): redirect expired admin sessions to login page
test(auth): cover invalid credentials and token validation
```

Never place passwords, JWT secrets, or access tokens in commits.

### 4.6. Example: website settings

```text
feat(database): add persistent website settings schema
feat(backend): add public website settings endpoint
feat(admin-settings): implement editable landing page tagline
feat(admin-settings): add footer social link management
feat(landing): load tagline and footer links from settings API
test(admin-settings): validate website settings updates
```

Do not put actual production credentials or private configuration into the repository.

---

## 5. Atomic Commit Rules

### 5.1. One commit = one coherent logical change

Each commit must:

- Have one clear purpose.
- Be understandable without inspecting unrelated commits.
- Contain only files necessary for that purpose.
- Avoid bundling independent features or refactoring.
- Preserve a buildable or reasonably verifiable project state whenever practical.

Do not interpret an entire development milestone, module, or full-stack feature as one mandatory commit.

### 5.2. Commit granularity

Prefer the smallest meaningful change, not the smallest possible file count.

Good examples:

- One component and its directly related stylesheet.
- One API service and its related TypeScript types.
- One database migration and its directly related migration metadata.
- One Spring Boot service implementation and its focused unit tests.
- One Admin form and its direct validation logic.

A commit may contain more than five files when they are all necessary for the same coherent change. Do not split trivial changes merely to increase the commit count.

### 5.3. Separate implementation from unrelated cleanup

Do not combine a requested feature with unrelated formatting, renaming, dependency updates, or broad refactoring.

When a refactor is necessary for the feature, keep it limited to the affected area and explain it in the commit body.

### 5.4. Commit after each verified task

After completing one independent task:

1. Inspect the diff.
2. Check the files and behavior affected.
3. Run relevant validation.
4. Stage only files belonging to the task.
5. Create a detailed commit.
6. Verify the resulting commit.
7. Continue to the next independent task.

Do not wait until the whole Admin area, API, or development milestone is complete before committing.

Never create empty commits or meaningless commits.

---

## 6. Conventional Commits

Use this format:

```text
type(scope): specific description
```

### 6.1. Allowed commit types

| Type | Meaning |
|---|---|
| `feat` | Add a user-visible or API capability |
| `fix` | Correct a defect |
| `refactor` | Restructure code without intended behavior changes |
| `style` | Formatting or code-style changes that do not affect behavior |
| `test` | Add or update tests |
| `docs` | Documentation changes |
| `chore` | Maintenance and configuration |
| `perf` | Performance improvements |
| `build` | Build or packaging changes |
| `ci` | CI/CD pipeline changes |

### 6.2. Good commit subjects

```text
feat(landing): implement responsive hero section with existing brand styling
feat(artists): display published artists from the public artists endpoint
feat(artist-links): open artist platform profiles in a new browser tab
feat(admin): add dashboard navigation for artist and site settings
feat(admin-artists): add artist form with avatar and biography fields
feat(auth): protect admin routes using the authenticated session
feat(database): add indexes for artist publication status and display order
fix(artists): prevent hidden artists from appearing on the public page
fix(admin-auth): display validation errors for invalid login credentials
refactor(api): centralize frontend HTTP error handling
test(artists): cover empty and populated artist list states
docs(setup): document local MySQL and Spring Boot startup
chore(frontend): configure TypeScript strict mode and import aliases
```

### 6.3. Bad commit subjects

```text
feat(frontend): initialize frontend
feat(admin): update UI
feat(artists): improve functionality
chore: update project
fix: fix bugs
update code
finish admin
```

Avoid vague descriptions such as `update`, `change`, `improve`, `complete`, or `fix bug` without identifying what changed.

### 6.4. Mandatory detailed commit body

Every implementation commit MUST contain a meaningful body with these sections:

```text
Changes:
- Describe the exact behavior, logic, or configuration implemented.
- Mention relevant validation, edge cases, or constraints.

Files changed:
- List important files created, modified, or deleted.
- Explain the responsibility of each file or related file group.

Reason:
- Explain why this change is needed in MELODICK RECS.

Verification:
- List the actual commands and checks executed.
- Report the actual result of each check.
- Clearly state checks that were not run.
```

The body must match the actual staged changes. Do not list files that are not part of the commit.

### 6.5. Example commit

Subject:

```text
feat(admin-artists): add artist creation form with platform links
```

Body:

```text
Changes:
- Implement the Admin form for creating a new artist.
- Add artist name, avatar URL, biography, and publication status fields.
- Add Spotify, SoundCloud, YouTube, and Instagram URL inputs.
- Validate required fields and reject malformed platform URLs.
- Display field-level validation messages before submitting.

Files changed:
- ArtistCreateForm.tsx: implements the artist creation interface.
- ArtistCreateForm.css: defines the form layout and responsive styling.
- artist.types.ts: defines the artist creation request types.
- artist.validation.ts: contains artist and platform link validation rules.

Reason:
- Allow administrators to add artist profiles that can be displayed on the public MELODICK RECS website.

Verification:
- TypeScript type check: passed.
- Frontend production build: passed.
- Manual validation of required fields: passed.
- Backend API integration: not tested in this commit.
```

This is an example only. Use real file names and real verification results.

### 6.6. Commit body restrictions

- Never fabricate test or build results.
- Never claim an API integration works when only the frontend was implemented.
- Never list files that were not staged.
- Never combine unrelated tasks in one body.
- Do not omit important limitations or unverified behavior.
- Do not use a generic one-line commit for implementation work.

Use a detailed `git commit` message with a subject and body. A multiline shell command or an equivalent safe Git interface may be used.

---

## 7. Staging Rules

Before staging:

```bash
git status --short
git diff
git diff --check
```

Then stage only the files required by the current logical change:

```bash
git add path/to/related-file
git add path/to/another-related-file
git diff --cached
```

Rules:

- Never run `git add .` blindly.
- Never stage unrelated user changes.
- Review the staged diff before every commit.
- Use interactive staging when a single file contains unrelated changes.
- Do not commit generated files unless the project explicitly requires them.
- Do not commit dependency folders such as `node_modules/` or Java build output such as `target/`.

### 7.1. Sensitive and local files

Never commit:

```text
.env
.env.*
!.env.example
application-local.yml
application-local.properties
*.pem
*.key
*.p12
*.jks
credentials.json
secrets.*
```

Also inspect for:

- MySQL usernames and passwords.
- Production database URLs.
- JWT signing secrets.
- Cloud storage credentials.
- Admin passwords or private account data.
- API keys and access tokens.

Commit safe example configuration such as `.env.example` only when it contains placeholders and no real secrets.

If a secret is accidentally staged, unstage it and inspect the repository state. If it was already committed or pushed, stop and report the exposure. Do not assume deleting it in a later commit removes it from Git history.

---

## 8. Validation Before Committing

Run checks relevant to the files changed. First inspect the scripts and build tools actually defined by the repository.

### 8.1. React / TypeScript frontend

When the corresponding scripts exist, run the project's applicable commands, for example:

```bash
npm run lint
npm run typecheck
npm run build
```

If the project uses another package manager, use its existing lockfile and equivalent commands (for example `pnpm` or `yarn`).

Check, where relevant:

- TypeScript errors.
- ESLint errors.
- Production build.
- Route behavior for public and Admin areas.
- Responsive behavior.
- API request and error handling.
- Admin route protection.
- Form validation and loading / error states.

Do not invent scripts that are not present in `package.json`.

### 8.2. Java / Spring Boot backend

If the project uses Maven and provides the Maven wrapper:

```bash
./mvnw test
./mvnw package
```

On Windows, use the corresponding `mvnw.cmd` command.

If the project uses Gradle, use the wrapper and the existing tasks, such as:

```bash
./gradlew test
./gradlew build
```

Check, where relevant:

- Java compilation.
- Unit and integration tests.
- REST endpoint behavior.
- Request validation.
- Authentication and authorization.
- Error responses.
- Database repository behavior.

Do not claim integration tests passed when they could not connect to the required database or test environment.

### 8.3. MySQL and database migrations

For database-related changes:

- Inspect the migration mechanism already used by the project.
- Use versioned migrations when Flyway, Liquibase, or an equivalent tool is configured.
- Keep schema changes separate from unrelated application logic where practical.
- Check table names, column types, constraints, indexes, and relationships.
- Check compatibility with the existing entity and repository mappings.
- Validate migrations against a development or test database when available.
- Do not run destructive migration, table deletion, or production database modification without explicit authorization.
- Never commit production database dumps or real user data.

For artist management, changes may affect artist records, publication status, display order, and platform links. Check that the migration and application code agree on these fields.

For website settings, check that persisted settings remain consistent with the public API and Admin forms.

### 8.4. Validation report

If a check cannot be run, write the exact reason, for example:

```text
- Frontend lint: passed.
- TypeScript type check: passed.
- Frontend build: passed.
- Backend tests: not run — MySQL test service is unavailable.
- Database migration: not executed — no development database credentials configured.
```

Never replace a missing check with an assumption.

---

## 9. Automatic GitHub Push Policy

The goal is to preserve every atomic commit while keeping the remote feature branch up to date.

After a task or a coherent group of verified commits is completed, push to the authorized feature branch.

### 9.1. Before pushing

Execute and inspect:

```bash
git status --short --branch
git branch --show-current
git remote -v
git log -5 --oneline
```

Confirm:

- The current branch is the intended feature/fix/docs/chore branch.
- The remote points to the expected GitHub repository.
- The commits belong to the current task.
- No secrets or unrelated files are included.
- The local branch is not `main` or `develop`.

### 9.2. Push rules

- Automatically push completed work to the current authorized task branch.
- Preserve every atomic commit.
- Never squash implementation commits merely to reduce commit count.
- Never rewrite already published commits with `git commit --amend`.
- Never push directly to `main` or `develop`.
- Never force push.
- Never overwrite remote history.
- Never push secrets, local environment files, or generated build artifacts.
- Do not change the remote URL.
- Do not push an unrelated branch simply because it is currently checked out.
- If the branch is protected or the push is rejected, stop and report the error.
- If push fails, preserve local commits and inspect the failure before proposing a safe next step.
- Do not automatically resolve remote conflicts by resetting, rebasing destructively, or overwriting remote work.

For a normal first push of a verified feature branch, use:

```bash
git push -u origin <current-feature-branch>
```

For later pushes:

```bash
git push
```

Replace `origin` only when the repository's configured remote has a different verified name.

### 9.3. Merge policy

- Feature branches are integrated into `develop` only after the work is complete and verified.
- Do not merge a feature branch into `main` or `develop` without explicit authorization.
- Do not perform a release merge automatically.
- Do not delete remote or local branches after merging unless authorized.
- Preserve the project's pull-request and review workflow if one exists.

---

## 10. Git Command Permission Policy

Follow this table when deciding whether a Git command can be executed automatically.

| Command | Permission | Rule |
|---|---|---|
| `git status` | Allow | Run automatically |
| `git diff` | Allow | Run automatically |
| `git log` | Allow | Run automatically |
| `git branch` | Allow | Run automatically |
| `git remote -v` | Allow | Run automatically |
| `git add <specific-path>` | Allow | Only after reviewing the diff |
| `git commit` | Allow | Only with a reviewed, task-specific staged diff |
| `git push` | Allow after checks | Only to the verified current task branch |
| `git fetch` | Allow | May be used to inspect remote updates |
| `git pull` | Ask when it may alter local work | Inspect branch state and impact first |
| `git switch` / `git checkout` | Ask when work is uncommitted | Never risk losing user changes |
| `git merge` | Ask | Requires explicit authorization |
| `git rebase` | Ask | Never rewrite shared history automatically |
| `git stash` | Ask | Do not hide user changes without permission |
| `git branch -d` | Ask | Confirm branch is no longer needed |
| `git branch -D` | Deny by default | Never force-delete a branch |
| `git reset --hard` | Deny by default | Requires explicit user authorization for any exceptional recovery |
| `git clean -fd` | Deny by default | May delete untracked user files |
| `git push --force` | Deny | Absolutely forbidden |
| `git push --force-with-lease` | Deny | Do not rewrite remote history |
| `git commit --amend` | Deny for published commits | Create a new corrective commit instead |

A command being listed as `Allow` does not override the safety rules in this skill.

---

## 11. Handling Common Git Problems

### 11.1. Existing uncommitted changes

- Identify which files were changed before the current task.
- Do not stage them.
- Do not overwrite or revert them.
- Work only on clearly isolated files.
- If the task cannot be safely isolated, stop and report the conflict.

### 11.2. Merge conflicts

- Do not choose "ours" or "theirs" globally without inspecting the conflicting code.
- Do not discard either side automatically.
- Preserve the current working tree.
- Explain which files conflict and why.
- Ask for authorization when resolving the conflict requires a potentially destructive decision.

### 11.3. Push rejected

- Preserve local commits.
- Inspect the current branch and remote tracking state.
- Fetch remote information if safe.
- Do not force push.
- Do not reset the local branch to the remote.
- Report whether the problem is authentication, branch protection, remote updates, or another Git error when identifiable.

### 11.4. Accidental staging

Unstage only the files that should not be included. Do not delete their working-tree contents.

Example:

```bash
git restore --staged path/to/unrelated-file
```

Inspect `git status` again before continuing.

### 11.5. Accidental commit

Do not rewrite published history. If the incorrect commit has not been pushed, stop and inspect the state before choosing a recovery procedure. If it has been pushed, create a safe corrective commit when appropriate and report the issue.

---

## 12. Required Final Git Report

After completing a task, provide a report in clear language.

### 12.1. Repository status

Report:

- Repository name / remote URL.
- Current branch.
- Base branch used.
- Number of commits created.
- Target remote branch.
- Remaining uncommitted changes.
- Whether the push succeeded.

### 12.2. Every commit

For every commit, report:

1. Full commit hash.
2. Commit subject.
3. Summary of the commit body.
4. Files added.
5. Files modified.
6. Files deleted.
7. Verification results.
8. Push status.

Do not combine multiple commits into one generic summary.

### 12.3. Example report

```text
Repository: MELODICK RECS.
Branch: feature/artist-management
Base branch: develop
Remote: origin
Remote branch: origin/feature/artist-management

Commits created: 3

1. <full-hash>
   feat(database): add artist table migration

   - Added the artist table migration.
   - Added fields required by the public artist listing and Admin management.
   - Verification: migration validation passed.
   - Push: included in successful feature branch push.

2. <full-hash>
   feat(backend): implement artist entity and repository

   - Added artist persistence mapping and repository operations.
   - Verification: backend tests passed.
   - Push: included in successful feature branch push.

3. <full-hash>
   feat(admin-artists): add artist creation form

   - Added the Admin artist creation form and field validation.
   - Verification: TypeScript check and frontend build passed.
   - Push: included in successful feature branch push.

Working tree: clean
Push status: successful
```

Use actual hashes, files, branch names, and results. The example is not a claim that these changes have been performed.

If there are no commits or no push, state that clearly.

---

## 13. Non-negotiable Rules

1. Never work directly on `main` or `develop`.
2. Never use phase-based branch names.
3. Never create a large commit for multiple unrelated tasks.
4. Every implementation commit must have a detailed Conventional Commit subject and body.
5. Never stage all files blindly.
6. Never commit secrets, production credentials, or local environment files.
7. Never fabricate test, build, or push results.
8. Never force push or overwrite remote history.
9. Never discard user changes to make Git operations easier.
10. Never merge into protected integration or production branches without authorization.
11. Push verified work to the correct feature branch while preserving individual commits.
12. Report every created commit and the final repository state accurately.

**Priority:** Protect user data and Git history first. Maintain atomic commits second. Automate safe GitHub pushes third.
