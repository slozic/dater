---
name: code-review-gate
description: Perform a pre-commit code review gate for this project. Use when the user asks for review, pre-commit checks, commit readiness, risk scan, or "quick review" vs "strict review" before merge/push.
---

# Code Review Gate (Project)

Use this skill before commit/push or when user asks for a review.

## Modes

Choose one mode based on user request or risk level.

- `quick-review`:
  - Focus on blockers only.
  - Timebox to high-impact issues.
- `strict-review`:
  - Full sweep for correctness, regressions, security, performance, and test gaps.

Default:
- If user says "quick", use `quick-review`.
- If user says "strict", "full", "thorough", or change touches auth/data migrations/notifications, use `strict-review`.

## Mandatory checks

1. Inspect change scope:
   - `git status --short`
   - `git diff -- .`
   - recent commit style (`git log -5 --oneline`)
2. Identify changed files and rank risk:
   - high: auth/security/data model/migrations/notifications
   - medium: API/controller/service logic
   - low: UI copy/styling/docs
3. Validate no secrets or local artifacts are being committed:
   - keys, service-account JSON, `.env*`, debug logs, scratch notes

## Review checklist

- Correctness:
  - logic/edge cases/null handling
  - authorization checks
  - backward compatibility
- Regression risk:
  - changed contracts (DTO/API/types)
  - behavior shifts not covered by tests/manual validation
- Security/privacy:
  - token handling, credential leakage, unsafe logging
- Operational quality:
  - meaningful error handling and logs
  - noisy debug settings accidentally left enabled
- Tests:
  - missing or stale tests for changed behavior

## Independent reviewer pass

When feasible, run a second read-only reviewer pass in separate context and merge findings.

Rules:
- Only critical/high confidence issues block commit.
- Non-blockers become follow-up suggestions.

## Output format

Report findings first, ordered by severity:

- `Critical`: must fix before commit/push
- `High`: strong recommendation before merge
- `Medium`: should improve soon
- `Low`: optional polish

If no findings:
- State explicitly: "No blocking findings."
- Mention residual risk/testing gaps briefly.

## Project-specific reminders

- Repo has mobile + backend integration; check contract parity when either side changes.
- Keep `agents.md` aligned when user requests project-notes updates.
- For mobile push work, never commit `google-services.json` or Firebase admin key files.
