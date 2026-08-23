# ImageToolbox Reference Lab — Second-Wave Requirements

Date: 2026-08-23

Status: Approved scope. These requirements extend `REFERENCE_LAB_FEATURE_SPEC.md`, `REFERENCE_LAB_ADVANCED_REQUIREMENTS.md`, and `GAIN_MAP_LAB.md`.

## Purpose

These features were approved immediately before implementation began. They are intentionally separated from the early architecture locks: they do not all block the first renderer/recipe work, but they are part of the intended Reference Lab product and must remain compatible with the foundational design.

## 41. Lossless / passthrough editing

When pixels do not need to change, do not decode/re-encode them merely because the file passes through the editor.

Required behavior:

- metadata-only changes remain metadata-only where the container permits it;
- orientation/rotation/crop should use lossless coded-domain transforms when the format genuinely supports them and constraints are satisfied;
- unchanged encoded payloads/regions should be preserved when practical;
- the UI must distinguish truly lossless operations from visually lossless re-rendering;
- operations advertised as lossless must be objectively verified.

## 42. Professional Color Mixer

Add a high-precision color-mixing workspace with explicit working-space semantics:

- Hue / Saturation / Lightness by color range;
- Selective Color-style controls;
- Channel Mixer;
- split toning / color grading;
- Lift / Gamma / Gain color wheels;
- range overlap/feather controls;
- gamut warning/compression integration;
- mask support;
- no hidden creative color mixing in Reference mode.

## 43. Real soft proofing

Reference Lab should simulate target output behavior before export:

- sRGB;
- Display P3;
- Rec.2020;
- custom ICC profiles;
- rendering intent selection where supported;
- black-point compensation where meaningful;
- target-paper/display black/white behavior when known;
- before/after gamut compression;
- out-of-gamut overlay;
- explicit statement of which transform/profile is being simulated.

Soft proofing is a view/analysis transform and must not modify the edit recipe unless the user explicitly bakes a corresponding output transform.

## 44. Multi-point scientific color sampler

Allow multiple persistent sample points and sampled regions.

Each sample can report, where mathematically meaningful:

- source coordinate;
- encoded RGB/A;
- linear RGB;
- XYZ;
- xyY;
- Lab;
- OKLab / OKLCH;
- luminance / relative headroom;
- HDR/gain-map contribution;
- Delta-E against another sample/reference;
- before/after values while editing.

Samples must remain attached to stable source coordinates across zoom/pan and compatible geometry edits.

## 45. Non-destructive Healing / Clone / Remove

Add repair tools as recipe operations rather than automatically baking pixels:

- healing brush;
- clone stamp with tracked source coordinates;
- spot removal;
- content-aware/object removal later;
- source/target overlay;
- feather/opacity controls;
- operation/mask ordering;
- deterministic export behavior;
- full-resolution source-space rendering independent of preview zoom.

## 46. Image-defect correction

Add technically oriented restoration controls:

- luminance denoise;
- chroma denoise;
- hot/dead-pixel removal;
- banding reduction;
- moiré suppression;
- chromatic-noise controls;
- optional sensor-pattern/fixed-pattern correction where valid data exists;
- before/after and high-zoom inspection support.

These operations must be positioned correctly relative to RAW development, sharpening and resampling.

## 47. Operation blend modes

In addition to opacity, recipe operations should support selected blend modes with explicit color-space semantics.

Initial useful modes:

- Normal;
- Luminosity;
- Color;
- Hue;
- Saturation;
- selected technical/creative modes later.

Rules:

- define the domain/space in which each blend mode operates;
- do not inherit ambiguous legacy Photoshop-like behavior without documenting it;
- preserve alpha correctly;
- masks and opacity apply predictably before/after blending according to the documented operation contract.

## 48. Dodge & Burn workspace

Provide local exposure/lightness painting using the common mask/recipe system:

- dodge;
- burn;
- EV-oriented amount where appropriate;
- shadows/midtones/highlights targeting;
- protect color/chroma option;
- feather/flow/opacity;
- pressure support where available;
- cumulative vs bounded stroke behavior;
- mask visualization;
- non-destructive re-editing.

## 49. Profile assignment vs color conversion

The color-management UI must explicitly separate two fundamentally different actions:

### Assign / reinterpret profile

Change the metadata/interpretation of existing numeric pixel values without transforming those values.

### Convert profile

Transform pixel values so the appearance is preserved in a different color space/profile.

Requirements:

- clear warnings for profile assignment because it changes interpretation/appearance;
- preview before commit;
- preserve source profile provenance;
- use for repairing incorrectly tagged files without conflating repair with conversion;
- recipe/audit record stores which action occurred and which profiles were involved.

## 50. Bit-exact / lossless output verification

For operations or workflows advertised as lossless, verify the claim rather than inferring it.

Possible validation:

- encoded payload hash comparison when payload preservation is expected;
- pixel hash comparison after decode when encoded representation may legitimately change but pixels must remain exact;
- untouched-region comparison for partial coded-domain operations where feasible;
- metadata diff report;
- explicit PASS / FAIL / NOT VERIFIABLE result.

The term `lossless` must never be used as a marketing approximation.

# Integration rules

These second-wave features must inherit the Reference Lab architecture contracts already locked elsewhere:

- versioned operation algorithms;
- deterministic recipes;
- stable source-coordinate geometry/mask semantics;
- explicit scene/display/perceptual/encoded processing stage;
- profile provenance/conflict handling;
- correct alpha/premultiplication;
- full-resolution tiled equivalence;
- preview/export conformance;
- explicit quality fallback telemetry;
- non-destructive gain-map dependency tracking when an operation changes the SDR/HDR relationship.

# Priority guidance

The following should be enabled by the foundation from day one even if their UI/algorithms land later:

1. operation blend-mode field and versioning extensibility;
2. stable coordinate model for healing/clone/dodge/burn/sample points;
3. profile assignment vs conversion representation;
4. lossless/passthrough capability flags and validation result model;
5. analysis samples as non-pixel-changing project state;
6. soft-proof/output-transform separation from actual edit operations.

# Definition of done

A second-wave feature is complete only when its code, operation semantics, color/alpha domain, preview/export equivalence, tests, fallback behavior and continuity documentation are all updated together.