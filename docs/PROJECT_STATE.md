# ImageToolbox Reference Lab — Project State

Last updated: 2026-08-23

## Repository

- Active repository: `mekromn/ImageToolbox`
- Default/base branch: `master`
- Working branch: `feature/reference-lab`
- Base commit: `70cce13f91ba80df9ac11f2c58f3c3bb0d6048d2`
- Previous Aves implementation target: superseded; `mekromn/aves-editor` PR #1 is closed but preserved for historical/reference use.

## P0 goals

1. Maximum-fidelity viewer to the limits of source + hardware.
2. Full non-destructive high-precision editor integrated into existing ImageToolbox architecture.
3. No hidden image enhancement in Reference mode.
4. Wide gamut, HDR and Ultra HDR as first-class paths.
5. Scientific inspection, scopes, comparison, calibration and regression testing.
6. Professional export using existing ImageToolbox save/codec strengths.
7. 101% continuity in repository docs.

Full scope: `docs/REFERENCE_LAB_FEATURE_SPEC.md`.

Continuity rules: `docs/CONTINUITY_PROTOCOL.md`.

## Pivot rationale

ImageToolbox is a stronger implementation target than Aves for this feature set because it already provides native Android/Kotlin/Compose image editing architecture and dedicated modules for:

- `feature:single-edit`;
- `feature:filters`;
- `feature:curves`;
- `feature:image-preview`;
- `feature:compare`;
- `core:filters`;
- image decode/transform/compress abstractions;
- export profiles;
- `FileController` / `ImageSaveTarget` saving architecture.

This substantially reduces architectural mismatch and lets the project focus on fidelity, non-destructive recipes, color/HDR science, analysis and advanced controls.

## Existing SingleEdit findings

`SingleEditComponent` already owns substantial editor state and infrastructure, including:

- source URI;
- original size;
- original/internal bitmap and working/preview bitmap;
- `ImageInfo`;
- filter list;
- curves state;
- crop/rotation/flip state;
- drawing/erase state;
- metadata;
- presets;
- export profiles;
- save flow through `ImageCompressor.compressAndTransform()` + `FileController.save(ImageSaveTarget(...))`;
- history through `BaseHistoryComponent<HistorySnapshot>`;
- debounced preview recalculation.

This means Reference Lab should extend/refactor the existing SingleEdit state model rather than build a second editor.

## Important architectural issue to solve

The existing editor can replace/update the working bitmap after editing and uses scaled bitmaps/previews for display. Reference Lab requires a durable non-destructive operation recipe whose authoritative state is edit intent rather than a chain of progressively modified/quantized pixel buffers.

The design must preserve existing tools while moving high-fidelity operations toward:

`source -> versioned recipe/operation graph -> preview renderer -> explicit full-resolution export renderer -> existing ImageToolbox save layer`

## Current implementation state

Completed in this pivot session:

- created `feature/reference-lab` from current `master`;
- added full Reference Lab feature specification;
- added continuity protocol;
- inspected current module graph and SingleEdit architecture;
- closed Aves draft PR #1 as superseded, preserving the branch/history;
- created an external Word feature specification for handoff/reference.

No pixel-rendering behavior has been changed in ImageToolbox yet in this branch.

## Immediate next implementation order

1. Map `BaseHistoryComponent` / `HistorySnapshot` and the exact SingleEdit filter/curve/export render path.
2. Define a versioned non-destructive `EditRecipe` that can coexist with existing ImageToolbox tools.
3. Persist/recover recipe state without altering originals.
4. Trace the image-preview/fullscreen zoom implementation and double the relevant maximum pinch zoom safely.
5. Audit decode -> bitmap/image -> Compose/GPU -> Android surface -> display for color/precision/HDR losses.
6. Add viewer/render telemetry and strict Reference / physical 1:1 / Pixel Inspector behavior.
7. Establish the high-precision working/color contract before exposing new Light/Color sliders.
8. Implement first real pointwise adjustments and comparison controls.
9. Implement HDR/Ultra HDR display/preservation/export.
10. Add spatial/local tools, masks, scopes, RAW and advanced export in roadmap order.

## Validation rule

Do not claim a feature works until build/tests and, for rendering behavior, target-device validation have actually occurred.
