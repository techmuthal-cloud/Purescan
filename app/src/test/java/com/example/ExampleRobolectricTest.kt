package com.example

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.GeminiService
import com.example.data.catalog.EvidenceEngine
import com.example.data.local.AppDatabase
import com.example.data.local.ScanEntity
import com.example.data.repository.ScanRepository
import com.example.ui.ScanViewModel
import com.example.ui.Screen
import com.example.ui.util.PdfExportHelper
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Purescan", appName)
    }

    @Test
    fun `real bitmap extraction produces product details and ingredients`() = runBlocking {
        val bmp = Bitmap.createBitmap(240, 240, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(android.graphics.Color.rgb(220, 235, 225))
        val result = GeminiService.extractAndAnalyzeFromImage(
            bitmap = bmp,
            region = "US",
            userAllergens = listOf("Niacinamide")
        )
        assertTrue(result.productTitle.isNotBlank())
        assertTrue(result.brandName.isNotBlank())
        assertTrue(result.extractedIngredientsText.isNotBlank())
        assertTrue(result.insight.ingredientBreakdowns.isNotEmpty())
        assertTrue(result.insight.groundingCitations.isNotEmpty())
    }

    @Test
    fun `pdf and visual scorecard export generate valid files and formatted text`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sampleScan = ScanEntity(
            id = "scan-1",
            title = "Hydrating Daily Moisturizer",
            category = "Skin Care",
            regionCode = "US",
            status = "completed",
            summaryStatus = "NO_RELEVANT_CONCERN",
            rawOcrText = "Aqua, Glycerin, Ceramide NP",
            correctedText = "Aqua, Glycerin, Ceramide NP",
            matchedCount = 3,
            unmatchedCount = 0,
            isSaved = true,
            limitationsNote = "Standard cosmetic evaluation",
            createdAt = System.currentTimeMillis(),
            safetyScore = 95,
            safetyTier = "SAFE",
            aiHeadline = "This Product Appears Safe",
            aiSubheadline = "No major safety concerns found."
        )

        val historyPdf = PdfExportHelper.generateHistoryPdfFile(context, listOf(sampleScan))
        assertTrue(historyPdf.exists())
        assertTrue(historyPdf.length() > 100)

        val report = EvidenceEngine.analyzeIngredients(
            productTitle = "Hydrating Daily Moisturizer",
            category = "Skin Care",
            region = "US",
            rawOcrText = "Aqua, Glycerin, Ceramide NP",
            correctedText = "Aqua, Glycerin, Ceramide NP",
            activePreferences = emptyList()
        )
        val insight = GeminiService.generateFallbackInsight(report.correctedText, report.category)
        val singlePdf = PdfExportHelper.generateSingleReportPdfFile(context, report, insight)
        assertTrue(singlePdf.exists())
        assertTrue(singlePdf.length() > 100)

        val scorecardPng = PdfExportHelper.generateVisualScorecardFile(context, report, insight)
        assertTrue(scorecardPng.exists())
        assertTrue(scorecardPng.length() > 100)

        val shareText = PdfExportHelper.formatShareTextReport(report, insight)
        assertTrue(shareText.contains("Hydrating Daily Moisturizer"))
        assertTrue(shareText.contains("Bioscience & Toxicology Profile"))
    }

    @Test
    fun `room database persists and restores full scan hazard and bioscience profiles`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.createInMemory(context)
        val repo = ScanRepository(db)

        val savedReport = repo.processAndSaveScan(
            productTitle = "Renewal Night Cream",
            category = "Skin Care",
            region = "EU",
            rawOcrText = "Aqua, Glycerin, Retinol, Parfum, Methylparaben",
            correctedText = "Aqua, Glycerin, Retinol, Parfum, Methylparaben"
        )

        val restored = repo.getFullReport(savedReport.scanId)
        assertNotNull(restored)
        assertEquals("Renewal Night Cream", restored!!.productTitle)
        assertNotNull(restored.geminiInsight)
        assertTrue(restored.geminiInsight!!.ingredientBreakdowns.isNotEmpty())
        assertTrue(restored.geminiInsight!!.ingredientBreakdowns.any { it.casNumber.isNotBlank() })
        assertTrue(restored.geminiInsight!!.groundingCitations.isNotEmpty())
        assertTrue(restored.geminiInsight!!.toxicologyProfile.dermalBioavailability.isNotBlank())
        db.close()
    }

    @Test
    fun `once user is logged in app opens directly to main home screen on launch`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val prefs = app.getSharedPreferences(ScanViewModel.AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        // 1. Fresh install before login starts on Splash
        val initialVm = ScanViewModel(app)
        assertEquals(Screen.Splash, initialVm.uiState.value.currentScreen)

        // 2. Simulate login persisted in session
        prefs.edit()
            .putBoolean(ScanViewModel.KEY_IS_LOGGED_IN, true)
            .putString(ScanViewModel.KEY_USER_NAME, "Priya Sharma")
            .putString(ScanViewModel.KEY_USER_EMAIL, "priya@scansafe.ai")
            .putString(ScanViewModel.KEY_AUTH_PROVIDER, "GOOGLE")
            .commit()

        // 3. Reopening the app with active session opens directly to Screen.Home!
        val relaunchedVm = ScanViewModel(app)
        assertTrue(relaunchedVm.uiState.value.isAuthenticated)
        assertEquals(Screen.Home, relaunchedVm.uiState.value.currentScreen)
        assertEquals("GOOGLE", relaunchedVm.uiState.value.authProvider)
    }

    @Test
    fun `ingredient glossary toxicology catalog and theme mode persistence work accurately`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val prefs = app.getSharedPreferences(ScanViewModel.AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        val vm = ScanViewModel(app)
        assertEquals(com.example.ui.theme.ThemeMode.LIGHT, vm.uiState.value.themeMode)

        vm.setThemeMode(com.example.ui.theme.ThemeMode.DARK)
        vm.setOutdoorMode(true)
        assertEquals(com.example.ui.theme.ThemeMode.DARK, vm.uiState.value.themeMode)
        assertTrue(vm.uiState.value.outdoorMode)

        // Verify persistence across ViewModel recreation
        val relaunchedVm = ScanViewModel(app)
        assertEquals(com.example.ui.theme.ThemeMode.DARK, relaunchedVm.uiState.value.themeMode)
        assertTrue(relaunchedVm.uiState.value.outdoorMode)

        relaunchedVm.setThemeMode(com.example.ui.theme.ThemeMode.SYSTEM)
        assertEquals(com.example.ui.theme.ThemeMode.SYSTEM, relaunchedVm.uiState.value.themeMode)

        // Verify IngredientCatalog contains Safe, Caution, and High Risk entries with CAS numbers & toxicology mechanisms
        val catalog = com.example.data.catalog.IngredientCatalog.allIngredients
        assertTrue(catalog.size >= 25)
        assertTrue(catalog.all { it.casNumber.isNotBlank() && it.toxicologyMechanism.isNotBlank() })
        assertTrue(catalog.any { ing -> ing.evidenceRecords.all { it.severity == com.example.data.model.Severity.INFO } }) // Safe tier
        assertTrue(catalog.any { ing -> ing.evidenceRecords.any { it.severity == com.example.data.model.Severity.CRITICAL } }) // High Risk tier
        assertTrue(catalog.any { ing -> ing.evidenceRecords.any { it.severity == com.example.data.model.Severity.CAUTION } }) // Caution tier
    }

    @Test
    fun `haptics manager and skeleton loading state persist and execute safely`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val prefs = app.getSharedPreferences(ScanViewModel.AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        val vm = ScanViewModel(app)
        assertTrue(vm.uiState.value.hapticsEnabled)
        assertTrue(com.example.ui.util.HapticsManager.isEnabled)

        // Trigger camera capture, analysis success, and error haptics safely
        com.example.ui.util.HapticsManager.triggerCameraCapture(app)
        com.example.ui.util.HapticsManager.triggerAnalysisSuccess(app)
        com.example.ui.util.HapticsManager.triggerErrorState(app)
        com.example.ui.util.HapticsManager.triggerSelectionTap(app)

        // Toggle haptics off and verify persistence
        vm.setHapticsEnabled(false)
        assertEquals(false, vm.uiState.value.hapticsEnabled)
        assertEquals(false, com.example.ui.util.HapticsManager.isEnabled)

        // Toggle skeleton loading state on and off
        vm.setDataLoading(true)
        assertEquals(true, vm.uiState.value.isDataLoading)
        vm.setDataLoading(false)
        assertEquals(false, vm.uiState.value.isDataLoading)

        val relaunchedVm = ScanViewModel(app)
        assertEquals(false, relaunchedVm.uiState.value.hapticsEnabled)
    }

    @Test
    fun `room schema stores scan history verdicts and user defined allergen profiles`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.createInMemory(context)
        val repo = ScanRepository(db)

        db.preferenceDao().insertPreferences(
            listOf(
                com.example.data.local.PreferenceSettingEntity(
                    key = "pref_gluten",
                    label = "Gluten / Wheat Derivatives",
                    description = "Flag wheat",
                    category = "allergen",
                    isEnabled = false
                ),
                com.example.data.local.PreferenceSettingEntity(
                    key = "pref_tree_nuts",
                    label = "Tree Nut & Peanut Allergens",
                    description = "Flag almond and peanut",
                    category = "allergen",
                    isEnabled = false
                )
            )
        )

        val createdProfile = repo.saveUserAllergenProfile(
            profileName = "Strict Nut & Gluten Profile",
            description = "Flags tree nuts, gluten, and methylparaben",
            enabledPreferenceKeys = listOf("pref_gluten", "pref_tree_nuts"),
            customTriggers = listOf("Methylparaben"),
            defaultSeverity = "HIGH_RISK",
            activateImmediately = true
        )

        val activeProfile = db.allergenProfileDao().getActiveProfileSync()
        assertNotNull(activeProfile)
        assertEquals("Strict Nut & Gluten Profile", activeProfile!!.profileName)
        assertEquals(createdProfile.id, activeProfile.id)

        val syncedPrefs = repo.getPreferencesList()
        assertTrue(syncedPrefs.first { it.key == "pref_gluten" }.isEnabled)
        assertTrue(syncedPrefs.first { it.key == "pref_tree_nuts" }.isEnabled)
        val activeCustom = db.customAllergenDao().getActiveCustomAllergens()
        assertTrue(activeCustom.any { it.name.equals("Methylparaben", ignoreCase = true) })

        val unsafeReport = repo.processAndSaveScan(
            productTitle = "Almond & Wheat Hair Mask",
            category = "Hair Care",
            region = "US",
            rawOcrText = "Water, Hydrolyzed Wheat Protein, Sweet Almond Oil, Methylparaben",
            correctedText = "Water, Hydrolyzed Wheat Protein, Sweet Almond Oil, Methylparaben"
        )

        val storedScan = db.scanDao().getScanById(unsafeReport.scanId)
        assertNotNull(storedScan)
        assertEquals(com.example.data.local.ScanStatus.COMPLETED.name, storedScan!!.scanStatus)
        assertEquals(com.example.data.local.SafetyVerdict.UNSAFE_MATCH.name, storedScan.safetyVerdict)
        assertEquals("HIGH_RISK", storedScan.safetyTier)

        db.close()
    }

    @Test
    fun `camerax capture state transitions handle start error and recovery deterministically`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = ScanViewModel(app)

        vm.onCaptureStarted()
        assertEquals(com.example.data.local.ScanStatus.CAPTURING, vm.uiState.value.captureStatus)
        assertEquals(null, vm.uiState.value.captureErrorMessage)

        vm.onCaptureFailed("Camera sensor timeout")
        assertEquals(com.example.data.local.ScanStatus.FAILED, vm.uiState.value.captureStatus)
        assertEquals("Camera sensor timeout", vm.uiState.value.captureErrorMessage)

        vm.clearCaptureError()
        assertEquals(com.example.data.local.ScanStatus.PENDING, vm.uiState.value.captureStatus)
        assertEquals(null, vm.uiState.value.captureErrorMessage)
    }
}


