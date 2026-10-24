package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ScanViewModel
import com.example.ui.Screen
import com.example.ui.components.FeedbackModal
import com.example.ui.components.PaywallModal
import com.example.ui.components.ScanSafeBottomNavigation
import com.example.ui.screens.AnalysisProgressScreen
import com.example.ui.screens.CompareProductsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IngredientGlossaryScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PreferencesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReportResultScreen
import com.example.ui.screens.SavedWatchlistScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.TextReviewScreen
import com.example.ui.theme.FloralWhite
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScanSafeApp()
        }
    }
}

@Composable
fun ScanSafeApp(viewModel: ScanViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    MyApplicationTheme(
        themeMode = uiState.themeMode,
        outdoorHighContrast = uiState.outdoorMode
    ) {

    LaunchedEffect(uiState.notificationMessage) {
        uiState.notificationMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissToast()
        }
    }

    // Determine whether bottom navigation should be visible
    val showBottomNav = when (uiState.currentScreen) {
        Screen.Home, Screen.History, Screen.Saved, Screen.Profile -> true
        else -> false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = FloralWhite,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomNav) {
                Box(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                    ScanSafeBottomNavigation(
                        currentScreen = uiState.currentScreen,
                        onNavigate = { screen -> viewModel.navigateTo(screen) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentScreen) {
                Screen.Splash -> com.example.ui.screens.SplashScreen(viewModel = viewModel)
                Screen.Onboarding -> OnboardingScreen(viewModel = viewModel)
                Screen.Auth -> com.example.ui.screens.AuthScreen(viewModel = viewModel)
                Screen.Home -> HomeScreen(viewModel = viewModel)
                Screen.Scanner -> ScannerScreen(viewModel = viewModel)
                Screen.TextReview -> TextReviewScreen(viewModel = viewModel)
                Screen.AnalysisProgress -> AnalysisProgressScreen(viewModel = viewModel)
                Screen.ReportResult -> ReportResultScreen(viewModel = viewModel)
                Screen.BatchReport -> com.example.ui.screens.BatchReportScreen(viewModel = viewModel)
                Screen.History -> HistoryScreen(viewModel = viewModel)
                Screen.Saved -> SavedWatchlistScreen(viewModel = viewModel)
                Screen.Preferences -> PreferencesScreen(viewModel = viewModel)
                Screen.Profile -> ProfileScreen(viewModel = viewModel)
                Screen.CompareProducts -> CompareProductsScreen(viewModel = viewModel)
                Screen.Glossary -> IngredientGlossaryScreen(viewModel = viewModel)
            }
        }
    }

    // Modal Overlays
    if (uiState.showPaywall) {
        PaywallModal(
            onDismiss = { viewModel.showPaywall(false) },
            onUpgrade = { viewModel.upgradeToPremium() }
        )
    }

    if (uiState.showFeedbackDialog) {
        FeedbackModal(
            onDismiss = { viewModel.showFeedback(false) },
            onSubmit = { category, msg -> viewModel.submitFeedback(category, msg) }
        )
    }
    }
}
