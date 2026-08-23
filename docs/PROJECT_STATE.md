# ImageToolbox Reference Lab — Project State

Last updated: 2026-08-23

## Repository

- Active repository: `mekromn/ImageToolbox`
- Base/default branch: `master`
- Working branch: `feature/reference-lab`
- Base commit: `70cce13f91ba80df9ac11f2c58f3c3bb0d6048d2`
- Active draft PR: `#1` — **Reference Lab foundation: versioned non-destructive recipe architecture**
- Current PR head at this update: `72a80e5c8d9136ef8c21b644aa92875bf4dc18cb`
- Superseded Aves work remains preserved in `mekromn/aves-editor`; its draft PR #1 was closed rather than deleted.

## Documentation resume order

1. `docs/PROJECT_STATE.md`
2. `docs/CONTINUITY_PROTOCOL.md`
3. `docs/REFERENCE_LAB_FEATURE_SPEC.md`
4. `docs/REFERENCE_LAB_ADVANCED_REQUIREMENTS.md`
5. `docs/REFERENCE_LAB_SECOND_WAVE_REQUIREMENTS.md`
6. `docs/GAIN_MAP_LAB.md`
7. latest `docs/chat/*.md`
8. current draft PR / branch history

## P0 product rules

1. Maximum-fidelity viewing to the limits of source, decoder, GPU, Android display path and panel hardware.
2. Untouched **Reference** viewing receives no hidden beautification.
3. Editing is non-destructive: recipe/edit intent is authoritative; rendered bitmaps are derived artifacts.
4. Wide gamut, HDR and Ultra HDR are first-class paths.
5. Gain Map Lab can create, derive, extract, edit, tune, analyze, validate and export gain maps.
6. Preview and final export use the same mathematical operation definitions.
7. Fidelity fallbacks are explicit rather than silent.
8. Every operation has stable semantics and an algorithm version so old recipes do not silently change when implementations improve.
9. Every important decision, failure, experiment, test and chat-driven requirement is documented for 101% continuity.

Full approved scope is split between:

- `REFERENCE_LAB_FEATURE_SPEC.md`;
- advanced requirements 1–40;
- second-wave requirements 41–50;
- `GAIN_MAP_LAB.md`.

## Existing ImageToolbox architecture being extended

ImageToolbox already provides native Android/Kotlin/Compose modules for `single-edit`, filters, curves, image preview, compare, codecs, image transformation/compression and mature saving/export.

`SingleEditComponent` currently owns source/working/preview bitmap state, ImageInfo, filters, curves, crop/rotation/flip, draw/erase, metadata, presets, export profiles and history.

Important existing limitation:

`SingleEditComponent.HistorySnapshot` currently contains a cached rendered URI plus image/export settings. Existing Filter/Curves subtools may render a new cached PNG/bitmap and make that the working image. This must remain only a transitional compatibility path.

Migration rule:

`source -> versioned EditRecipe -> preview renderer -> full-resolution renderer -> existing ImageToolbox save/export layer`

Cached/baked bitmaps are disposable acceleration or legacy compatibility artifacts, never the long-term authoritative representation of Reference Lab edits.

## IMPLEMENTED — versioned recipe domain model

File:

`core/domain/src/main/kotlin/com/t8rin/imagetoolbox/core/domain/image/editing/EditRecipe.kt`

Initial commit: `20045d659ff9d1e092d1c63bc04ba207bab2a67b`

Schema v1 already contains architecture that would be expensive to retrofit later:

- recipe schema version;
- source identity/fingerprint fields;
- ordered edit operations;
- stable operation ID/type;
- `algorithmId` + `algorithmVersion`;
- enabled/opacity;
- blend mode;
- explicit processing stage: Encoded / SceneReferredLinear / DisplayReferred / Perceptual / Geometry;
- typed parameter tree;
- mask reference;
- lossless intent;
- stable source-normalized mask coordinates;
- dual SDR/HDR rendition edit mode;
- HDR Master state;
- first-class Gain Map recipe state;
- gain-map generator algorithm/version;
- gain-map Current/Stale dependency state and stale reason;
- SDR/HDR reference-pair identity;
- gain-map operation stack;
- gain-map metadata;
- luminance or RGB gain-map channel mode;
- gain-map direction;
- distinct semantic operations for profile assignment vs profile conversion.

The model provides append/replace/remove/move helpers and can invalidate a gain map without deleting the user's map edits.

## IMPLEMENTED — recipe history

File:

`core/domain/src/main/kotlin/com/t8rin/imagetoolbox/core/domain/image/editing/EditRecipeHistory.kt`

Commit: `0245b75cbeb827ae37f6ba45b9c5e4bd2e0dd201`

Behavior:

- bounded undo/redo;
- begin/end continuous interaction grouping so a slider drag is one undo entry;
- cancel interaction;
- restore persistent authoritative state without retaining stale session undo history;
- whole-recipe history naturally includes gain-map dependency state.

## IMPLEMENTED — durable recipe repository contract

File:

`core/domain/src/main/kotlin/com/t8rin/imagetoolbox/core/domain/image/editing/EditRecipeRepository.kt`

Important correction commit: `b781ae0b17ff9a8318b4d477fa5ea468de19e1b1`

The repository loads against the **current full `EditSourceIdentity`**, not merely a URI, so storage can report whether a found recipe matches the current source fingerprint.

Stale work is returned for recovery/rebinding rather than silently deleted.

`EditSourceIdentity.matches(...)` accepts partial current metadata but rejects any known mismatch in URI, dimensions, MIME type, size, modified time or content hash.

## IMPLEMENTED — typed JSON serialization

ImageToolbox already uses Moshi through `core:data`; no new serialization framework was introduced.

New file:

`core/data/src/main/java/com/t8rin/imagetoolbox/core/data/json/EditValueJsonAdapter.kt`

Commit: `3abda3e5531d5631129d1c4278cf7218494d4be9`

Registered in the existing Moshi graph in commit:

`d0c72bd55298d25a2ad89cb40568b2d4811ab4f9`

The adapter stores explicit type tags for Decimal, Integer, Flag, Text, List, Object and Null so generic JSON number coercion cannot silently change recipe parameter types.

## IMPLEMENTED — crash-recoverable app-private recipe persistence

File:

`core/data/src/main/java/com/t8rin/imagetoolbox/core/data/image/AndroidEditRecipeRepository.kt`

Initial commit: `7e1b4e52355215bd71d261c354a9a433f374ac4c`

Crash-recovery hardening commit: `d57c0b3f0eeb9425d05b1b3eb794175629890a72`

Hilt binding:

`core/data/src/main/java/com/t8rin/imagetoolbox/core/data/di/EditRecipeModule.kt`

commit `a3b83fada3d20796c507c79edd4257957c5968ff`.

Storage behavior:

- app-private `filesDir/reference_lab/recipes`;
- intentionally separate from preview/image caches;
- source URI mapped to stable SHA-256 filename;
- writes serialized JSON to same-directory temp file and `fd.sync()`s it;
- guarded by coroutine `Mutex`;
- previous complete document is renamed to backup before new temp file moves into place;
- load falls back to backup if a crash happens between replacement renames;
- malformed/stale data is not silently deleted;
- delete cleans final/temp/backup documents.

ImageToolbox minSdk is 24, so this design intentionally does not depend on API-26-only `java.nio` atomic-move APIs.

Persistence exists in `core:data` but is **not yet wired into SingleEditComponent**.

## IMPLEMENTED — tests

Domain test file:

`core/domain/src/test/kotlin/com/t8rin/imagetoolbox/core/domain/image/editing/EditRecipeTest.kt`

Coverage includes:

- operation order;
- algorithm-version state;
- gain-map invalidation preserving map edits;
- blend mode and lossless intent fields;
- source-normalized masks;
- profile assignment vs conversion distinction;
- recipe emptiness;
- grouped continuous interaction;
- undo/redo including gain-map dependency state;
- cancel interaction;
- partial source fingerprint matching and known mismatch rejection.

Full serialization test:

`core/data/src/test/java/com/t8rin/imagetoolbox/core/data/json/EditRecipeJsonRoundTripTest.kt`

Commit: `84fc15825a0e121a0b0ebfd71caf836bc765df5a`

It round-trips a complete recipe containing nested typed values, masks, SDR/HDR rendition state and detailed RGB gain-map state through Moshi and requires exact model equality.

`core:data` unit-test dependency was enabled in commit `2dbcda8f985634df6f9a91a84152606ec62e5e41`.

## IMPLEMENTED — Reference Lab CI / FOSS test APK

Workflow:

`.github/workflows/reference_lab.yml`

The first workflow mistakenly used ambiguous Gradle task `testDebugUnitTest`; ImageToolbox has FOSS and Market variants. Run `32634707730` therefore failed before compiling the recipe tests. This was a CI task-selection error, not a recipe-code failure.

Fix commit:

`807d3c2829b0c6319844d133ea80e56135aa679a`

The workflow now uses Temurin JDK 21 and explicit FOSS test tasks.

Later commit `7477b41dd8628c50aabe6d87405c22ca5f0aff8f` expanded CI to execute both:

- `:core:domain:testFossDebugUnitTest`
- `:core:data:testFossDebugUnitTest`

then build:

- `:app:assembleFossDebug`

and upload `Reference-Lab-FOSS-Debug-APK`.

Validation state at this documentation update:

- earlier corrected run `32634938193`: core/domain tests completed **successfully** and its FOSS APK build was still in progress at last observation;
- that run predates the newest persistence/serialization/viewer commits;
- current branch head `72a80e5c8d9136ef8c21b644aa92875bf4dc18cb` has Reference Lab CI run `32635366253` **queued**;
- therefore the newest head is **not yet claimed green** and no latest APK is claimed available.

## IMPLEMENTED — doubled zoom ceilings

### Shared zoom modal

`EnhancedZoomableModalBottomSheet` changed from 20x to 40x.

Commit: `6dfbb0d25a4700644e85fd9b827afb4efe44ee36`

### Primary gallery ImagePager

Viewer audit found the primary ImagePager had an independent `rememberZoomState(20f)` and was therefore not affected by the shared-sheet change.

It is now also 40x.

Commit: `72a80e5c8d9136ef8c21b644aa92875bf4dc18cb`

Both implementations explicitly note that gesture magnification does not imply additional source detail. Source-aware decode/LOD and physical 1:1 Pixel Inspector are still required.

## VIEWER AUDIT — important current findings

### `Picture` / Coil path

`core/ui/.../widget/image/Picture.kt` already contains optional Ultra HDR scaffolding:

- Android 14+ `Bitmap.hasGainmap()` detection;
- optional `enableUltraHDRSupport`;
- dynamic Activity window `COLOR_MODE_HDR` / default switching;
- primary `ImagePager` enables this Ultra HDR option.

However the current path is **not yet accepted as Reference-grade HDR**. It performs gain-map/window decisions through Coil/Bitmap behavior, includes a delayed window-mode switch in a transformation, and still needs end-to-end verification that source gain-map/color/precision information survives decode and Compose/display correctly.

Do not claim correct Ultra HDR presentation yet.

### Source-detail / LOD audit still open

The primary ImagePager passes the source URI through `Picture`/Coil with `ContentScale.Fit`. The actual decode-size/LOD behavior at extreme zoom still needs to be traced. 40x is currently a navigation ceiling, not proof of source-resolution sampling.

## Word handoff specification

Current external feature document:

`ImageToolbox_Reference_Lab_Feature_Specification_v4.docx`

- 20 pages;
- includes approved requirements 1–50 and Gain Map Lab;
- rendered page-by-page and visually inspected after modification.

## Current completed state

- branch + draft PR established;
- all approved feature/architecture/continuity docs established;
- versioned recipe model implemented;
- recipe undo/redo/history engine implemented;
- durable repository contract implemented;
- typed Moshi recipe serialization implemented;
- crash-recoverable app-private recipe repository implemented and Hilt-bound;
- domain and full JSON round-trip tests added;
- CI + FOSS debug APK workflow added;
- domain recipe/history tests have passed on an earlier corrected head;
- primary gallery pager and shared fullscreen sheet doubled from 20x to 40x;
- first viewer/Ultra HDR path audit underway.

## Not yet complete / do not claim

- `EditRecipe` is not yet attached to `SingleEditComponent.HistorySnapshot` / live editor state;
- repository persistence is not yet invoked by SingleEdit;
- existing Filter/Curves/Draw/Crop tools are not recipe-native yet;
- high-precision Reference renderer is not implemented yet;
- source-aware zoom LOD is not verified/implemented;
- true physical 1:1 / Pixel Inspector is not implemented yet;
- wide-gamut end-to-end correctness is not verified;
- HDR/Ultra HDR Reference correctness is not verified;
- Gain Map Lab generation/editing/export is not implemented yet;
- new Light/Color/Detail adjustment UIs are not exposed yet;
- newest CI head has not yet been confirmed successful;
- no target-device rendering fidelity claim has been made.

## Immediate next implementation order

1. let latest CI validate domain + core:data serialization/persistence changes and fix real failures;
2. attach recipe state to `SingleEditComponent` and its existing history snapshots without breaking legacy baked-bitmap tools;
3. initialize/load/save recipe source identity when the editor source changes and autosave authoritative changes;
4. mark an existing gain map stale whenever a transitional baked-bitmap edit changes the SDR/HDR relationship;
5. trace Coil decode sizing / image-loader configuration and establish source-aware LOD for the 40x pager;
6. audit decode precision, color-profile handling, Compose/GPU surface and Android HDR/wide-gamut output;
7. add render telemetry and strict Reference / physical 1:1 / Pixel Inspector behavior;
8. define and validate high-precision/tiled full-resolution render contracts;
9. implement first real pointwise Light/Color recipe operations and live comparison controls;
10. implement correct HDR/Ultra HDR presentation and then Gain Map Lab creation/extraction/tuning/headroom/validation/export;
11. proceed through spatial tools, masks, scopes, RAW and advanced output in documented roadmap order.

## Validation rule

Do not claim a feature works until its relevant build/tests have passed and, for rendering/color/HDR behavior, target-device validation has occurred.
