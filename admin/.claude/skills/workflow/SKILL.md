---
name: workflow
description: End-to-end feature development pipeline from requirements to PR.
argument-hint: <feature-name> <entity>
disable-model-invocation: true
allowed-tools: Bash(pnpm *), Bash(git *), Bash(gh *)
---

Run the full development pipeline for: $ARGUMENTS

Parse as `<feature-name> <entity>`. If entity is omitted, infer from feature name.

Stop immediately if any step fails. Report the error and wait for user input before retrying.

---

## Step 1: Feature Planning

Follow the instructions in the `feature-kickoff` skill using the feature name from $ARGUMENTS.

**⏸️ GATE: Present the plan to the user and wait for explicit approval (yes/no) before proceeding to Step 2.**

---

## Step 2: File Scaffold

After approval, follow the instructions in the `page-scaffold` skill using the entity name from $ARGUMENTS.

Generate both List and Detail pages unless the user specified otherwise in Step 1.

---

## Step 3: Validation

Run the full validation suite:

```bash
pnpm run lint && tsc --noEmit && pnpm run test
```

- On failure: auto-fix and re-run (max 3 attempts)
- After 3 failures: report errors to user and stop

Do not proceed to Step 4 until validation passes.

---

## Step 4: Commit

**⏸️ GATE: Show the user a summary of changed files and the proposed commit grouping. Wait for confirmation before committing.**

After confirmation:

1. `git status` — identify changed files
2. Group by logical unit (api, hooks, ui, pages)
3. For each group: `git add <files>` → `git commit -m "<type>: <Korean subject>"`
4. `git log -n 5` — verify commits

Rules:
- Korean commit messages only
- Atomic commits (one logical change per commit)
- No Co-Authored-By

---

## Step 5: PR Creation

**⏸️ GATE: Show the user the draft PR title and body. Wait for confirmation before running `gh pr create`.**

Draft format:
```
# 📋 연관 이슈
close #<issue_number>

# 🚀 작업 내용
- <핵심 변경사항>

# 💬 리뷰 중점 사항
- <해당 시>
```

After confirmation: `gh pr create --title "..." --body "..."` and return the PR URL.
