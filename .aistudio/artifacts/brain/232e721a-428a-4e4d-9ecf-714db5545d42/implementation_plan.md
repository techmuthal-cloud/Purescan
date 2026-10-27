# PureScan Authentication Refactor & Evidence-Backed Product Scan Result Component

This update refactors the **Login/Authentication Screen** to eliminate promotional clutter and competing actions in favor of a clean, focused **Dedicated Sign In view** with a prominent **Google Sign-In button** and a clear **bottom Create Account path**. In parallel, it introduces a comprehensive **Product Scan Result UI Component** that presents the overall analysis status, interactive ingredient insights with 3-tier clinical rule badges, match certainty tags, expandable regulatory citations, and a **Dedicated Uncertainty Banner** with unverified token chips and an inline OCR text editor for inconclusive findings.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and UX choices incorporate your confirmed selections from the clarification interview:

- **Confirmed Decision 1 — Clean Dedicated Sign-In Flow & Account Creation Path**: The Authentication screen defaults to a clean, dedicated **Sign In** view containing only the PureScan emblem, a single concise value proposition, email/password inputs, the primary Sign In CTA, a prominent **Continue with Google** button, and a dedicated **bottom Create Account link** (`Don't have an account? Create Account`) that transitions smoothly into the registration view. Promotional benefit chips (`BrandPillarBadge`) and competing one-tap profile banners are removed from the primary authentication card.
- **Confirmed Decision 2 — Interactive Ingredient Insights Card with Rule Badges, Certainty Tags & Expandable Citations**: The Product Scan Result component renders each analyzed ingredient inside an interactive Material 3 card equipped with its clinical classification badge (**Medical Allergy**, **Personal Sensitivity**, **Preference Avoidance**, or **Regulatory Evaluation**), an explicit **Match Certainty Tag** (`Confirmed Match to Your Selected Rule`, `Possible Match / Botanical Derivative`, `Ingredient Detected • No Personal Rule Triggered`, or `Evidence Insufficient • Unverified Token`), the exact user-authored reason note or rule trigger, and an expandable regulatory citation drawer (EU SCCS, US CIR, FDA).
- **Confirmed Decision 3 — Dedicated Uncertainty Banner with Unverified Token Chips & Inline OCR Editor**: Whenever a scan contains unrecognized/unmatched ingredient tokens, low OCR confidence (`< 85%`), or cross-reactive botanical derivatives, the Result screen surfaces a dedicated **Uncertainty & Inconclusive Findings Banner**. Users can tap individual unverified token chips or expand the **Inline OCR Label Editor** directly on the result screen to fix misread characters and immediately re-run the analysis.

---

## 1. Overview & Core Concept

- **What It Does**:
  1. **Clutter-Free Authentication Screen**: Provides an immediately trustworthy entry point that separates authentication from product marketing. Users see a focused Sign In form, a one-tap **Continue with Google** button, a clear password recovery action, and a distinct bottom **Create Account** path (with an optional subtle "Continue as Guest" link in the top header).
  2. **Evidence-Backed Product Scan Result Component**: Gives users a transparent, clinical-grade breakdown of their scanned product:
     - **Analysis Status Header**: Displays the overall compatibility verdict (`Compatible with Your Selected Rules`, `Use with Caution`, `High Hazard Detected`, or `Inconclusive — Review Needed`) alongside a non-universal safety notice.
     - **Dedicated Uncertainty Banner & Inline OCR Editor**: Surfaces any unverified tokens, blurred OCR words, or possible derivative matches as interactive chips, allowing users to edit the transcribed ingredient text inline and re-analyze on the spot.
     - **Classified Ingredient Insights & Evidence-Backed Matching Rules**: Displays categorized Material 3 cards (`High Hazard`, `Warning`, `Safe`, and `Unverified / Uncertain`) with rule classification badges, match certainty tags, personal reason notes, and expandable scientific citations.
- **Target Audience / Persona**: Ingredient-conscious consumers and individuals managing diagnosed medical allergies, skin sensitivities, or personal dietary/cosmetic avoidances who need clear, trustworthy evidence rather than opaque alarmist labels.
- **Key Value**: Eliminates sign-in friction and builds clinical trust on the scan result screen by distinguishing confirmed personal rule matches from uncertain or unverified findings.

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Focused Sign-In & Account Creation Flow**:
     - User lands on the **Sign In** view featuring the PureScan emblem, a single value proposition subtitle, Email and Password fields, a "Forgot password?" link, the primary **Sign In** button (`ScanOrange`), an `OR` divider, and a clean **Continue with Google** button.
     - At the bottom of the card, a dedicated prompt (`New to PureScan? Create Account`) switches the view to the **Create Account** mode (adding the Full Name field and updating the primary CTA to **Create Account**, with a bottom link to return to **Sign In**).
  2. **Product Scan Result & Evidence Inspection Flow**:
     - User completes a CameraX label capture or barcode/text scan and lands on the **Product Scan Result Screen**.
     - At the top, the **Analysis Status Banner** communicates whether any personal rules were triggered, the OCR confidence score, and the non-universal safety disclaimer.
     - If any ingredients could not be matched in the canonical catalog or if OCR confidence is below threshold, the **Uncertainty & Inconclusive Findings Banner** appears with interactive chips for each unverified token and an **Edit & Re-Analyze OCR Text** inline drawer.
     - Below, users filter and inspect the **Interactive Ingredient Insight Cards**, viewing the exact matching rule (`Medical Allergy`, `Personal Sensitivity`, or `Preference Avoidance`), the **Match Certainty Tag**, their saved personal reason note, and a tap-to-expand **Regulatory & Toxicology Evidence** section.

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Warm Editorial Clinical Minimalism — high-contrast botanical ink typography on warm ivory/cream surfaces with semantic clinical status accents.
  - *Color Palette & Mood*:
    - `--background`: `FloralWhite` (`#FAF7F0`) / Dark mode deep charcoal (`#121410`)
    - `--surface`: `CardSurface` (`#FFFFFF`) & `SurfaceCream` (`#F3EFE6`)
    - `--primary`: `SmokyBlack` (`#1C1D18`) and `OliveDrab` (`#525B44`)
    - `--accent`: `ScanOrange` (`#E87A3E`) for primary actions
    - *Semantic Status Tokens*: `SemanticCritical` (`#B94032` / `#FBEAE8`) for High Hazard & Medical Allergy matches, `SemanticCaution` (`#B8791A` / `#FDF4E3`) for Warnings, Sensitivities & Uncertainty states, and `SemanticSuccess` (`#2E6B47` / `#E7F3EB`) for Safe/Verified findings.
  - *Typography & Hierarchy*: Editorial Serif (`FontFamily.Serif`, `ExtraBold`/`Bold`) for headers and verdicts paired with clean Sans-Serif body copy, structured metadata pills, and monospace CAS/citation badges.
  - *Component Styling & Layout*: Material 3 `Card`, `Surface`, `FilterChip`, and `OutlinedTextField` components adhering to an 8.dp spatial grid, 20.dp–24.dp card corner radii, and 48.dp minimum touch targets.

- **Interactive Feedback & Motion**:
  - Smooth `AnimatedVisibility` transitions when switching between **Sign In** and **Create Account** modes, expanding inline OCR text editing on the Result screen, and toggling per-ingredient regulatory citation drawers.
  - Action-triggered feedback toasts only in direct response to user authentication or re-analysis actions.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Dedicated Sign In View with Bottom Create Account Link (No Promotional Chips)**
  - *Chosen Approach*: Remove all `BrandPillarBadge` promotional chips and the prominent one-tap demo profile card from the primary authentication container. Present a clean Sign In form by default, a standalone **Continue with Google** button, and a dedicated footer link to switch to **Create Account** (while keeping "Skip" in the top bar for guest access).
  - *Why*: Reduces visual cognitive load and separates product marketing (which belongs in Onboarding) from authentication, directly resolving the P0 UX Review finding.
  - *Alternatives Considered*: Keeping a segmented top toggle alongside promotional chips was rejected because it crowded the viewport on compact mobile screens.

- **Decision 2: Four-State Match Certainty & Rule Classification on Ingredient Cards**
  - *Chosen Approach*: Every ingredient in the Product Scan Result component is tagged with both a **Hazard Category** (`High Hazard`, `Warning`, `Safe`, `Inconclusive / Unverified`) and an explicit **Match Certainty** (`Confirmed Match to Your Selected Rule`, `Possible Match / Botanical Derivative`, `Ingredient Detected • No Personal Rule Triggered`, or `Evidence Insufficient • Unverified Token`), plus an expandable citation panel.
  - *Why*: Users need to know *why* an ingredient was flagged (e.g., their own Medical Allergy rule vs. a general regulatory limit) and how certain the match is.
  - *Alternatives Considered*: Showing only a generic red/yellow/green dot without rule attribution was rejected because it conflates personal allergies with general cosmetic restrictions.

- **Decision 3: Dedicated Uncertainty Banner with Inline OCR Editor**
  - *Chosen Approach*: Surface a dedicated amber/slate **Uncertainty & Inconclusive Findings Banner** whenever `report.unmatchedIngredients` is non-empty, `gemini.ocrConfidence < 85`, or derivative cross-reactivity is possible. Include tap-to-edit token chips and an expandable inline text editor that calls `viewModel.reanalyzeCurrentReportText(updatedText)` directly in place.
  - *Why*: Enables immediate recovery from blurred camera captures or unusual INCI spellings without forcing the user to abandon the result screen and rescan from scratch.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

- **Architecture & Component Diagram**:
```text
┌────────────────────────────────────────────────────────────────────────────┐
│                           ScanViewModel (StateFlow)                        │
│  • Auth Actions: signInWithEmail, signInWithGoogleAccount, resetPassword   │
│  • Scan Result Actions: reanalyzeEditedReportText, toggleSave, setLanguage │
└───────────────────┬────────────────────────────────────┬───────────────────┘
                    │                                    │
                    ▼                                    ▼
┌───────────────────────────────────────┐  ┌─────────────────────────────────┐
│       Refactored AuthScreen           │  │     ReportResultScreen          │
│ ┌───────────────────────────────────┐ │  │ ┌─────────────────────────────┐ │
│ │ Top Bar: Language Pill + Skip     │ │  │ │ AnalysisStatusHeaderCard    │ │
│ └───────────────────────────────────┘ │  │ │ • Verdict + Evidence Status │ │
│ ┌───────────────────────────────────┐ │  │ │ • Non-Universal Safety Note │ │
│ │ Clean Editorial Auth Card         │ │  │ └──────────────┬──────────────┘ │
│ │ • PureScan Emblem + 1-Line Value  │ │  │                ▼                │
│ │ • Email & Password Fields         │ │  │ ┌─────────────────────────────┐ │
│ │   (+ Full Name in Create Mode)    │ │  │ │ UncertaintyFindingsBanner   │ │
│ │ • Primary CTA: Sign In / Create   │ │  │ │ • Unverified Token Chips    │ │
│ │ • Divider + Continue with Google  │ │  │ │ • Inline OCR Editor &       │ │
│ │ • Bottom Link: Switch Sign In <-> │ │  │ │   Instant Re-Analyze CTA    │ │
│ │   Create Account                  │ │  │ └──────────────┬──────────────┘ │
│ └───────────────────────────────────┘ │  │                ▼                │
└───────────────────────────────────────┘  │ ┌─────────────────────────────┐ │
                                           │ │ ProductScanResultComponent  │ │
                                           │ │ • Category & Certainty Pills│ │
                                           │ │ • Interactive Ingredient    │ │
                                           │ │   Cards with Rule Badges,   │ │
                                           │ │   Reason Notes & Expandable │ │
                                           │ │   Regulatory Citations      │ │
                                           │ └─────────────────────────────┘ │
                                           └─────────────────────────────────┘
```

- **Data Model & State**:
  - `CategorizedIngredientEntry`: Extended with `matchCertainty: MatchCertainty`, `ruleClassificationLabel: String?`, `personalReasonNote: String?`, and `isUncertainOrUnverified: Boolean` so each card renders complete rule provenance and uncertainty state.
  - `ScanViewModel`: Adds `reanalyzeCurrentReportWithText(updatedText: String)` so inline OCR edits inside the Uncertainty Banner immediately re-evaluate the ingredient list against the Room allergen profile (`PreferenceSettingEntity`, `CustomAllergenEntity`) and Gemini service in the active `AppLanguage`.

- **Interactive Component & State Mapping**:
  - **AuthScreen Mode Switching**: Tapping the bottom account creation link (`auth_switch_mode_button`) toggles `isSignUp` state, clears any stale `authErrorMessage`, reveals/hides the `fullName` field with `AnimatedVisibility`, and updates the primary submit button label between `Sign In` and `Create Account`.
  - **Google Sign-In Flow**: Tapping `Continue with Google` (`auth_google_button`) opens the account confirmation dialog (`confirm_google_signin_button`), persists the account in Room via `authenticateProviderAccount`, and navigates to `Screen.Home`.
  - **Inline Uncertainty Resolution**: Tapping any unverified token chip or the `Resolve / Edit OCR Text` button on the Uncertainty Banner opens the inline OCR editor seeded with `report.correctedText`. Submitting calls `viewModel.reanalyzeCurrentReportWithText(editedText)` to refresh the current `ScanReport` in-place.
  - **Expandable Ingredient Evidence**: Tapping an ingredient card toggles its expanded state to reveal CAS registry number, dermal bioavailability, toxicology mechanism, and regulatory citations from EU SCCS, US CIR, and FDA.
