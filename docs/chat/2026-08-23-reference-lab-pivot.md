# Project Chat Record — 2026-08-23 ImageToolbox Reference Lab Pivot

## Context inherited from the immediately preceding Aves work

The project had been targeting `mekromn/aves-editor` as a maximum-fidelity gallery/viewer plus non-destructive editor. During that work the following requirements became explicit:

- maximum-fidelity viewer to the limits of hardware is mandatory;
- double the relevant current maximum pinch zoom;
- add robust Ultra HDR support;
- add major photo rendering quality improvements;
- add non-destructive high-precision editing;
- add Snapseed-like Tonal Contrast, Details/Structure/Sharpening and Ambiance equivalents;
- add Google Photos-like Skin Tone and Blue Tone equivalents;
- add Pixel-Adaptive-style color-volume expansion as an explicit creative mode;
- add masks, scopes, comparison tools, RAW/DNG, professional export, batch editing/export, calibration patterns, render telemetry and automated image regression testing;
- document absolutely everything, including chat-derived requirements, for 101% continuity.

The Aves branch had begun implementing documentation, recipe persistence and a first native-to-Flutter precision audit, but the user changed direction before continuing that implementation.

## 2026-08-23 09:32Z — pivot request

User:

> I changed my mind let's pivot and add all relevant features we just talked about to image toolbox instead

Decision:

- Active implementation target becomes `mekromn/ImageToolbox`.
- ImageToolbox is considered the cleaner architecture for this scope because it is already native Android/Kotlin/Compose and already contains single-edit, filters, curves, preview, compare, codec, compression and saving/export systems.
- The good product/color/HDR/non-destructive design decisions from the Aves discussion carry forward.
- Aves-specific Flutter transport design does not carry forward as implementation architecture.

Repository actions:

- verified `mekromn/ImageToolbox` with admin/push permission;
- verified default branch `master`;
- current base commit at pivot: `70cce13f91ba80df9ac11f2c58f3c3bb0d6048d2`;
- created branch `feature/reference-lab` from `master`;
- closed `mekromn/aves-editor` draft PR #1 as superseded, preserving its branch/history for reference.

## 2026-08-23 09:33Z — feature document request

User:

> Give me a document of features as well

Actions:

- created a standalone Word specification: `ImageToolbox_Reference_Lab_Feature_Specification.docx`;
- the DOCX contains the maximum-fidelity viewer, non-destructive editing, Light/Color/Detail tools, masks, scopes, HDR/Ultra HDR, RAW/DNG, professional export, touch UX, validation corpus, regression strategy, implementation phases and definition of done;
- rendered and visually inspected the DOCX before delivery;
- added equivalent authoritative in-repo scope at `docs/REFERENCE_LAB_FEATURE_SPEC.md`;
- added `docs/CONTINUITY_PROTOCOL.md`;
- added `docs/PROJECT_STATE.md`.

## Initial ImageToolbox architecture findings

The repository module graph already includes relevant dedicated modules:

- `feature:single-edit`;
- `feature:filters`;
- `feature:curves`;
- `feature:image-preview`;
- `feature:compare`;
- `core:filters`.

`SingleEditComponent` already contains:

- history via `BaseHistoryComponent<HistorySnapshot>`;
- working and preview bitmap state;
- image metadata/info;
- filters;
- curves;
- crop/rotate/flip;
- drawing/erase state;
- presets;
- export profiles;
- preview generation;
- compression + transformation;
- saving through `FileController.save(ImageSaveTarget(...))`.

Important implication:

The project should extend/refactor SingleEdit into a durable recipe-driven non-destructive system rather than layering a second editor beside it.

Important future audit:

The current workflow can update/replace working bitmap state after sub-edits and can scale images for display. Reference Lab must prevent progressive pixel degradation from becoming the authoritative edit model. The target is:

`source -> versioned recipe/operation graph -> preview renderer -> full-resolution export renderer -> existing ImageToolbox save layer`

## 2026-08-23 09:43Z — Gain Map Lab requirement

User:

> It should also be able to create edit and tune a gainmap for an image

Decision:

Gain maps become a first-class P0 editable asset, not only an automatic side effect of Ultra HDR export.

Required capabilities added:

- create a gain map automatically from an SDR image;
- derive a gain map from a matched SDR/HDR pair and measure reconstruction error;
- extract an existing Ultra HDR base image, gain-map contents and metadata;
- create an SDR base + gain map from an HDR source;
- directly edit 1-channel or 3-channel gain maps;
- map levels/curves/gamma/black point/white point/threshold/rolloff/smoothing;
- local brush/gradient/range-mask gain editing;
- metadata tuning for ratio min/max, gamma, SDR/HDR epsilon, HDR transition/full-HDR display ratios and newer platform fields;
- actual and simulated display-headroom preview;
- gain-map grayscale/RGB view, heatmap, histogram, pixel inspector and reconstruction-error map;
- non-destructive storage inside the main edit recipe;
- dependency tracking so edits to SDR/HDR image state invalidate, regenerate or re-fit stale gain maps;
- standards/platform-compatible backward-compatible Ultra HDR export.

Created authoritative subsystem document:

- `docs/GAIN_MAP_LAB.md`

## 2026-08-23 09:44Z — GlowHDR inspiration

User:

> Glowhdr app is inspiration

Publicly observable GlowHDR behavior was reviewed and recorded as product inspiration, especially:

- quick SDR-to-Ultra-HDR conversion;
- smart/automatic gain-map generation;
- simple gain-map strength/intensity tuning;
- HDR display preview;
- backward-compatible SDR behavior;
- ordinary JPEG/PNG/HEIC input workflow.

The design rule is **GlowHDR-like simplicity as the quick path, but with a much deeper expert Gain Map Lab available underneath**.

No proprietary implementation details are to be copied. The algorithms will be independently designed from documented gain-map math/platform APIs and validated objectively.

## Immediate next task

Inspect `HistorySnapshot`, `BaseHistoryComponent`, filter/curve transformation flow, preview/fullscreen zoom implementation, and current image codec capability. Then define the first compatible versioned edit recipe with gain-map state included from the start, and safely double the relevant maximum pinch zoom.
