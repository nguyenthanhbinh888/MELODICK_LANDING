
---
name: react-architecture
description: Enforces maintainable React and TypeScript architecture, component separation, reusable logic and frontend coding conventions.
---

# React Architecture Standards

## 1. General Rules

- Use TypeScript strict mode.
- Avoid using any unless absolutely necessary.
- Use meaningful variable and function names.
- Separate UI from business logic.
- Keep components small and focused.

## 2. Suggested Structure

src/
  assets/
  components/
    common/
    layout/
  features/
    auth/
      components/
      hooks/
      services/
      types/
    users/
      components/
      hooks/
      services/
      types/
  hooks/
  layouts/
  pages/
  routes/
  services/
    api/
  types/
  utils/
  styles/

Adapt the structure to the existing project.

## 3. Component Rules

- One primary responsibility per component.
- Reuse shared UI components.
- Avoid deeply nested components.
- Define clear component props.
- Avoid unnecessary prop drilling.

## 4. State Management

- Use local state for local interactions.
- Use shared state only when needed.
- Avoid duplicating server state.
- Handle loading and error states.

## 5. API Integration

- Keep API requests inside service modules.
- Centralize API configuration.
- Use typed request and response models.
- Handle API errors consistently.
- Never hardcode API credentials.

## 6. Quality

- Run TypeScript checks.
- Run ESLint.
- Run production build.
- Test important interactions.
