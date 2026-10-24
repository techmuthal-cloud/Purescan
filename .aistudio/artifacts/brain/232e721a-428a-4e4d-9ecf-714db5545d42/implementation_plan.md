# CameraX Label Capture, Gemini API Analysis & Material 3 Categorized Result Cards Plan

## 1. Overview
Enhance the **Purescan** scanning and product report experience with:
1. **CameraX Ingredient Label Capture & Dual-Flow Gemini Analysis (`ScannerScreen.kt` & `GeminiService.kt`)**:
   - Full-bleed CameraX preview (`ImageCapture` + `ImageAnalysis` luminosity + torch/zoom controls + hardware shutter & gallery picker).
   - **Dual Capture Workflow (Options 1 & 2)**:
     - **Instant Gemini Multimodal Analysis**: One-tap capture directly sends the scaled label image and user allergen profile to the Google Gemini API (`gemini-3.5-flash` via `BuildConfig.GEMINI_API_KEY`) to extract and analyze ingredients for allergens and safety hazards.
     - **Crop & OCR Text Review Mode**: Post-capture interactive crop/align frame and editable extracted ingredient text sheet allowing the user to inspect or refine OCR text before triggering Gemini API analysis.
2. **Material 3 Categorized Ingredient Results Component (`CategorizedIngredientCards.kt` & `ReportResultScreen.kt`)**:
   - Create a dedicated Material 3 visual component (`CategorizedIngredientResultsSection`) using `Card` / `ElevatedCard` and `OutlinedCard` containers that categorize every scanned ingredient into three color-coded tiers:
     - **High Hazard (`HIGH_RISK` / `CRITICAL`)**: Coral-red indicator (`#D93829` / `#FCE8E6`), priority warning icon, and allergen escalation badge.
     - **Warning (`WARNING` / `CAUTION`)**: Amber-gold indicator (`#D97706` / `#FEF3C7`), caution icon, and regulatory condition or sensitivity note.
     - **Safe (`SAFE` / `INFO`)**: Botanical emerald-green indicator (`#2E6F40` / `#E6F4EA`), verified checkmark icon, and approved functional role.
   - **Interactive Category Filter Pills with Counts**: Top filter bar (`All (N)`, `High Hazard (N)`, `Warning (N)`, `Safe (N)`) to filter or view all grouped Material 3 category cards at once.
   - **Rich Scientific & Toxicology Details (Options 1 & 3)**: Each ingredient card displays its color-coded status indicator, functional role, personal **Allergen Match** badge (if matched against the user's Allergy Filter), plain-language safety explanation, **CAS Registry Number**, **Toxicology & Dermal Absorption Mechanism**, **Concentration Limit**, and **Regulatory Citations** (`US CIR`, `EU SCCS`, `FDA`, `PubChem`) with an inline toggle to expand/collapse full bioscience details.

---

## 2. Planned File-by-File Changes

### A. Material 3 Categorized Ingredient Component
1. **`/app/src/main/java/com/example/ui/components/CategorizedIngredientCards.kt` (New File)**
   - Implement `IngredientHazardCategory` enum (`HIGH_HAZARD`, `WARNING`, `SAFE`) with semantic M3 colors, icons, and labels.
   - Implement `CategorizedIngredientResultsSection`:
     - Summary header with category distribution bar and interactive filter pills (`All`, `High Hazard`, `Warning`, `Safe`) showing live ingredient counts.
     - Grouped Material 3 `Card` containers for **High Hazard**, **Warning**, and **Safe** categories with color-coded header banners, count badges, and descriptions.
     - Individual Material 3 `OutlinedCard` items for each ingredient showing:
       - Color-coded status indicator dot and badge (`High Hazard`, `Warning`, `Safe`)
       - Canonical & raw ingredient name + functional role pill
       - Personal **ALLERGEN MATCH** badge when flagged by the user's Allergy Filter
       - Safety & regulatory explanation
       - Inline / expandable **Bioscience & Regulatory Dossier** section displaying CAS number, regulatory status & concentration limit, toxicology mechanism, and authoritative source citations.

### B. Product Report & Verdict Integration
2. **`/app/src/main/java/com/example/ui/screens/ReportResultScreen.kt`**
   - Integrate `CategorizedIngredientResultsSection` prominently into both the **Verdict Summary View** (so users immediately see the categorized `Safe`, `Warning`, and `High Hazard` Material 3 cards right below the verdict card) and the **Product Details View** (`Ingredients` and `Overview` tabs).

### C. CameraX Capture & Gemini API Analysis Workflow
3. **`/app/src/main/java/com/example/ui/screens/ScannerScreen.kt`**
   - Enhance the CameraX capture bar and post-capture review overlay so users can seamlessly choose between:
     - **Analyze Immediately with Gemini AI** (direct multimodal image-to-Gemini pipeline), or
     - **Crop & Review OCR Text First** (extracts text from the captured image into an editable preview card before running Gemini hazard & allergen analysis).
4. **`/app/src/main/java/com/example/data/api/GeminiService.kt`**
   - Ensure `extractAndAnalyzeFromImage` and `analyzeIngredientsWithGemini` map all extracted ingredients into ` SAFE`, `CAUTION` (`Warning`), and `HIGH_RISK` (`High Hazard`) breakdowns enriched with CAS numbers, toxicology mechanisms, and allergen flags.

### D. Unit & Robolectric Verification
5. **`/app/src/test/java/com/example/ScanSafeEvidenceTest.kt`**
   - Add unit test verifying ingredient categorization into `HIGH_HAZARD`, `WARNING`, and `SAFE` buckets with allergen escalation and toxicology metadata.
6. **Build & Test Execution**
   - Run `compile_applet` and `gradle :app:testDebugUnitTest` to verify zero compilation errors and 100% passing tests.
