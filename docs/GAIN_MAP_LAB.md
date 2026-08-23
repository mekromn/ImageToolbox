# ImageToolbox Reference Lab — Gain Map Lab

Date: 2026-08-23

Status: P0 feature specification

## Purpose

Gain Map Lab makes gain maps a first-class editable asset rather than a hidden by-product of Ultra HDR export.

The user must be able to:

- create a gain map for an SDR image;
- derive a gain map from an SDR/HDR pair;
- extract and inspect the gain map and metadata from an existing Ultra HDR image;
- edit an existing gain map directly;
- tune gain-map metadata and display behavior;
- locally paint/mask gain-map changes;
- preview the SDR base, gain map and reconstructed HDR rendition independently;
- export a valid backward-compatible Ultra HDR image;
- regenerate a gain map after normal image edits so HDR and SDR renditions remain consistent.

## Inspiration: GlowHDR

Glow HDR: Ultra HDR converter is an explicit product inspiration for the fast workflow. Its useful ideas include:

- one-step SDR-to-Ultra-HDR conversion;
- automatic/smart gain-map generation;
- a simple gain-map intensity control;
- HDR-screen preview;
- backward-compatible SDR presentation;
- accepting ordinary JPEG/PNG/HEIC input.

Reference Lab must not copy proprietary implementation details. It should independently implement the behavior using documented gain-map math, Android platform APIs, objective tests and our own algorithms.

The intended product direction is **GlowHDR simplicity when desired, plus a much deeper expert Gain Map Lab when opened**.

## Platform model

Android `android.graphics.Gainmap` represents an enhancement layer plus metadata used to reconstruct an HDR rendition from a base image.

The platform supports a 1-channel or 3-channel gain-map image. Relevant metadata includes:

- ratio min: R/G/B;
- ratio max: R/G/B;
- gamma: R/G/B;
- epsilon SDR: R/G/B;
- epsilon HDR: R/G/B;
- minimum display HDR/SDR ratio for beginning the HDR transition;
- display HDR/SDR ratio for full HDR;
- gain-map image contents;
- Android 16+: gain-map direction (SDR -> HDR or HDR -> SDR);
- Android 16+: alternative image primaries for applying gain-map math.

For single-plane gain maps, per-channel metadata values should normally remain equal unless a format/algorithm explicitly defines otherwise.

## Input modes

### 1. Automatic SDR -> Ultra HDR

Input:

- JPEG;
- PNG;
- HEIC/HEIF;
- other decoded SDR formats where the output encoder can create a valid gain-map image.

Workflow:

1. analyze the source in a defined linear/perceptual representation;
2. create a target HDR rendition;
3. derive the gain map from the SDR base and target HDR rendition;
4. optimize/smooth the map while preserving edges and avoiding halos;
5. select safe metadata defaults from the actual derived range;
6. preview at real and simulated display headroom;
7. export Ultra HDR.

Provide a one-tap **Smart Gain Map** preset inspired by GlowHDR's simple workflow, but keep every generated parameter editable afterward.

### 2. SDR + HDR reference pair

This is the preferred reference-grade creation path when matching SDR and HDR renditions are available.

Requirements:

- register/validate dimensions and orientation;
- optional alignment if the pair is not pixel-identical;
- convert both images into an agreed color/linear-light representation;
- derive the gain field mathematically from the pair;
- handle zeros/near-black robustly with epsilon terms;
- allow 1-channel luminance or 3-channel RGB gain derivation;
- report reconstruction error between the supplied HDR reference and the gain-map reconstruction.

### 3. Existing Ultra HDR / gain-map image

Extract and show:

- SDR/base image;
- gain-map contents;
- gain-map resolution relative to base;
- channel count;
- ratio min/max;
- gamma;
- SDR/HDR epsilon;
- transition/full-HDR display ratios;
- direction and alternative primaries when available;
- reconstructed HDR preview.

The user can modify the map or metadata non-destructively and save as a new Ultra HDR file.

### 4. HDR source -> SDR base + gain map

For a true HDR input:

1. preserve the HDR source as the reference rendition;
2. derive or let the user tune an SDR base rendition;
3. calculate the gain map needed to reconstruct the HDR reference from that SDR base;
4. optimize map resolution/precision and metadata;
5. validate reconstruction.

This mode is important for converting HDR stills into a backward-compatible gain-map format without throwing away the original highlight structure.

## Quick controls

The default Gain Map Lab view should be simple enough for casual use:

- **Smart Auto** — regenerate a sensible gain map automatically;
- **HDR Strength** — global interpolation/scaling of gain-map effect;
- **Highlight Glow** — increases/decreases gain emphasis in the high-luminance regions without indiscriminately lifting shadows;
- **Shadow Protection** — prevents generated gain from unnaturally lifting deep shadows;
- **Rolloff** — controls how smoothly gain enters highlights and approaches maximum gain;
- **Smoothness** — edge-aware map smoothing/regularization;
- **HDR Headroom Preview** — choose actual display headroom or simulate a target ratio;
- **Reset**;
- **Before/After** / hold-to-original.

## Expert gain-map image controls

### Tone/shape

- map exposure/bias;
- black point;
- white point;
- map gamma;
- map contrast;
- map levels;
- arbitrary map curve;
- highlight threshold;
- transition width/soft knee;
- upper rolloff;
- shadow floor/protection;
- maximum local gain clamp;
- minimum local gain clamp.

### Spatial behavior

- edge-aware smoothing;
- blur radius where appropriate;
- edge preservation;
- halo suppression;
- local contrast regularization;
- map-resolution selector/diagnostic;
- high-quality resize filter for the gain map;
- seam/edge validation.

### Channel behavior

Modes:

- monochrome/luminance gain map;
- RGB gain map when supported/desired;
- per-channel curve/levels for RGB maps;
- link/unlink RGB controls;
- chroma-change warning when independent RGB gain materially alters hue/chroma.

## Local gain-map editing

Gain-map editing should use the same non-destructive mask system as the main editor.

Required tools:

- brush gain up/down;
- erase/reset local gain;
- linear gradient;
- radial gradient;
- luminance-range selection;
- hue/color-range selection;
- highlight-range mask;
- shadow-protection mask;
- skin-protection mask;
- subject/sky masks later;
- invert/intersect/subtract/combine.

Local edits modify gain-map intent, not the SDR base, unless the user explicitly switches to base-image editing.

## Metadata tuning panel

Expose an **Advanced Metadata** panel with validated numerical controls for:

- Ratio Min R/G/B;
- Ratio Max R/G/B;
- Gamma R/G/B;
- Epsilon SDR R/G/B;
- Epsilon HDR R/G/B;
- Min Display Ratio for HDR Transition;
- Display Ratio for Full HDR;
- Android 16+: Gain Map Direction;
- Android 16+: Alternative Image Primaries.

Rules:

- show human-readable interpretation next to raw values;
- validate platform/format limits before export;
- offer safe auto-derived values;
- allow expert override;
- warn when metadata no longer matches the actual map contents;
- provide a **Re-fit Metadata to Map** command that derives appropriate limits from the edited map and SDR/HDR relationship.

## Preview and analysis modes

Gain Map Lab must provide instant switching between:

- SDR Base;
- Gain Map grayscale/RGB;
- Gain Map heatmap;
- Reconstructed HDR;
- SDR/HDR split view;
- Original vs edited gain map;
- Original vs reconstructed HDR;
- simulated headroom values;
- actual device headroom.

Analysis overlays:

- gain histogram;
- min/mean/max gain;
- effective stops/EV of local boost;
- clipped gain-map pixels;
- HDR clipping;
- SDR clipping;
- reconstruction error map for SDR+HDR pair mode;
- gamut warning;
- pixel inspector reporting base RGB, gain-map value(s), metadata interpolation state and reconstructed HDR RGB/luminance.

## Display-headroom simulator

The preview must not be limited to 'HDR on/off'.

Provide a headroom control that evaluates the gain map at different HDR/SDR display ratios, including:

- 1.0x SDR;
- transition region;
- intermediate HDR headroom;
- full-HDR ratio from metadata;
- actual current device ratio when available;
- user-entered/custom ratio for diagnostics.

This lets the user see whether a map behaves gracefully across different HDR displays rather than only looking good on one phone.

## Smart gain-map generation

Smart Auto should be transparent and reproducible, not a hidden magic filter.

Candidate inputs to the algorithm:

- scene luminance distribution;
- highlight percentile structure;
- local contrast;
- specular-highlight likelihood;
- shadow and midtone preservation;
- saturation/chroma near highlights;
- skin-tone protection;
- noise level;
- existing clipping;
- available/target HDR headroom.

Possible presets:

- Natural;
- Balanced;
- Highlight Pop;
- Specular Glow;
- Portrait Safe;
- Night Lights;
- Landscape;
- Maximum Headroom.

Presets are starting points only; every resulting parameter remains editable.

## Non-destructive representation

Gain Map Lab state belongs in the same versioned edit recipe architecture as normal edits.

Recipe data should include:

- gain-map source mode;
- source identities for SDR/HDR pair inputs when applicable;
- generation algorithm/version;
- map image/derived representation or deterministic generation parameters;
- map edit operation stack;
- masks;
- metadata values;
- preview headroom preference;
- output format/profile settings.

The original image and original embedded gain map remain untouched until explicit export/overwrite.

## Interaction with normal image edits

If the SDR base or HDR rendition changes after gain-map creation, the old gain map may become invalid.

Reference Lab must track dependency state and do one of the following explicitly:

1. regenerate the gain map from the edited SDR/HDR relationship;
2. re-fit the existing map and metadata when mathematically valid;
3. mark the map stale and block Reference-grade Ultra HDR export until resolved.

Never silently retain a gain map that no longer describes the edited image pair.

## Export

Primary target:

- standards/platform-compatible Ultra HDR JPEG/JPEG-R.

Later/where platform support is proven:

- HEIC/HEIF gain-map HDR;
- other ISO 21496-1-compatible containers supported by codecs/platform.

Export validation should verify:

- base image decodes correctly as SDR without gain-map support;
- gain map exists after encode;
- metadata round-trips;
- HDR reconstruction remains within defined error limits;
- orientation/profile/metadata are correct;
- file reopens in ImageToolbox as a valid gain-map image;
- file displays correctly in known Android Ultra HDR consumers.

## Definition of done

Gain Map Lab is not complete until all of the following are true:

- an SDR image can produce a valid Ultra HDR output;
- existing gain maps can be extracted and inspected;
- gain-map image data can be edited non-destructively;
- metadata can be tuned and validated;
- SDR+HDR pair derivation works with measurable reconstruction error;
- real and simulated display-headroom preview works;
- map/base/HDR comparison modes work;
- normal edits correctly invalidate/regenerate dependent gain maps;
- export is backward compatible in SDR;
- automated tests cover gain-map math and metadata round-trip;
- target-device tests validate HDR behavior;
- all algorithms, assumptions and fallbacks are documented.
