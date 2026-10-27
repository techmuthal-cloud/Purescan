package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.SampleLabel
import com.example.data.local.AllergenProfileEntity
import com.example.data.local.AppDatabase
import com.example.data.local.PreferenceSettingEntity
import com.example.data.local.ScanEntity
import com.example.data.local.ScanStatus
import com.example.data.model.BatchQueueItem
import com.example.data.model.BatchScanSummary
import com.example.data.model.ParsedIngredient
import com.example.data.model.ReportFinding
import com.example.data.model.ScanReport
import com.example.data.repository.ScanRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.ui.theme.ThemeMode
import com.example.ui.util.AppLanguage
import com.example.ui.util.HapticsManager
import java.util.UUID

sealed class Screen {
    object Splash : Screen()
    object Onboarding : Screen()
    object Auth : Screen()
    object Home : Screen()
    object Scanner : Screen()
    object TextReview : Screen()
    object AnalysisProgress : Screen()
    object ReportResult : Screen()
    object BatchReport : Screen()
    object History : Screen()
    object Saved : Screen()
    object Preferences : Screen()
    object AllergenSettings : Screen()
    object Profile : Screen()
    object CompareProducts : Screen()
    object Glossary : Screen()
}

data class OfflineQueuedScan(
    val id: String = UUID.randomUUID().toString(),
    val productTitle: String,
    val category: String,
    val region: String,
    val ingredientsText: String,
    val queuedAt: Long = System.currentTimeMillis()
)

data class ScanUiState(
    val currentScreen: Screen = Screen.Splash,
    val isAuthenticated: Boolean = false,
    val authProvider: String = "NONE", // "GOOGLE", "EMAIL", "LOCAL_PROFILE"
    val authErrorMessage: String? = null,
    val productTitle: String = "",
    val category: String = "Skin Care",
    val region: String = "US",
    val rawOcrText: String = "",
    val editableText: String = "",
    val analysisStep: Int = 0,
    val currentReport: ScanReport? = null,
    val showFullProductDetails: Boolean = false,
    val selectedFinding: ReportFinding? = null,
    val selectedIngredient: ParsedIngredient? = null,
    val showPaywall: Boolean = false,
    val showFeedbackDialog: Boolean = false,
    val scanQuotaUsed: Int = 2,
    val scanQuotaTotal: Int = 5,
    val isPremium: Boolean = false,
    val activeFilter: String = "all", // "all", "concerns", "clean", "unknowns"
    val historySearchQuery: String = "",
    val deleteConfirmationScanId: String? = null,
    val notificationMessage: String? = null,
    val userName: String = "Priya Sharma",
    val userEmail: String = "priya@scansafe.ai",
    val offlineQueuedScans: List<OfflineQueuedScan> = emptyList(),
    val isRetryingOfflineQueue: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val outdoorMode: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val isDataLoading: Boolean = false,
    val isBatchScanMode: Boolean = false,
    val batchQueue: List<BatchQueueItem> = emptyList(),
    val currentBatchSummary: BatchScanSummary? = null,
    val batchProgressIndex: Int = 0,
    val batchTotalCount: Int = 0,
    val returnToBatchReport: Boolean = false,
    val compareLeftScanId: String? = null,
    val compareRightScanId: String? = null,
    val captureStatus: ScanStatus = ScanStatus.PENDING,
    val captureErrorMessage: String? = null,
    val activeScanJobId: String? = null,
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    val showLanguageDialog: Boolean = false,
    val showHowItWorksDialog: Boolean = false
)

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val AUTH_PREFS_NAME = "scansafe_auth_session"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_AUTH_PROVIDER = "auth_provider"
        const val KEY_IS_PREMIUM = "is_premium"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_OUTDOOR_MODE = "outdoor_mode"
        const val KEY_HAPTICS_ENABLED = "haptics_enabled"
        const val KEY_APP_LANGUAGE = "app_language"
    }

    private val authPrefs = application.getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
    private val repository = ScanRepository(AppDatabase.getDatabase(application))

    private val _uiState = MutableStateFlow(createInitialAuthState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val screenStack = mutableListOf<Screen>()

    val allScans: StateFlow<List<ScanEntity>> = repository.allScans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedScans: StateFlow<List<ScanEntity>> = repository.savedScans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val preferences: StateFlow<List<PreferenceSettingEntity>> = repository.allPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customAllergens: StateFlow<List<com.example.data.local.CustomAllergenEntity>> = repository.allCustomAllergens
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allergenProfiles: StateFlow<List<AllergenProfileEntity>> = repository.allAllergenProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAllergenProfile: StateFlow<AllergenProfileEntity?> = repository.activeAllergenProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun createInitialAuthState(): ScanUiState {
        val isLoggedIn = authPrefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val savedName = authPrefs.getString(KEY_USER_NAME, "Priya Sharma") ?: "Priya Sharma"
        val savedEmail = authPrefs.getString(KEY_USER_EMAIL, "priya@scansafe.ai") ?: "priya@scansafe.ai"
        val savedProvider = authPrefs.getString(KEY_AUTH_PROVIDER, "LOCAL_PROFILE") ?: "LOCAL_PROFILE"
        val savedPremium = authPrefs.getBoolean(KEY_IS_PREMIUM, false)
        val savedThemeStr = authPrefs.getString(KEY_THEME_MODE, ThemeMode.LIGHT.name) ?: ThemeMode.LIGHT.name
        val savedTheme = runCatching { ThemeMode.valueOf(savedThemeStr) }.getOrDefault(ThemeMode.LIGHT)
        val savedOutdoor = authPrefs.getBoolean(KEY_OUTDOOR_MODE, false)
        val savedHaptics = authPrefs.getBoolean(KEY_HAPTICS_ENABLED, true)
        val savedLangCode = authPrefs.getString(KEY_APP_LANGUAGE, AppLanguage.ENGLISH.code) ?: AppLanguage.ENGLISH.code
        val savedLang = AppLanguage.fromCode(savedLangCode)
        HapticsManager.isEnabled = savedHaptics

        // When user is already logged in, open directly to Main Home page on startup!
        return ScanUiState(
            currentScreen = if (isLoggedIn) Screen.Home else Screen.Splash,
            isAuthenticated = isLoggedIn,
            authProvider = if (isLoggedIn) savedProvider else "NONE",
            userName = savedName,
            userEmail = savedEmail,
            isPremium = savedPremium,
            themeMode = savedTheme,
            outdoorMode = savedOutdoor,
            hapticsEnabled = savedHaptics,
            selectedLanguage = savedLang,
            isDataLoading = false
        )
    }

    private fun persistAuthSession(
        isLoggedIn: Boolean,
        fullName: String,
        email: String,
        provider: String,
        isPremium: Boolean = _uiState.value.isPremium
    ) {
        authPrefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
            .putString(KEY_USER_NAME, fullName)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_AUTH_PROVIDER, provider)
            .putBoolean(KEY_IS_PREMIUM, isPremium)
            .apply()
    }

    fun clearAuthError() {
        if (_uiState.value.authErrorMessage != null) {
            _uiState.value = _uiState.value.copy(authErrorMessage = null)
        }
    }

    fun signInWithEmail(
        email: String,
        password: String,
        fullName: String,
        isSignUp: Boolean
    ) {
        viewModelScope.launch {
            val result = repository.authenticateEmailAccount(
                email = email,
                password = password,
                fullName = fullName,
                isSignUp = isSignUp
            )
            result.fold(
                onSuccess = { account ->
                    persistAuthSession(
                        isLoggedIn = true,
                        fullName = account.fullName,
                        email = account.email,
                        provider = "EMAIL",
                        isPremium = account.isPremium
                    )
                    screenStack.clear()
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = true,
                        authProvider = "EMAIL",
                        authErrorMessage = null,
                        userName = account.fullName,
                        userEmail = account.email,
                        currentScreen = Screen.Home
                    )
                    showToast("Signed in as ${account.fullName}")
                },
                onFailure = { err ->
                    HapticsManager.triggerErrorState(getApplication())
                    _uiState.value = _uiState.value.copy(
                        authErrorMessage = err.message ?: "Authentication failed"
                    )
                }
            )
        }
    }

    fun signInWithGoogleAccount(fullName: String, email: String) {
        viewModelScope.launch {
            val account = repository.authenticateProviderAccount(
                email = email,
                fullName = fullName,
                provider = "GOOGLE"
            )
            persistAuthSession(
                isLoggedIn = true,
                fullName = account.fullName,
                email = account.email,
                provider = "GOOGLE"
            )
            screenStack.clear()
            _uiState.value = _uiState.value.copy(
                isAuthenticated = true,
                authProvider = "GOOGLE",
                authErrorMessage = null,
                userName = account.fullName,
                userEmail = account.email,
                currentScreen = Screen.Home
            )
            showToast("Signed in with Google (${account.email})")
        }
    }

    fun signInWithQuickProfile(fullName: String, email: String) {
        viewModelScope.launch {
            val account = repository.authenticateProviderAccount(
                email = email,
                fullName = fullName,
                provider = "LOCAL_PROFILE"
            )
            persistAuthSession(
                isLoggedIn = true,
                fullName = account.fullName,
                email = account.email,
                provider = "LOCAL_PROFILE"
            )
            screenStack.clear()
            _uiState.value = _uiState.value.copy(
                isAuthenticated = true,
                authProvider = "LOCAL_PROFILE",
                authErrorMessage = null,
                userName = account.fullName,
                userEmail = account.email,
                currentScreen = Screen.Home
            )
            showToast("Welcome back, ${account.fullName}!")
        }
    }

    fun signOut() {
        persistAuthSession(
            isLoggedIn = false,
            fullName = _uiState.value.userName,
            email = _uiState.value.userEmail,
            provider = "NONE"
        )
        screenStack.clear()
        _uiState.value = _uiState.value.copy(
            isAuthenticated = false,
            authProvider = "NONE",
            authErrorMessage = null,
            currentScreen = Screen.Auth
        )
        showToast("Signed out of PureScan")
    }

    fun navigateTo(screen: Screen) {
        if (_uiState.value.currentScreen != screen) {
            screenStack.add(_uiState.value.currentScreen)
            _uiState.value = _uiState.value.copy(currentScreen = screen)
        }
    }

    fun navigateBack(): Boolean {
        if (_uiState.value.currentScreen == Screen.ReportResult && _uiState.value.showFullProductDetails) {
            _uiState.value = _uiState.value.copy(showFullProductDetails = false)
            return true
        }
        if (_uiState.value.currentScreen == Screen.ReportResult && _uiState.value.returnToBatchReport && _uiState.value.currentBatchSummary != null) {
            // Pop if BatchReport is on top of stack to avoid duplicate stack entry
            if (screenStack.lastOrNull() == Screen.BatchReport) {
                screenStack.removeAt(screenStack.size - 1)
            }
            _uiState.value = _uiState.value.copy(
                currentScreen = Screen.BatchReport,
                returnToBatchReport = false
            )
            return true
        }
        if (screenStack.isNotEmpty()) {
            val previous = screenStack.removeAt(screenStack.size - 1)
            _uiState.value = _uiState.value.copy(currentScreen = previous)
            return true
        } else if (_uiState.value.currentScreen != Screen.Home) {
            _uiState.value = _uiState.value.copy(currentScreen = Screen.Home)
            return true
        }
        return false
    }

    fun setShowFullProductDetails(show: Boolean) {
        _uiState.value = _uiState.value.copy(showFullProductDetails = show)
    }

    fun updateUserProfile(name: String, email: String) {
        if (name.isNotBlank() && email.isNotBlank()) {
            val trimmedName = name.trim()
            val trimmedEmail = email.trim()
            persistAuthSession(
                isLoggedIn = _uiState.value.isAuthenticated,
                fullName = trimmedName,
                email = trimmedEmail,
                provider = _uiState.value.authProvider
            )
            _uiState.value = _uiState.value.copy(
                userName = trimmedName,
                userEmail = trimmedEmail
            )
            showToast("Profile updated")
        }
    }

    fun setRegion(region: String) {
        _uiState.value = _uiState.value.copy(region = region)
    }

    fun setCategory(category: String) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun setProductTitle(title: String) {
        _uiState.value = _uiState.value.copy(productTitle = title)
    }

    fun setEditableText(text: String) {
        _uiState.value = _uiState.value.copy(editableText = text)
    }

    fun loadSample(sample: SampleLabel) {
        _uiState.value = _uiState.value.copy(
            productTitle = sample.name,
            category = sample.category,
            rawOcrText = sample.ingredientsText,
            editableText = sample.ingredientsText
        )
        startAnalysis()
    }

    fun startManualEntry() {
        _uiState.value = _uiState.value.copy(
            productTitle = "",
            rawOcrText = "",
            editableText = ""
        )
        navigateTo(Screen.TextReview)
    }

    fun editCurrentReportText() {
        val report = _uiState.value.currentReport ?: return
        _uiState.value = _uiState.value.copy(
            productTitle = report.productTitle,
            category = report.category,
            region = report.region,
            rawOcrText = report.rawOcrText,
            editableText = report.correctedText
        )
        navigateTo(Screen.TextReview)
    }

    fun reanalyzeCurrentReportWithText(updatedText: String) {
        val current = _uiState.value.currentReport ?: return
        val cleaned = updatedText.trim()
        if (cleaned.isBlank()) {
            showToast("Ingredient text cannot be empty")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                editableText = cleaned,
                captureStatus = ScanStatus.PROCESSING
            )
            val updatedReport = repository.processAndSaveScan(
                productTitle = current.productTitle,
                category = current.category,
                region = current.region,
                rawOcrText = current.rawOcrText,
                correctedText = cleaned,
                languageCode = _uiState.value.selectedLanguage.code
            )
            HapticsManager.triggerAnalysisSuccess(getApplication())
            _uiState.value = _uiState.value.copy(
                currentReport = updatedReport,
                editableText = updatedReport.correctedText,
                captureStatus = ScanStatus.COMPLETED
            )
            showToast("Re-analyzed ${updatedReport.ingredients.size} ingredients with updated OCR text")
        }
    }

    fun queueCurrentScanOffline() {
        val text = _uiState.value.editableText.trim()
        if (text.isBlank()) {
            showToast("Enter or paste ingredients before queuing")
            return
        }
        val title = _uiState.value.productTitle.ifBlank {
            "Queued Scan #${_uiState.value.offlineQueuedScans.size + 1}"
        }
        val item = OfflineQueuedScan(
            productTitle = title,
            category = _uiState.value.category,
            region = _uiState.value.region,
            ingredientsText = text
        )
        _uiState.value = _uiState.value.copy(
            offlineQueuedScans = _uiState.value.offlineQueuedScans + item
        )
        showToast("Saved \"$title\" to offline queue for automatic retry")
        navigateTo(Screen.Home)
    }

    fun retryOfflineQueue() {
        val queue = _uiState.value.offlineQueuedScans
        if (queue.isEmpty() || _uiState.value.isRetryingOfflineQueue) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRetryingOfflineQueue = true)
            var lastReport: ScanReport? = null
            for (queued in queue) {
                lastReport = repository.processAndSaveScan(
                    productTitle = queued.productTitle,
                    category = queued.category,
                    region = queued.region,
                    rawOcrText = queued.ingredientsText,
                    correctedText = queued.ingredientsText
                )
            }
            _uiState.value = _uiState.value.copy(
                offlineQueuedScans = emptyList(),
                isRetryingOfflineQueue = false,
                currentReport = lastReport ?: _uiState.value.currentReport
            )
            showToast("Synced ${queue.size} queued scan(s) successfully")
        }
    }

    fun onImageCapturedOrSelected(detectedText: String, titleFallback: String = "Captured Label") {
        _uiState.value = _uiState.value.copy(
            productTitle = if (_uiState.value.productTitle.isBlank()) titleFallback else _uiState.value.productTitle,
            rawOcrText = detectedText,
            editableText = detectedText
        )
        startAnalysis()
    }

    fun onCaptureStarted() {
        _uiState.value = _uiState.value.copy(
            captureStatus = ScanStatus.CAPTURING,
            captureErrorMessage = null
        )
    }

    fun onCaptureFailed(reason: String) {
        HapticsManager.triggerErrorState(getApplication())
        _uiState.value = _uiState.value.copy(
            captureStatus = ScanStatus.FAILED,
            captureErrorMessage = reason
        )
    }

    fun clearCaptureError() {
        _uiState.value = _uiState.value.copy(
            captureStatus = ScanStatus.PENDING,
            captureErrorMessage = null
        )
    }

    fun onRealPhotoCapturedOrPicked(bitmap: android.graphics.Bitmap) {
        if (_uiState.value.captureStatus == ScanStatus.PROCESSING &&
            _uiState.value.currentScreen == Screen.AnalysisProgress
        ) {
            return
        }

        val jobId = UUID.randomUUID().toString()
        _uiState.value = _uiState.value.copy(
            captureStatus = ScanStatus.PROCESSING,
            captureErrorMessage = null,
            activeScanJobId = jobId
        )
        navigateTo(Screen.AnalysisProgress)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(analysisStep = 1)
            delay(350) // Step 1: Reading text from image
            _uiState.value = _uiState.value.copy(analysisStep = 2)
            delay(350) // Step 2: Extracting ingredients
            _uiState.value = _uiState.value.copy(analysisStep = 3)

            val report = repository.processAndSaveImageScan(
                bitmap = bitmap,
                region = _uiState.value.region,
                languageCode = _uiState.value.selectedLanguage.code
            )

            if (_uiState.value.activeScanJobId != null && _uiState.value.activeScanJobId != jobId) {
                return@launch
            }

            _uiState.value = _uiState.value.copy(analysisStep = 4)
            delay(300) // Step 4: Calculating results

            val newQuota = _uiState.value.scanQuotaUsed + 1
            HapticsManager.triggerAnalysisSuccess(getApplication())
            _uiState.value = _uiState.value.copy(
                productTitle = report.productTitle,
                category = report.category,
                rawOcrText = report.rawOcrText,
                editableText = report.correctedText,
                currentReport = report,
                showFullProductDetails = false,
                scanQuotaUsed = newQuota,
                captureStatus = ScanStatus.COMPLETED,
                activeScanJobId = null,
                currentScreen = Screen.ReportResult
            )
        }
    }

    fun extractAndReviewFromBitmap(bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                captureStatus = ScanStatus.PROCESSING,
                captureErrorMessage = null
            )
            val activeAllergens = customAllergens.value.filter { it.isEnabled }.map { it.name }
            val extraction = com.example.data.api.GeminiService.extractAndAnalyzeFromImage(
                bitmap = bitmap,
                region = _uiState.value.region,
                userAllergens = activeAllergens,
                languageCode = _uiState.value.selectedLanguage.code
            )
            _uiState.value = _uiState.value.copy(
                productTitle = extraction.productTitle,
                category = extraction.category,
                rawOcrText = extraction.extractedIngredientsText,
                editableText = extraction.extractedIngredientsText,
                captureStatus = ScanStatus.PENDING
            )
            navigateTo(Screen.TextReview)
        }
    }

    fun startAnalysis() {
        if (_uiState.value.editableText.isBlank()) return
        if (_uiState.value.captureStatus == ScanStatus.PROCESSING &&
            _uiState.value.currentScreen == Screen.AnalysisProgress
        ) {
            return
        }

        val jobId = UUID.randomUUID().toString()
        _uiState.value = _uiState.value.copy(
            captureStatus = ScanStatus.PROCESSING,
            captureErrorMessage = null,
            activeScanJobId = jobId
        )
        navigateTo(Screen.AnalysisProgress)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(analysisStep = 1)
            delay(350) // Step 1: Reading text from image
            _uiState.value = _uiState.value.copy(analysisStep = 2)
            delay(400) // Step 2: Extracting ingredients
            _uiState.value = _uiState.value.copy(analysisStep = 3)
            delay(400) // Step 3: Checking safety data

            val report = repository.processAndSaveScan(
                productTitle = _uiState.value.productTitle.ifBlank { "Label Scan #${(allScans.value.size + 1)}" },
                category = _uiState.value.category,
                region = _uiState.value.region,
                rawOcrText = _uiState.value.rawOcrText.ifBlank { _uiState.value.editableText },
                correctedText = _uiState.value.editableText,
                languageCode = _uiState.value.selectedLanguage.code
            )

            if (_uiState.value.activeScanJobId != null && _uiState.value.activeScanJobId != jobId) {
                return@launch
            }

            _uiState.value = _uiState.value.copy(analysisStep = 4)
            delay(300) // Step 4: Calculating results

            val newQuota = _uiState.value.scanQuotaUsed + 1
            HapticsManager.triggerAnalysisSuccess(getApplication())

            _uiState.value = _uiState.value.copy(
                currentReport = report,
                showFullProductDetails = false,
                scanQuotaUsed = newQuota,
                captureStatus = ScanStatus.COMPLETED,
                activeScanJobId = null,
                currentScreen = Screen.ReportResult
            )
        }
    }

    fun openReport(scanId: String) {
        viewModelScope.launch {
            val report = repository.getFullReport(scanId)
            if (report != null) {
                _uiState.value = _uiState.value.copy(
                    currentReport = report,
                    showFullProductDetails = false,
                    currentScreen = Screen.ReportResult
                )
            }
        }
    }

    fun toggleSaveCurrentReport() {
        val report = _uiState.value.currentReport ?: return
        viewModelScope.launch {
            val isCurrentlySaved = savedScans.value.any { it.id == report.scanId }
            repository.toggleSaved(report.scanId, isCurrentlySaved)
            showToast(if (!isCurrentlySaved) "Product added to watchlist" else "Removed from watchlist")
        }
    }

    fun toggleSavedScan(scanId: String, currentSaved: Boolean) {
        viewModelScope.launch {
            repository.toggleSaved(scanId, currentSaved)
        }
    }

    fun confirmDeleteScan(scanId: String?) {
        _uiState.value = _uiState.value.copy(deleteConfirmationScanId = scanId)
    }

    fun executeDeleteScan() {
        val scanId = _uiState.value.deleteConfirmationScanId ?: return
        viewModelScope.launch {
            repository.deleteScan(scanId)
            _uiState.value = _uiState.value.copy(deleteConfirmationScanId = null)
            showToast("Scan record deleted")
            if (_uiState.value.currentReport?.scanId == scanId) {
                navigateBack()
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            showToast("All scan records deleted")
        }
    }

    fun setBatchScanMode(enabled: Boolean) {
        HapticsManager.triggerSelectionTap(getApplication())
        _uiState.value = _uiState.value.copy(isBatchScanMode = enabled)
    }

    fun startBatchScanFromHome() {
        HapticsManager.triggerSelectionTap(getApplication())
        _uiState.value = _uiState.value.copy(isBatchScanMode = true)
        navigateTo(Screen.Scanner)
    }

    fun enqueueBatchBitmap(bitmap: android.graphics.Bitmap, labelHint: String? = null) {
        val nextIdx = _uiState.value.batchQueue.size + 1
        val item = BatchQueueItem(
            bitmap = bitmap,
            labelHint = labelHint ?: "Label #$nextIdx"
        )
        _uiState.value = _uiState.value.copy(
            batchQueue = _uiState.value.batchQueue + item
        )
        showToast("Added ${item.labelHint} to Batch Queue (${_uiState.value.batchQueue.size} items)")
    }

    fun enqueueBatchSample(sample: SampleLabel) {
        val item = BatchQueueItem(
            presetSample = sample,
            labelHint = sample.name
        )
        _uiState.value = _uiState.value.copy(
            batchQueue = _uiState.value.batchQueue + item
        )
        showToast("Added \"${sample.name}\" to Batch Queue (${_uiState.value.batchQueue.size} items)")
    }

    fun removeBatchQueueItem(id: String) {
        HapticsManager.triggerSelectionTap(getApplication())
        _uiState.value = _uiState.value.copy(
            batchQueue = _uiState.value.batchQueue.filterNot { it.id == id }
        )
    }

    fun clearBatchQueue() {
        HapticsManager.triggerSelectionTap(getApplication())
        _uiState.value = _uiState.value.copy(batchQueue = emptyList())
        showToast("Cleared batch queue")
    }

    fun analyzeBatchQueue() {
        val items = _uiState.value.batchQueue
        if (items.isEmpty()) {
            showToast("Capture or select at least 1 label for batch analysis")
            return
        }

        _uiState.value = _uiState.value.copy(
            batchProgressIndex = 1,
            batchTotalCount = items.size,
            analysisStep = 1
        )
        navigateTo(Screen.AnalysisProgress)

        viewModelScope.launch {
            delay(250)
            val summary = repository.processAndSaveBatchScans(
                items = items,
                region = _uiState.value.region,
                onItemProgress = { currentIdx, total ->
                    val mappedStep = when {
                        total <= 1 -> 3
                        currentIdx == 1 -> 1
                        currentIdx < total -> 2 + (currentIdx % 2)
                        else -> 4
                    }
                    _uiState.value = _uiState.value.copy(
                        batchProgressIndex = currentIdx,
                        batchTotalCount = total,
                        analysisStep = mappedStep
                    )
                }
            )
            delay(250)
            val newQuota = _uiState.value.scanQuotaUsed + items.size
            HapticsManager.triggerAnalysisSuccess(getApplication())
            _uiState.value = _uiState.value.copy(
                currentBatchSummary = summary,
                currentReport = summary.safestReport ?: summary.reports.firstOrNull(),
                batchQueue = emptyList(),
                batchProgressIndex = 0,
                batchTotalCount = 0,
                scanQuotaUsed = newQuota,
                currentScreen = Screen.BatchReport
            )
        }
    }

    fun openReportFromBatch(scanId: String) {
        val batchReport = _uiState.value.currentBatchSummary?.reports?.firstOrNull { it.scanId == scanId }
        if (batchReport != null) {
            screenStack.add(Screen.BatchReport)
            _uiState.value = _uiState.value.copy(
                currentReport = batchReport,
                showFullProductDetails = false,
                returnToBatchReport = true,
                currentScreen = Screen.ReportResult
            )
        } else {
            viewModelScope.launch {
                val report = repository.getFullReport(scanId)
                if (report != null) {
                    screenStack.add(Screen.BatchReport)
                    _uiState.value = _uiState.value.copy(
                        currentReport = report,
                        showFullProductDetails = false,
                        returnToBatchReport = true,
                        currentScreen = Screen.ReportResult
                    )
                }
            }
        }
    }

    fun compareBatchProducts(leftScanId: String, rightScanId: String) {
        _uiState.value = _uiState.value.copy(
            compareLeftScanId = leftScanId,
            compareRightScanId = rightScanId
        )
        navigateTo(Screen.CompareProducts)
    }

    fun togglePreference(key: String, currentEnabled: Boolean) {
        viewModelScope.launch {
            repository.setPreference(key, !currentEnabled)
        }
    }

    fun updatePreferenceRuleDetails(
        key: String,
        ruleClassification: String,
        userReasonNote: String
    ) {
        viewModelScope.launch {
            HapticsManager.triggerSelectionTap(getApplication())
            repository.updatePreferenceRuleDetails(key, ruleClassification, userReasonNote)
            showToast("Updated allergen rule & reason note")
        }
    }

    fun addCustomAllergen(
        name: String,
        severityLevel: String = "HIGH_RISK",
        ruleClassification: String = if (severityLevel.equals("HIGH_RISK", ignoreCase = true)) "MEDICAL_ALLERGY" else "PERSONAL_SENSITIVITY"
    ) {
        viewModelScope.launch {
            repository.addCustomAllergen(name, severityLevel, ruleClassification)
            showToast("Added \"$name\" to Allergy Filter")
        }
    }

    fun deleteCustomAllergen(id: String) {
        viewModelScope.launch {
            repository.deleteCustomAllergen(id)
            showToast("Removed from Allergy Filter")
        }
    }

    fun toggleCustomAllergen(id: String, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleCustomAllergen(id, !isEnabled)
        }
    }

    fun toggleCustomAllergenSeverity(id: String, currentSeverity: String) {
        val nextSeverity = if (currentSeverity.equals("WARNING", ignoreCase = true)) "HIGH_RISK" else "WARNING"
        viewModelScope.launch {
            HapticsManager.triggerSelectionTap(getApplication())
            repository.updateCustomAllergenSeverity(id, nextSeverity)
            val label = if (nextSeverity == "HIGH_RISK") "Medical Allergy (High Risk)" else "Personal Sensitivity (Caution)"
            showToast("Allergen rule set to $label")
        }
    }

    fun updateCustomAllergenDetails(
        id: String,
        severityLevel: String,
        ruleClassification: String
    ) {
        viewModelScope.launch {
            HapticsManager.triggerSelectionTap(getApplication())
            repository.updateCustomAllergenDetails(id, severityLevel, ruleClassification)
            showToast("Updated custom allergen classification")
        }
    }

    fun saveCurrentAsAllergenProfile(profileName: String, description: String = "") {
        val trimmedName = profileName.trim()
        if (trimmedName.isBlank()) {
            showToast("Enter a profile name")
            return
        }
        viewModelScope.launch {
            val enabledKeys = preferences.value.filter { it.isEnabled }.map { it.key }
            val activeTriggers = customAllergens.value.filter { it.isEnabled }.map { it.name }
            val resolvedDesc = description.trim().ifBlank {
                "${enabledKeys.size} standard rules • ${activeTriggers.size} custom triggers"
            }
            repository.saveUserAllergenProfile(
                profileName = trimmedName,
                description = resolvedDesc,
                enabledPreferenceKeys = enabledKeys,
                customTriggers = activeTriggers,
                activateImmediately = true
            )
            HapticsManager.triggerSelectionTap(getApplication())
            showToast("Saved & activated profile \"$trimmedName\"")
        }
    }

    fun activateAllergenProfile(profileId: String) {
        viewModelScope.launch {
            HapticsManager.triggerSelectionTap(getApplication())
            repository.activateAllergenProfile(profileId)
            val name = allergenProfiles.value.firstOrNull { it.id == profileId }?.profileName ?: "Allergen Profile"
            showToast("Activated profile: $name")
        }
    }

    fun deleteAllergenProfile(profileId: String) {
        viewModelScope.launch {
            repository.deleteAllergenProfile(profileId)
            showToast("Deleted allergen profile")
        }
    }

    fun setHistorySearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(historySearchQuery = query)
    }

    fun setActiveFilter(filter: String) {
        _uiState.value = _uiState.value.copy(activeFilter = filter)
    }

    fun setHistoryFilter(filter: String) {
        setActiveFilter(filter)
    }

    fun setSelectedFinding(finding: ReportFinding?) {
        _uiState.value = _uiState.value.copy(selectedFinding = finding)
    }

    fun setSelectedIngredient(ingredient: ParsedIngredient?) {
        _uiState.value = _uiState.value.copy(selectedIngredient = ingredient)
    }

    fun showPaywall(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPaywall = show)
    }

    fun upgradeToPremium() {
        persistAuthSession(
            isLoggedIn = _uiState.value.isAuthenticated,
            fullName = _uiState.value.userName,
            email = _uiState.value.userEmail,
            provider = _uiState.value.authProvider,
            isPremium = true
        )
        _uiState.value = _uiState.value.copy(
            isPremium = true,
            showPaywall = false
        )
        showToast("Welcome to PureScan Premium! Unlimited scans unlocked.")
    }

    fun showFeedback(show: Boolean) {
        _uiState.value = _uiState.value.copy(showFeedbackDialog = show)
    }

    fun submitFeedback(category: String, message: String) {
        _uiState.value = _uiState.value.copy(showFeedbackDialog = false)
        showToast("Report submitted to review queue. Thank you!")
    }

    fun setThemeMode(mode: ThemeMode) {
        authPrefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _uiState.value = _uiState.value.copy(themeMode = mode)
        showToast("Theme set to ${mode.label}")
    }

    fun cycleThemeMode() {
        val next = when (_uiState.value.themeMode) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
        }
        setThemeMode(next)
    }

    fun setOutdoorMode(enabled: Boolean) {
        authPrefs.edit().putBoolean(KEY_OUTDOOR_MODE, enabled).apply()
        _uiState.value = _uiState.value.copy(outdoorMode = enabled)
        HapticsManager.triggerSelectionTap(getApplication())
        showToast(if (enabled) "Outdoor Readability High-Contrast Mode ON" else "Outdoor Readability Mode OFF")
    }

    fun setHapticsEnabled(enabled: Boolean) {
        authPrefs.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
        HapticsManager.isEnabled = enabled
        _uiState.value = _uiState.value.copy(hapticsEnabled = enabled)
        if (enabled) {
            HapticsManager.triggerSelectionTap(getApplication())
        }
        showToast(if (enabled) "Subtle Haptic Feedback ON" else "Subtle Haptic Feedback OFF")
    }

    fun setAppLanguage(language: AppLanguage) {
        authPrefs.edit().putString(KEY_APP_LANGUAGE, language.code).apply()
        HapticsManager.triggerSelectionTap(getApplication())
        _uiState.value = _uiState.value.copy(
            selectedLanguage = language,
            showLanguageDialog = false
        )
        showToast("Language set to ${language.nativeName} (${language.englishName})")
    }

    fun setShowLanguageDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showLanguageDialog = show)
    }

    fun setShowHowItWorksDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showHowItWorksDialog = show)
    }

    fun setDataLoading(loading: Boolean) {
        _uiState.value = _uiState.value.copy(isDataLoading = loading)
    }

    fun testGlossaryIngredientInAnalyzer(ingredientName: String, category: String) {
        _uiState.value = _uiState.value.copy(
            productTitle = "$ingredientName Evaluation",
            category = category,
            rawOcrText = "Aqua, Glycerin, $ingredientName",
            editableText = "Aqua, Glycerin, $ingredientName"
        )
        navigateTo(Screen.TextReview)
    }

    fun showToast(msg: String) {
        _uiState.value = _uiState.value.copy(notificationMessage = msg)
    }

    fun dismissToast() {
        _uiState.value = _uiState.value.copy(notificationMessage = null)
    }
}
