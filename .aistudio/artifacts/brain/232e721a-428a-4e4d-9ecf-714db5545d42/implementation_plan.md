# Implementation Plan: Color-Coded Traffic-Light Ingredient Safety Badge System

## Goal
Implement a color-coded **Traffic-Light Safety Badge** system (`Green` / `Yellow` / `Red`) that appears next to individual ingredients whenever a product is analyzed. Each badge combines a custom traffic-light signal icon with a short severity label pill, classifies unverified/uncertain OCR tokens with a **Yellow caution badge + `UNVERIFIED` tag**, and supports a **tap-to-view hazard tooltip** across all report ingredient lists.

---

## Confirmed Design Decisions
1. **Badge Visual Style**: Traffic-light icon (3-lens vertical/horizontal signal housing highlighting the active Green, Yellow, or Red lamp + semantic status icon) paired with a short severity label pill (`SAFE`, `CAUTION`, `UNVERIFIED`, or `HIGH HAZARD`).
2. **Unverified / Uncertain Ingredients**: Assigned to the **Yellow caution tier** (`SemanticCaution` / `SemanticCautionBg`) with an explicit `UNVERIFIED` badge label so ingredients lacking catalog verification or sufficient clinical data are never mistakenly badged Green.
3. **Placement & Interactivity**: Displayed next to every ingredient across **all report ingredient lists** (Verdict Summary categorized ingredient cards, Product Details **Ingredients** tab chips & filtered breakdown list, and the Ingredient Detail inspector sheet) with an interactive **tap-to-view hazard tooltip** that reveals a concise severity explanation, matched personal rule, and regulatory source.

---

## Architecture & Planned Changes

### 1. Traffic-Light Severity Model & Unverified Mapping (`CategorizedIngredientCards.kt`)
- **Define `TrafficLightBadgeSeverity` enum & `IngredientSafetyBadgeSpec` model**:
  - `GREEN_SAFE`: Active green lamp (`SemanticSuccess` `#2E6B47`, container `SemanticSuccessBg`), pill label `"SAFE"`, summary title `"Low Hazard • Approved Within Thresholds"`.
  - `YELLOW_CAUTION`: Active yellow/amber lamp (`SemanticCaution` `#B8791A`, container `SemanticCautionBg`), pill label `"CAUTION"`, summary title `"Moderate Caution • Concentration or Sensitivity Alert"`.
  - `YELLOW_UNVERIFIED`: Active yellow/amber lamp (`SemanticCaution` `#B8791A`, container `SemanticCautionBg`), pill label `"UNVERIFIED"`, summary title `"Caution • Unverified Label Token / Insufficient Data"`.
  - `RED_HIGH_HAZARD`: Active red lamp (`SemanticCritical` `#B94032`, container `SemanticCriticalBg`), pill label `"HIGH HAZARD"`, summary title `"High Hazard • Restricted or Medical Allergy Match"`.
- **Update `categorizeReportIngredients(...)`**:
  - Ensure `isUnverifiedToken` (`MatchCertainty.EVIDENCE_INSUFFICIENT`) and `MatchCertainty.POSSIBLE_DERIVATIVE_MATCH` deterministically map to `IngredientHazardCategory.WARNING` (Yellow tier) instead of falling through to `SAFE`, and expose a helper `CategorizedIngredientEntry.toSafetyBadgeSpec()` and `ParsedIngredient.toSafetyBadgeSpec(report, gemini)`.

### 2. Reusable `IngredientSafetyBadge` & Tap-to-View Hazard Tooltip (`CategorizedIngredientCards.kt` & `CommonComponents.kt`)
- **`TrafficLightSignalIcon` Composable**:
  - Renders a crisp miniature traffic-light housing with three stacked/aligned signal lamps (Red, Yellow, Green) where the active hazard severity lamp glows at full saturation with a subtle halo ring and the inactive lamps remain muted.
- **`IngredientSafetyBadge` Composable**:
  - Combines `TrafficLightSignalIcon` + short severity pill (`SAFE`, `CAUTION`, `UNVERIFIED`, `HIGH HAZARD`) with accessible contrast, `1.dp` semantic border, and `testTag("ingredient_safety_badge_${ingredientKey}")`.
  - Clicking/tapping the badge toggles an inline or anchored **Hazard Tooltip Card / Popover** (`testTag("ingredient_safety_tooltip_${ingredientKey}")`) displaying:
    - Traffic-light state & severity headline (Green / Yellow / Red)
    - Why it received this badge (e.g., EU SCCS / US CIR restriction, personal allergen trigger, or unverified OCR token guidance)
    - Quick action link to inspect full toxicology or edit OCR text when `UNVERIFIED`.

### 3. Integration Across All Report Ingredient Lists (`CategorizedIngredientCards.kt`, `CommonComponents.kt`, `ReportResultScreen.kt`)
- **Categorized Ingredient Cards (`CategorizedIngredientMaterialCard`)**:
  - Replace the static dot and plain text pill with the interactive `IngredientSafetyBadge` next to each ingredient title, including the expandable tap-to-view hazard tooltip banner directly below the header row when tapped.
- **Product Details — Extracted Ingredient Chips (`IngredientChip` in `CommonComponents.kt` & `ReportResultScreen.kt`)**:
  - Upgrade `IngredientChip` to display the compact traffic-light icon + severity badge pill next to each listed ingredient on the **Ingredients** tab, honouring the selected risk filter (`All`, `Safe`, `Caution`, `High Risk`) so filtering dynamically filters both the chip grid and the categorized breakdown list.
  - Add tap-to-view hazard tooltip support on the **Ingredients** tab and display the full `IngredientSafetyBadge` inside the `ModalBottomSheet` ingredient inspector in `ReportResultScreen.kt`.

### 4. Unit & Evidence Verification Tests (`ScanSafeEvidenceTest.kt`)
- Add unit tests verifying:
  - Deterministic mapping of analyzed ingredients to `RED_HIGH_HAZARD`, `YELLOW_CAUTION`, `YELLOW_UNVERIFIED`, and `GREEN_SAFE` traffic-light badge specs.
  - Unmatched/uncertain OCR tokens always receive `YELLOW_UNVERIFIED` (`IngredientHazardCategory.WARNING`) and never `GREEN_SAFE`.
- Run `compile_applet` and `gradle :app:testDebugUnitTest` to confirm zero build or test regressions.
