---
name: workflow
description: End-to-end feature development pipeline from requirements to PR.
argument-hint: <feature-name> <entity>
disable-model-invocation: true
allowed-tools: Bash(pnpm *), Bash(git *), Bash(gh *)
---

Run the full development pipeline for: $ARGUMENTS

Parse as `<feature-name> <entity>`. If entity is omitted, infer from the feature name.

Stop immediately if any step fails. Report the error and wait for user input.

---

## Step 1: Feature Planning

Read `.claude/skills/feature-kickoff/SKILL.md` and follow the instructions.

⏸️ **GATE: Present the plan and wait for explicit user approval before proceeding.**

---

## Step 2: File Scaffold

Read `.claude/skills/page-scaffold/SKILL.md` and follow the instructions using the entity from $ARGUMENTS.

---

## Step 3: Validation

Read `.claude/skills/validation/SKILL.md` and follow the instructions.

Do not proceed to Step 4 until validation passes.

---

## Step 4: Commit

Read `.claude/skills/smart-commit/SKILL.md` and follow the instructions.

⏸️ **GATE: Show the user the proposed commit grouping and messages. Wait for confirmation before committing.**

---

## Step 5: PR Creation

Read `.claude/skills/smart-pr/SKILL.md` and follow the instructions.

⏸️ **GATE: Show the draft PR title and body. Wait for confirmation before running `gh pr create`.**
