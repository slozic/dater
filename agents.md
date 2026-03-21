# Agent Guidance (`dater` workspace)

This file is for agent behavior and project working standards.

Persistent project context and evolving feature state should live in:
- `.cursor/rules/project-context.mdc` (backend repo)
- `../dater-mobile/.cursor/rules/project-context.mdc` (mobile repo)

## Scope and structure

- `dater` repository contains backend code and shared project notes.
- `dater-mobile` is a sibling repository and should keep its own runtime context/docs.
- Keep backend and mobile docs separated; cross-link when needed.

## Coding standards

- Prefer clear, maintainable code over clever shortcuts.
- Keep API contract changes synchronized across backend DTOs and mobile types.
- Keep logging production-friendly (avoid noisy debug logs unless actively diagnosing).
- For notification/chat flows, core business actions must remain resilient even if notification sending fails.

## Testing expectations

- Add or update tests for behavior changes:
  - backend service logic -> unit tests
  - endpoint/data-flow changes -> integration tests
  - mobile UI behavior -> lint + manual path verification
- Run focused tests for touched areas before committing.

## Documentation rules

- Root `README.md` files should give quick project setup and architecture overview.
- Detailed docs belong under `/docs` in each repository.
- Keep links between backend/mobile docs updated whenever paths change.
- Keep `agents.md` focused on standards/guidelines, not change history.

## Git and commit hygiene

- Never commit secrets or local diagnostics (`logs.txt`, credentials, firebase keys).
- Use concise commit titles focused on intent.
- Split backend and mobile commits unless a single atomic change requires both.

## Current priorities

- Keep notification flow stable (in-app + push + deep links).
- Preserve consistent mobile spacing/alignment patterns across Date Details, Chat, and Public Profile.
- Remaining backlog includes report/block functionality and final UI polish pass.
