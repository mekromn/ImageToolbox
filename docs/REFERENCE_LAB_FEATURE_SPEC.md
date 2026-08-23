# ImageToolbox Reference Lab — Feature Specification

Date: 2026-08-23

Repository: `mekromn/ImageToolbox`

Working branch: `feature/reference-lab`

Base: `master` at `70cce13f91ba80df9ac11f2c58f3c3bb0d6048d2`

## Mission

Evolve ImageToolbox into a maximum-fidelity reference viewer plus a full non-destructive, high-precision image editor and scientific image laboratory.

The goal is not simply more sliders. The goal is trustworthy viewing, explicit color science, HDR/Ultra HDR, durable edit recipes, professional export, analysis scopes, precise comparison tools, masks, RAW development, and objective validation.

## Hard rules

1. Reference mode must reproduce the source as faithfully as the source, decoder, GPU, Android display pipeline, and panel hardware permit.
2. No hidden beautification in Reference mode: no saturation boost, sharpening, contrast, local tone processing, gamut expansion, skin tuning, or similar aesthetic processing.
3. Creative transformations must be explicit and bypassable.
4. Originals remain untouched unless the user explicitly requests overwrite. Saving edits should persist edit intent/recipe; export renders pixels.
5. Avoid undocumented 8-bit or sRGB intermediate choke points in paths advertised as high precision, wide gamut, or HDR.
6. Pixel buffers must have explicit color-space, transfer-function, alpha, range, bit-depth/precision, and HDR meaning.
7. Preview and export must use the same operation definitions; a control is not complete if it only changes preview UI.
8. Do not activate an HDR surface while feeding an SDR-flattened image and call that HDR support.
9. Fidelity claims require measurement and test images, not only visual preference.
10. Documentation is part of implementation completion.

# 1. Maximum-fidelity viewer

- Reference mode with all creative processing bypassed.
- Maximum Quality mode using the highest-fidelity safe decode/sampling path the device can sustain.
- True physical 1:1 mode: one source pixel maps to one physical display pixel when geometry permits.
- Pixel Inspector mode with source-resolution data, deterministic pixel alignment, nearest/unfiltered inspection, coordinates, and numeric pixel information.
- At least double the normal maximum pinch zoom used by the current relevant viewer/editor surface, with a target of 10x or greater where practical.
- Scale-dependent adaptive resampling rather than blindly using one filter at all scales.
- Progressive preview that automatically resolves to the highest available source detail.
- Seam-safe tiled rendering with source gutters/overlap if reconstruction filters require adjacent texels.
- Wide-gamut preservation through decode, processing, compositing, and output surface.
- Correct HDR and Ultra HDR presentation using actual display headroom.
- Orientation/flip/crop handling that preserves source geometry, color meaning, and HDR metadata.
- Rendering telemetry: source format, bit depth, source color space, transfer, decode format, tile/LOD level, zoom, DPR, surface color mode, HDR state/headroom, frame time, and cache state.

Recommended view modes:

- Reference
- Maximum Quality
- Pixel Inspector
- SDR Reference
- Wide Gamut
- HDR / Ultra HDR
- Adaptive / Creative (explicit enhancement only)

# 2. Non-destructive editing core

ImageToolbox already has `single-edit`, history support, filters, curves, previews, compression, and saving. Reference Lab should extend those systems rather than create a parallel editor.

Required recipe capabilities:

- Versioned `EditRecipe` representation.
- Robust source identity/fingerprint.
- Ordered operation stack.
- Stable operation ID and operation type.
- Enabled state.
- Parameters.
- Per-operation opacity.
- Optional mask reference.
- Undo/redo with continuous-interaction grouping.
- Reorder, duplicate, enable/disable, reset, delete.
- Copy/paste edits between images.
- Presets from whole stacks or subsets.
- Crash/process-death recovery.
- Detect stale recipes when a source is modified/replaced.
- Optional portable sidecars later.
- Preview and full-resolution export consume the same operation definitions.

Comparison tools:

- Hold-to-preview original.
- One-touch full bypass.
- Split before/after.
- Swipe divider.
- Flicker comparison.
- Two-image side-by-side comparison with synchronized pan/zoom.
- Numeric difference metrics later.

# 3. Light controls

- Exposure in linear light using a documented EV model.
- Brightness/perceptual lightness distinct from exposure.
- Contrast around a documented luminance/perceptual pivot.
- Highlights.
- Shadows.
- Whites.
- Blacks.
- Ambiance-style local tonal balancing: local exposure balancing, highlight compression, shadow expansion, adaptive local contrast, restrained chroma compensation.
- Tonal Contrast-style high/mid/low local contrast with shadow/highlight protection and halo resistance.

# 4. Color controls

- Temperature.
- Tint.
- White-balance eyedropper.
- Multiple neutral samples.
- Auto neutral white balance.
- Golden-hour white-balance mode.
- Chromatic-adaptation-based white balance rather than arbitrary RGB multipliers.
- Saturation.
- Vibrance.
- Skin Tone: perceptual skin-locus selection, hue/chroma/luminance control, skin protection, mask preview.
- Blue Tone: perceptual cyan-to-blue selection, hue/chroma/luminance, aqua-blue balance, sky protection, blue clipping protection.
- Adaptive Color / color-volume expansion as explicit creative processing: luminance-preserving, hue-aware, gamut-boundary-aware, neutral-preserving, skin-protected, graceful gamut compression.
- Gamut compression and gamut warning rather than crude clipping.

# 5. Detail controls

- Structure separate from Sharpening.
- Sharpening with halo suppression.
- Fine / Medium / Coarse detail.
- Scale-independent source-resolution spatial processing.
- Noise-aware detail enhancement.
- Denoise.
- Future deconvolution-style sharpening where justified.

# 6. Curves

- Master curve.
- R/G/B curves.
- Luma curve.
- Perceptual curves in OKLab/OKLCH later where useful.
- Numeric control points.
- Presets.
- Live scopes while editing curves.

# 7. Masks

- Brush mask with hardness/feather/opacity/erase.
- Linear gradient.
- Radial gradient.
- Luminance range.
- Hue/color range.
- Skin mask.
- Sky/blue mask.
- Subject/background later.
- Invert/intersect/subtract/combine.
- Mask overlay and grayscale mask preview.
- Every core adjustment should eventually be maskable.

# 8. Scopes and scientific inspection

- RGB histogram.
- Luminance histogram.
- Waveform.
- RGB parade.
- Vectorscope.
- Highlight clipping overlay.
- Shadow clipping overlay.
- Gamut clipping/out-of-target-gamut overlay.
- False color.
- HDR nit-level / relative-headroom overlay.
- Pixel inspector with coordinates, encoded RGB, linear RGB, luminance, OKLab/OKLCH, alpha, profile, transfer, HDR/gain-map state.
- Delta-E / difference-map comparison later.

Calibration/test mode:

- Gray ramp and near-black ramp.
- Smooth gradients / banding tests.
- Gamut sweeps.
- Color patch targets.
- HDR highlight ramp.
- Black-level test.
- Display clipping test.
- Reference patterns shown with no image enhancement.

# 9. HDR / Ultra HDR / wide gamut

- Detect HDR transfers and wide-gamut metadata.
- Detect gain-map/Ultra HDR content.
- Correct display-headroom-aware Ultra HDR presentation.
- Inspect SDR base and gain map separately.
- Preserve gain maps through supported spatial transforms.
- Do not blindly reuse a stale gain map after pixel edits.
- Edited Ultra HDR pipeline: reconstruct HDR working representation -> high-precision edits -> edited HDR -> derive SDR rendition -> regenerate gain map -> encode.
- JPEG/R Ultra HDR export.
- Evaluate HEIC Ultra HDR where Android/platform encoders support it.
- Preserve metadata/profile semantics where formats permit.
- HDR scopes must analyze the HDR representation, not only the SDR base.
- Never silently flatten to SDR.

Ultra HDR acceptance rule: retaining a gain-map chunk is not sufficient. The resulting HDR rendition must remain consistent with the edited SDR/HDR relationship.

# 10. RAW / DNG

- Native RAW/DNG development path.
- High-quality demosaic.
- Metadata-derived and manual white balance.
- Camera color transform into chosen working space.
- Highlight recovery.
- Noise reduction before sharpening.
- Lens correction where supported.
- RAW development stage stays logically separate from post-development edit operations.

# 11. Export

Preserve and extend ImageToolbox's existing strengths:

- Chosen folder.
- Beside original.
- One-time destination.
- New copy / explicit overwrite policy.
- Export profiles.
- Filename templates.
- Preserve/selectively strip metadata.
- Preserve ICC/color metadata where possible.
- JPEG size/quality estimation.
- PNG/JPEG/WebP/HEIC/AVIF/TIFF/JXL and other genuinely supported formats.
- High-bit-depth output where format + encoder actually support it.
- Ultra HDR JPEG/R and later HEIC Ultra HDR.
- Explicit resize/resampling choice.
- Batch export with per-file results.
- One final lossy encode whenever possible.

# 12. Touch-first editor UX

- Collapsible Light / Color / Detail / Curves / Masks / Analyze / View groups.
- Large touch targets.
- Hold image for original without blocking pinch-to-zoom/multitouch.
- One-tap bypass.
- Double-tap control label/value to reset.
- Numeric entry.
- Fine-adjust mode.
- Haptic zero/neutral detent.
- Visible edit stack/history.
- Control-group jump navigation.
- Preset morphing/interpolation later.
- Destructive action confirmation.
- Fullscreen image-first layout with collapsible controls.
- Preserve pan/zoom state when changing controls.

Recommended groups:

- LIGHT: Exposure, Brightness, Contrast, Highlights, Shadows, Whites, Blacks, Ambiance, Tonal Contrast.
- COLOR: Temperature, Tint, Saturation, Vibrance, Skin Tone, Blue Tone, Adaptive Color, Gamut Expansion.
- DETAIL: Structure, Sharpening, Fine/Medium/Coarse Detail, Denoise.
- CURVES: Master, R, G, B, Luma, perceptual curves later.
- MASKS: Brush, gradients, luminance, color/hue, skin, sky/blue.
- ANALYZE: Histogram, waveform, parade, vectorscope, clipping, false color, pixel inspector.
- VIEW: Reference, Maximum Quality, Pixel Inspector, SDR, Wide Gamut, HDR/Ultra HDR, Creative.

# 13. Test corpus and automated validation

Permanent reference corpus should include:

- sRGB JPEG.
- Wide-gamut / Display P3 image.
- 8-bit and high-bit-depth PNG/TIFF.
- HEIC/HEIF.
- AVIF.
- JXL where supported.
- RAW/DNG.
- Ultra HDR JPEG/R.
- Very large panorama/image.
- Skin, foliage, saturated cyan/blue/red targets.
- Near-black, highlight, and smooth-gradient stress images.

Automated regression should include where meaningful:

- Pixel error / RMSE.
- PSNR.
- SSIM.
- Delta-E for color transforms.
- Golden tests for masks/operation ordering.
- CI failure for unexpected Reference-path changes.
- Separate performance/memory regression tracking.

Performance rule: temporary low-resolution previews are allowed while higher-fidelity data loads, but the best available representation must automatically replace the fallback when ready.

# 14. Implementation order

## Phase 0 — continuity + architecture map

- Create `feature/reference-lab` from current `master`.
- Add authoritative docs and chat-derived requirements.
- Map current `single-edit`, `filters`, `curves`, `image-preview`, `compare`, `core:filters`, image decode/transform/compress, and saving architecture.

## Phase 1 — non-destructive recipe integration

- Define versioned recipe model compatible with existing SingleEdit/history patterns.
- Persist recipes and recover sessions.
- Keep existing export/save system as the output layer.

## Phase 2 — Reference viewer audit + diagnostics

- Trace source -> decode -> Android image/bitmap -> Compose/GPU -> output surface -> display.
- Implement/verify physical 1:1 and Pixel Inspector.
- Add render/color/HDR telemetry.

## Phase 3 — high-precision color pipeline

- Define working representations and color contracts.
- Verify wide-gamut and FP16/FP32 behavior end-to-end.
- Correct display transform/output-surface behavior.

## Phase 4 — first real Light/Color operations

- Exposure, contrast, highlights, shadows, whites, blacks.
- Temperature/tint.
- Saturation/vibrance.
- Hold-original, bypass, split comparison.

## Phase 5 — HDR / Ultra HDR

- Correct HDR presentation.
- Ultra HDR detection/inspection/preservation.
- Regenerated gain maps and JPEG/R export.

## Phase 6 — spatial/local tools

- Structure, sharpening, detail bands.
- Tonal Contrast.
- Ambiance.
- Denoise/noise-aware processing.

## Phase 7 — selective color + masks

- Skin Tone.
- Blue Tone.
- Brush/gradient/luminance/hue masks and mask composition.

## Phase 8 — analysis + comparison lab

- Scopes.
- Clipping/false color/HDR overlays.
- Synchronized comparison, flicker, difference tools.

## Phase 9 — RAW + professional output

- RAW/DNG development.
- High-bit-depth/HDR-aware export profiles.
- Batch editing/export.

# Definition of done

A feature is not complete until:

- code is implemented;
- behavior is documented;
- relevant tests exist;
- failure/fallback behavior is explicit;
- preview and export behavior is validated where applicable;
- color/HDR meaning is documented where pixels are involved;
- project state and continuity docs are updated.
