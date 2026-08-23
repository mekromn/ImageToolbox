# Reference Lab Continuity Protocol

This project must remain fully resumable across chats, devices, developers, branches, and interrupted sessions.

## Non-negotiable rule

No important project knowledge may exist only in chat, memory, a local working tree, or an unreferenced commit.

Documentation is part of implementation completion.

## Record all material information

Document:

- user requirements and changes of direction;
- feature scope and priority;
- architectural decisions and rationale;
- alternatives considered and rejected;
- exact mathematical models, constants, color spaces, transfer functions, and HDR assumptions;
- implementation changes and relevant commits;
- experiments and measurements;
- failed approaches and why they failed;
- bugs and visual observations;
- CI/build/test results, including failures;
- device-specific findings;
- compatibility/fallback behavior;
- performance/memory findings;
- format/codec limitations;
- licensing/provenance decisions;
- deferred work and known unknowns.

## Required resume order

At the beginning of a new work session/chat:

1. Read `docs/PROJECT_STATE.md`.
2. Read `docs/REFERENCE_LAB_FEATURE_SPEC.md`.
3. Read this continuity protocol.
4. Read the latest relevant `docs/chat/*.md` record.
5. Read subsystem design/audit docs relevant to the next task.
6. Inspect the current branch/PR/CI state before changing code.

Do not infer current implementation state from memory when repository state is available.

## Required close-out

Before ending a substantial work session:

1. Update `docs/PROJECT_STATE.md` with actual current status.
2. Add/update the session chat record with user requirements, decisions, commits, tests, failures, and next exact step.
3. Update any subsystem document affected by the work.
4. Record CI/build status truthfully; do not call unverified work passing.
5. Ensure the next developer can identify the exact branch, base, current head, blockers, and immediate next action.

## Definition of documented

A vague statement such as "worked on HDR" is not sufficient.

A useful record names:

- files/subsystems changed;
- intended behavior;
- important implementation details;
- assumptions;
- test/result status;
- commit or PR when available;
- unresolved risks;
- next validation step.

## Pixel/color work

For any pixel-changing or display-path change, documentation must explicitly state where relevant:

- input encoding/color space/transfer;
- working representation and precision;
- operation space;
- gamut behavior;
- clipping/compression behavior;
- alpha convention;
- HDR/Ultra HDR interpretation;
- preview path;
- export path;
- known device/platform fallbacks.

## Failure preservation

Failed approaches are project knowledge. Record them instead of silently deleting the history of why a path was rejected.
