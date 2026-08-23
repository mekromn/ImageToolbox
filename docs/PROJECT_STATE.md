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
5. **Gain Map Lab:** create, derive, extract, edit, tune, analyze and export gain maps as first-class editable assets.
6. Scientific inspection, scopes, comparison, calibration and regression testing.
7. Professional export using existing ImageToolbox save/codec strengths.
8. 101% continuity in repository docs.

Full scope: `docs/REFERENCE_LAB_FEATURE_SPEC.md`.

Gain Map Lab scope: `docs/GAIN_MAP_LAB.md`.

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

## Gain Map Lab — P0 addition

User requirement added 2026-08-23:

> It should also be able to create edit and tune a gainmap for an image.

User also identified **GlowHDR** as product inspiration for the gain-map workflow.

GlowHDR inspiration is limited to publicly observable behavior such as:

- fast SDR-to-Ultra-HDR conversion;
- smart/automatic gain-map generation;
- simple gain-map intensity control;
- HDR preview;
- backward-compatible SDR base.

Reference Lab must independently implement the math and go substantially deeper. The planned Gain Map Lab includes:

- automatic SDR -> gain-map Ultra HDR creation;
- SDR + HDR pair gain-map derivation with reconstruction-error measurement;
- extraction/inspection of existing Ultra HDR base, map and metadata;
- HDR source -> SDR base + gain-map conversion;
- direct non-destructive editing of 1-channel and 3-channel gain maps;
- map curves/levels/gamma/black-white points/threshold/rolloff/smoothing;
- local brush/gradient/range-mask gain editing;
- advanced gain-map metadata tuning;
- actual and simulated display-headroom preview;
- gain-map histogram/heatmap/pixel inspection and reconstruction-error maps;
- dependency tracking so normal SDR/HDR edits invalidate or regenerate stale maps;
- validated backward-compatible Ultra HDR export.

Android gain-map metadata controls to expose/validate include ratio min/max, gamma, SDR/HDR epsilon, HDR transition ratio and full-HDR display ratio; Android 16 additionally exposes gain-map direction and alternative image primaries.

Authoritative subsystem spec: `docs/GAIN_MAP_LAB.md`.

## Current implementation state

Completed in this pivot session:

- created `feature/reference-lab` from current `master`;
- added full Reference Lab feature specification;
- added continuity protocol;
- inspected current module graph and SingleEdit architecture;
- closed Aves draft PR #1 as superseded, preserving the branch/history;
- created an external Word feature specification for handoff/reference;
- added Gain Map Lab as a P0 subsystem and documented GlowHDR as workflow inspiration.

No pixel-rendering behavior has been changed in ImageToolbox yet in this branch.

## Immediate next implementation order

1. Map `BaseHistoryComponent` / `HistorySnapshot` and the exact SingleEdit filter/curve/export render path.
2. Define a versioned non-destructive `EditRecipe` that can coexist with existing ImageToolbox tools.
3. Include gain-map generation/edit metadata as recipe-owned non-destructive state from the start rather than bolting it on later.
4. Persist/recover recipe state without altering originals.
5. Trace the image-preview/fullscreen zoom implementation and double the relevant maximum pinch zoom safely.
6. Audit decode -> bitmap/image -> Compose/GPU -> Android surface -> display for color/precision/HDR losses.
7. Add viewer/render telemetry and strict Reference / physical 1:1 / Pixel Inspector behavior.
8. Establish the high-precision working/color contract before exposing new Light/Color sliders.
9. Implement first real pointwise adjustments and comparison controls.
10. Implement HDR/Ultra HDR display plus Gain Map Lab creation/extraction/tuning/preview/export.
11. Add spatial/local tools, masks, scopes, RAW and advanced export in roadmap order.

## Validation rule

Do not claim a feature works until build/tests and, for rendering behavior, target-device validation have actually occurred.
