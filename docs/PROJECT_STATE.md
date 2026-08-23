# ImageToolbox Reference Lab — Project State

Last updated: 2026-08-23

## Repository

- Active repository: `mekromn/ImageToolbox`
- Default/base branch: `master`
- Working branch: `feature/reference-lab`
- Base commit: `70cce13f91ba80df9ac11f2c58f3c3bb0d6048d2`
- Active draft PR: `#1` — **Reference Lab foundation: versioned non-destructive recipe architecture**
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

Approved advanced requirements 1–40: `docs/REFERENCE_LAB_ADVANCED_REQUIREMENTS.md`.

Approved second-wave requirements 41–50: `docs/REFERENCE_LAB_SECOND_WAVE_REQUIREMENTS.md`.

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

### Current history limitation discovered

`SingleEditComponent.HistorySnapshot` currently records a cached rendered URI plus image/export settings. It does **not** record the filter list, curves, masks, or a durable operation graph.

Current filter/curve subtools can render a result and call `updateBitmapAfterEditing(...)`, causing the working image to become a newly cached PNG/bitmap. That is useful existing behavior but cannot remain the authoritative non-destructive model for Reference Lab.

Migration rule:

- new Reference Lab edit intent lives in `EditRecipe`;
- cached/rendered bitmaps are disposable acceleration/legacy compatibility artifacts;
- existing baked-bitmap tools continue to work during migration;
- each tool is migrated incrementally to recipe-native preview + full-resolution rendering rather than breaking the existing editor all at once.

Target architecture:

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

## Advanced requirements 1–40 — APPROVED

The user approved the complete advanced expansion recorded in `docs/REFERENCE_LAB_ADVANCED_REQUIREMENTS.md`.

Major additions include:

- dual-rendition SDR/HDR editing;
- HDR Master controls;
- joint SDR/gain-map/metadata reconstruction optimization;
- subpixel/local alignment and ghost detection for SDR/HDR pairs;
- bracketed-exposure HDR merge -> Ultra HDR creation;
- gain-map resolution/precision/channel/encoding controls;
- perceptual gain-map painting and frequency-separated map shaping;
- halo/ringing detection and repair;
- detailed HDR reconstruction-error analysis;
- encode -> reopen -> validate Reference-grade export;
- standalone Ultra HDR integrity checker;
- consolidated HDR metadata inspector;
- Headroom Lab and display-adaptation curve visualization;
- luminance-aware HDR color-volume/gamut analysis;
- absolute/relative HDR scopes;
- reference-image matching;
- named edit snapshots;
- recipe/sidecar import-export;
- mandatory per-operation algorithm versioning;
- deterministic rendering mode;
- explicit fidelity-fallback telemetry;
- memory-bounded tiled full-resolution processing;
- hardware capability report and device verification status;
- HDR/SDR batch laboratory;
- Gain Map presets;
- optional machine-assisted Smart Gain Map later;
- dedicated `VIEW | EDIT | GAIN MAP | MASKS | ANALYZE | COMPARE | EXPORT` workspace;
- explicit scene-referred vs display-referred stage semantics;
- ICC/CICP/NCLX provenance and conflict resolution;
- straight/premultiplied alpha correctness;
- stable source-coordinate contract for geometry and masks;
- optional pixel/render provenance audit records;
- technical/creative LUT support;
- color-chart calibration/camera matching;
- non-destructive technical lens/geometry correction;
- depth-map-aware masking when available;
- CPU/GPU/preview/export cross-backend conformance testing.

## Second-wave requirements 41–50 — APPROVED

Recorded in `docs/REFERENCE_LAB_SECOND_WAVE_REQUIREMENTS.md`:

41. lossless/passthrough editing and objective lossless verification;
42. professional Color Mixer, Selective Color, Channel Mixer and grading controls;
43. real soft proofing for sRGB/P3/Rec.2020/custom ICC output;
44. persistent multi-point scientific color sampler;
45. non-destructive Healing / Clone / Remove;
46. image-defect correction including luma/chroma denoise, hot/dead-pixel, banding and moiré controls;
47. operation blend modes with explicit color-domain semantics;
48. Dodge & Burn using the shared recipe/mask engine;
49. explicit distinction between profile assignment and profile conversion;
50. bit-exact/lossless verification with PASS / FAIL / NOT VERIFIABLE outcomes.

The schema foundation must remain compatible with these even before their UIs/algorithms land.

## Early architecture locks — DO BEFORE DEEP RENDERER WORK

1. dual SDR/HDR rendition state model;
2. per-operation algorithm versioning;
3. stable geometry/mask coordinate system;
4. scene-referred/display-referred operation-stage contract;
5. color-profile provenance/conflict-resolution rules;
6. deterministic rendering contract;
7. tiled full-resolution processing semantics;
8. encode-reopen-validation contract;
9. gain-map dependency/invalidation rules;
10. preview/export backend conformance requirements.

## IMPLEMENTED: non-destructive recipe domain foundation

Production code now exists at:

`core/domain/src/main/kotlin/com/t8rin/imagetoolbox/core/domain/image/editing/EditRecipe.kt`

Initial commit:

- `20045d659ff9d1e092d1c63bc04ba207bab2a67b` — versioned non-destructive recipe model.

Schema v1 already includes architecture-significant fields instead of postponing them:

- recipe schema version;
- source identity/fingerprint fields;
- ordered operations;
- stable operation ID/type;
- explicit algorithm ID and algorithm version;
- enabled/opacity;
- blend mode;
- processing stage (`Encoded`, `SceneReferredLinear`, `DisplayReferred`, `Perceptual`, `Geometry`);
- typed generic parameters;
- mask reference;
- lossless intent;
- masks with stable source-normalized coordinate space;
- dual SDR/HDR rendition edit mode;
- HDR Master state;
- first-class gain-map recipe state;
- gain-map generator algorithm/version;
- gain-map dependency `Current` / `Stale` state and stale reason;
- SDR/HDR pair reference identity support;
- gain-map operation stack;
- gain-map metadata model;
- 1-channel luminance vs RGB gain-map mode;
- gain-map direction;
- separate semantic operation types for profile assignment and profile conversion.

The model also includes append/replace/remove/move operation helpers and explicit gain-map invalidation without discarding map edit intent.

## IMPLEMENTED: recipe architecture unit tests

Test file:

`core/domain/src/test/kotlin/com/t8rin/imagetoolbox/core/domain/image/editing/EditRecipeTest.kt`

Commit:

- `6e6d42f49a74259c5758e14827720ce38af1321c` — recipe architecture tests.

Current tests cover:

- ordered operation movement;
- algorithm version remaining recipe state;
- gain-map stale marking preserving map edits;
- blend-mode/lossless fields present from schema v1;
- masks defaulting to canonical source-normalized coordinates;
- profile assignment and conversion being distinct semantic operations;
- recipe emptiness semantics.

CI execution is required before these tests are considered passing.

## IMPLEMENTED: Reference Lab CI / debug APK pipeline

Workflow:

`.github/workflows/reference_lab.yml`

Commit:

- `1ae10995054681b219a4991dee65a84b9bd12d91`

Behavior:

- runs on pushes to `feature/reference-lab` and PRs targeting `master`;
- JDK 21;
- runs `:core:domain:testDebugUnitTest`;
- builds `:app:assembleFossDebug`;
- uploads the unsigned FOSS debug APK as `Reference-Lab-FOSS-Debug-APK`.

Draft PR #1 was opened so PR-triggered CI can be inspected through the connected GitHub tooling.

First observed PR run:

- workflow run `32634707730`;
- job `97182603437`;
- last observed state: core unit tests **in progress**; APK build pending.

Do not claim CI success or an APK artifact until the run actually finishes successfully.

## IMPLEMENTED: doubled shared fullscreen zoom ceiling

The shared `EnhancedZoomableModalBottomSheet` upstream/current limit was `20f`.

Reference Lab now sets:

`rememberZoomState(maxScale = 40f)`

Commit:

- `6dfbb0d25a4700644e85fd9b827afb4efe44ee36`

This is the actual ImageToolbox interpretation of the approved requirement to double the relevant existing maximum zoom. It replaces the old Aves-specific expectation of roughly 10x.

Important fidelity note: increasing the gesture scale ceiling does **not** guarantee more source detail. Reference/Pixel Inspector still requires source-aware LOD/full-resolution decode and deterministic physical-pixel sampling. The code comment states this explicitly.

## Current implementation state

Completed:

- created `feature/reference-lab` from current `master`;
- authoritative feature, Gain Map Lab, advanced, second-wave, continuity and chat documentation;
- full Word handoff specification, currently v4 / 20 pages;
- closed superseded Aves draft PR while preserving its history;
- mapped core SingleEdit/history/filter/curve architecture and identified the baked-bitmap migration problem;
- versioned recipe domain model implemented;
- recipe architecture tests implemented;
- Reference Lab CI + FOSS debug APK artifact workflow implemented;
- ImageToolbox draft PR #1 opened;
- shared fullscreen zoom ceiling doubled from 20x to 40x.

Not yet completed:

- recipe is not yet attached to `SingleEditComponent.HistorySnapshot` / editor state;
- recipe persistence/process-death recovery is not yet implemented;
- existing Filter/Curves tools are not yet recipe-native;
- high-precision Reference renderer has not yet been implemented;
- physical 1:1 / true Pixel Inspector has not yet been implemented;
- HDR/Ultra HDR/Gain Map Lab rendering has not yet been implemented;
- current CI run has not yet been confirmed successful;
- no target-device rendering claim has been made.

## Immediate next implementation order

1. finish CI validation of the domain model and fix any compile/test/build failures;
2. attach `EditRecipe` to `SingleEditComponent` state and existing `HistorySnapshot` undo/redo without breaking baked-bitmap compatibility;
3. initialize/update recipe source identity as image sources change;
4. mark dependent gain-map state stale when transitional legacy bitmap edits alter the rendered base;
5. persist/recover recipes without altering originals;
6. audit fullscreen/image-preview source resolution and LOD so 40x zoom never masquerades as higher source detail;
7. audit decode -> bitmap/image -> Compose/GPU -> Android surface -> display for color/precision/HDR losses;
8. add viewer/render telemetry and strict Reference / physical 1:1 / Pixel Inspector behavior;
9. establish high-precision working/color/tiled full-resolution contracts;
10. implement first real pointwise adjustments and comparison controls;
11. implement HDR/Ultra HDR display plus Gain Map Lab creation/extraction/tuning/headroom/validation/export;
12. add spatial/local tools, masks, scopes, RAW and advanced output in roadmap order.

## Validation rule

Do not claim a feature works until build/tests and, for rendering behavior, target-device validation have actually occurred.
