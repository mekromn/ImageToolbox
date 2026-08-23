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

## 2026-08-23 09:50Z — completeness question

User:

> What needs to be added

A further completeness pass identified 30 advanced requirements, with four especially important architecture items called out before deep implementation:

- dual SDR/HDR rendition editing;
- SDR/HDR pair alignment;
- encode -> reopen -> validate export;
- per-operation recipe algorithm versioning.

Other approved additions included HDR Master controls, reconstruction optimization, bracketed HDR merge, gain-map resolution/encoding controls, perceptual gain-map editing, frequency separation, halo detection, detailed reconstruction-error analysis, Ultra HDR integrity checker, consolidated metadata inspection, Headroom Lab, display-adaptation visualization, HDR color-volume analysis, absolute/relative HDR scopes, reference-image matching, named edit snapshots, recipe/sidecar import-export, deterministic rendering, explicit quality fallback telemetry, tiled full-resolution rendering, hardware capability/verification tools, HDR batch lab, gain-map presets, optional semantic Smart Gain Map and a dedicated Reference Lab workspace.

## 2026-08-23 09:59Z — user approved additions and asked for anything else

User:

> Ok add those as well. Anything else?

Repository action:

Created authoritative approved-scope supplement:

- `docs/REFERENCE_LAB_ADVANCED_REQUIREMENTS.md`

It records the previously identified 30 advanced requirements and an additional architecture completeness pass.

Additional architecture requirements locked during this pass:

- explicit scene-referred vs display-referred operation stages;
- ICC/CICP/NCLX color-profile provenance and deterministic conflict resolution;
- straight vs premultiplied alpha correctness;
- stable source-coordinate contract for geometry and all masks;
- optional render/pixel provenance audit record;
- high-precision technical and creative LUT support;
- chart-assisted color/camera calibration;
- non-destructive technical lens/geometry correction;
- depth-map-aware masking when available;
- CPU/GPU/preview/export cross-backend conformance tests.

The project state was updated to make these approved requirements and the early architecture locks part of the resume procedure.

## 2026-08-23 10:38Z — second-wave approval and implementation start

User:

> Ok add those and let's begin

Approved second-wave requirements were added as `docs/REFERENCE_LAB_SECOND_WAVE_REQUIREMENTS.md`, numbered 41–50:

41. lossless/passthrough editing and objective lossless verification;
42. professional Color Mixer / Selective Color / Channel Mixer / grading;
43. real soft proofing;
44. multi-point scientific color sampler;
45. non-destructive Healing / Clone / Remove;
46. image-defect correction;
47. operation blend modes;
48. Dodge & Burn;
49. explicit profile assignment vs profile conversion;
50. bit-exact/lossless output verification.

The standalone handoff document was updated to `ImageToolbox_Reference_Lab_Feature_Specification_v4.docx`, now 20 pages, rendered page-by-page and visually inspected before delivery.

### First production implementation

Created:

- `core/domain/src/main/kotlin/com/t8rin/imagetoolbox/core/domain/image/editing/EditRecipe.kt`
- initial recipe model commit `20045d659ff9d1e092d1c63bc04ba207bab2a67b`.

Recipe schema v1 intentionally includes expensive-to-retrofit architecture from day one:

- schema version and source identity/fingerprint;
- ordered operations;
- stable operation ID/type;
- algorithm ID and algorithm version;
- enabled/opacity;
- blend mode;
- explicit processing stage;
- typed parameters;
- mask reference;
- lossless intent;
- source-normalized masks;
- dual SDR/HDR rendition state;
- HDR Master state;
- first-class Gain Map recipe state;
- gain-map generator algorithm/version;
- current/stale gain-map dependency state;
- SDR/HDR pair reference source;
- gain-map operation stack;
- gain-map metadata and channel mode;
- separate profile-assignment and profile-conversion semantics.

Created recipe architecture unit tests in:

- `core/domain/src/test/kotlin/com/t8rin/imagetoolbox/core/domain/image/editing/EditRecipeTest.kt`
- initial test commit `6e6d42f49a74259c5758e14827720ce38af1321c`.

Created platform-independent recipe history engine:

- `core/domain/src/main/kotlin/com/t8rin/imagetoolbox/core/domain/image/editing/EditRecipeHistory.kt`
- commit `0245b75cbeb827ae37f6ba45b9c5e4bd2e0dd201`.

History behavior includes:

- undo/redo;
- bounded history;
- begin/end interaction grouping so a continuous slider drag becomes one undo entry;
- cancel interaction;
- restore persistent authoritative state without carrying stale undo history.

Additional tests for grouped interaction, cancel, and gain-map dependency state through undo/redo were added in commit `241eb4c894cedd6254ad86c369f398d0de0ad335`.

### Existing editor migration finding

`SingleEditComponent.HistorySnapshot` currently stores a cached rendered URI plus image/export settings rather than a durable operation graph. Existing filter/curve subtools can render into a new cached PNG/working bitmap.

Migration decision:

- `EditRecipe` becomes authoritative edit intent;
- baked/cached bitmaps become disposable acceleration/legacy compatibility artifacts;
- existing tools remain working during migration;
- each tool is migrated incrementally to recipe-native preview/full-resolution rendering rather than breaking the editor all at once.

### Zoom implementation

ImageToolbox's shared fullscreen zoom surface already used a 20x maximum. Therefore the approved requirement to double the relevant current maximum means 40x for ImageToolbox, not the old Aves-specific approximately-10x target.

Changed:

- `EnhancedZoomableModalBottomSheet`: `maxScale = 20f` -> `40f`;
- commit `6dfbb0d25a4700644e85fd9b827afb4efe44ee36`.

The code explicitly warns that 40x gesture zoom alone does not guarantee additional source detail; Reference/Pixel Inspector still needs source-aware LOD/full-resolution sampling.

### CI and APK workflow

Created:

- `.github/workflows/reference_lab.yml`
- initial commit `1ae10995054681b219a4991dee65a84b9bd12d91`.

Purpose:

- run core recipe tests;
- build an unsigned FOSS debug APK;
- upload that APK as a workflow artifact.

Opened ImageToolbox draft PR #1:

- **Reference Lab foundation: versioned non-destructive recipe architecture**.

First CI run `32634707730` failed before compiling tests because the Gradle task name `:core:domain:testDebugUnitTest` was ambiguous: the project defines `testFossDebugUnitTest` and `testMarketDebugUnitTest`.

This was a workflow selection error, not a recipe test/code failure.

Fix committed as `807d3c2829b0c6319844d133ea80e56135aa679a`:

- use `:core:domain:testFossDebugUnitTest` explicitly;
- migrate JDK action to `actions/setup-java@v5` with Temurin 21.

Latest observed branch/PR head after recipe-history tests:

- `241eb4c894cedd6254ad86c369f398d0de0ad335`;
- draft PR #1 remained open;
- corrected Reference Lab CI run `32634896356` was queued at last observation.

No CI success or APK availability is claimed until the corrected run actually completes.

## Immediate next task

1. let corrected CI validate the recipe/history model and fix any real compile/test/build failures;
2. attach `EditRecipe`/`EditRecipeHistory` to `SingleEditComponent` state and existing `HistorySnapshot` undo/redo without breaking baked-bitmap compatibility;
3. initialize/update recipe source identity as images change;
4. invalidate dependent gain-map state when legacy bitmap edits change the rendered base;
5. persist/recover recipes;
6. audit full-resolution/LOD behavior so 40x zoom never masquerades as source detail;
7. begin the high-precision Reference renderer/color/HDR path only after these foundations are sound.
