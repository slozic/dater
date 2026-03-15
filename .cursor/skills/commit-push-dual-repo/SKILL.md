---
name: commit-push-dual-repo
description: Commit and push changes safely across dater backend and dater-mobile repos with a quick review pass. Use when the user asks to commit, push, or "commit and push" changes, especially when both repositories are involved.
---

# Commit + Push (Dual Repo)

Use this workflow when user asks to commit/push in `dater` and/or `dater-mobile`.

## Scope detection

1. Detect requested scope:
   - backend only: `dater`
   - mobile only: `dater-mobile`
   - both repos
2. Respect user intent exactly (do not commit unrelated work).

## Required pre-commit checks (per repo)

Run in parallel:
- `git status --short`
- `git diff -- .`
- `git log -5 --oneline`

Then:
- Exclude secrets and local artifacts (for example `.env*`, `google-services.json`, `*firebase-adminsdk*.json`, `logs.txt`, scratch notes).
- If repo has unrelated modified files, stage only files relevant to requested task.

## Lightweight review before commit

Perform a short review of staged/targeted changes:
- Correctness/regression risk
- Security/privacy risk (credentials, tokens, logs)
- Consistency with existing code style and commit style
- Quick "can be follow-up later" notes (non-blockers)

If possible, run an independent reviewer pass (separate agent/context, read-only) and merge findings. Treat critical findings as blockers; suggestions can be deferred.

## Commit rules

- One commit per repo unless user asks otherwise.
- Message should explain purpose/intent, not raw file list.
- Use concise style matching repo history.
- Never amend unless user explicitly asks.

Suggested structure:

```
<imperative summary sentence>

<1 short sentence about why or outcome>
```

## Push rules

- Push only when user asked to push (or asked "commit and push").
- Push current branch with `git push origin HEAD`.
- Report resulting branch update lines to user.

## User-facing response format

For each repo, report:
- commit hash
- branch pushed
- short summary of included work
- explicitly mention intentionally excluded files (if any)

## Dater-specific reminders

- Backend repo: `C:/Users/sly-x/projects/spring/dater`
- Mobile repo: `C:/Users/sly-x/projects/spring/dater-mobile`
- Keep credential files uncommitted in mobile (`google-services.json`, Firebase admin keys).
- If docs/process changed, update `agents.md` in backend when requested.
