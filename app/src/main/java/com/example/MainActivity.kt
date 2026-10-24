package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.DownloadJobState
import com.example.ui.LinkFlowViewModel
import com.example.ui.components.LiquidGlassNavigationBar
import com.example.ui.screens.ActiveDownloadProgressModal
import com.example.ui.screens.AppLockGateScreen
import com.example.ui.screens.DownloadsHistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.QualitySelectorSheet
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.LightText
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.LinkFlowTheme

class MainActivity : FragmentActivity() {

    private val viewModel: LinkFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingSharedIntent(intent)

        setContent {
            LinkFlowTheme {
                LinkFlowApp(viewModel = viewModel)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onAppBackgrounded()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppResumed()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingSharedIntent(intent)
    }

    private fun handleIncomingSharedIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                val urlMatch = Regex("""https?://[^\s]+""").find(sharedText)?.value ?: sharedText.trim()
                viewModel.onUrlChanged(urlMatch)
                viewModel.analyzeCurrentUrl()
            }
        }
    }
}

@Composable
fun LinkFlowApp(viewModel: LinkFlowViewModel) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val uiState by viewModel.uiState.collectAsState()
    val allDownloads by viewModel.allDownloads.collectAsState()

    val activeDownloads = allDownloads.filter {
        it.state == DownloadJobState.DOWNLOADING.name ||
            it.state == DownloadJobState.PREPARING.name ||
            it.state == DownloadJobState.QUEUED.name ||
            it.state == DownloadJobState.PROCESSING.name
    }
    val completedCount = allDownloads.count { it.state == DownloadJobState.COMPLETED.name }

    val activeModalTask = allDownloads.firstOrNull { it.jobId == uiState.activeModalJobId }

    // Intercept back button when AppLock gate is active
    BackHandler(enabled = uiState.isAppLocked) {
        // App is locked; do not allow backing into protected content
    }

    // Handle Back Press Gracefully for secondary dialogs / tabs
    BackHandler(enabled = !uiState.isAppLocked && (uiState.currentTab != 0 || uiState.showQualitySheet || uiState.activeModalJobId != null || uiState.playbackPreviewTask != null || uiState.renamingTask != null)) {
        when {
            uiState.showQualitySheet -> viewModel.closeQualitySelector()
            uiState.activeModalJobId != null -> viewModel.dismissActiveProgressModal()
            uiState.playbackPreviewTask != null -> viewModel.closePlaybackPreview()
            uiState.renamingTask != null -> viewModel.dismissRenameDialog()
            uiState.currentTab != 0 -> viewModel.setTab(0)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            LiquidGlassNavigationBar(
                selectedTab = uiState.currentTab,
                onTabSelected = viewModel::setTab,
                activeDownloadsCount = activeDownloads.size,
                libraryCount = completedCount
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = uiState.currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "tab_transition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> HomeScreen(
                        uiState = uiState,
                        activeDownloads = activeDownloads,
                        onUrlChanged = viewModel::onUrlChanged,
                        onClearInput = viewModel::clearInput,
                        onAnalyze = viewModel::analyzeCurrentUrl,
                        onQuickFormatSelected = viewModel::setQuickFormat,
                        onOpenQualitySelector = viewModel::openQualitySelector,
                        onStartDownload = viewModel::startSelectedDownload,
                        onOpenDownloadModal = viewModel::openActiveProgressModal,
                        onSelectPreset = viewModel::loadAndAnalyzePreset
                    )
                    1 -> DownloadsHistoryScreen(
                        downloads = allDownloads,
                        onOpenModal = viewModel::openActiveProgressModal,
                        onPause = viewModel::pauseDownload,
                        onResume = viewModel::resumeDownload,
                        onDelete = viewModel::deleteDownload,
                        onClearCompleted = viewModel::clearCompleted
                    )
                    2 -> LibraryScreen(
                        downloads = allDownloads,
                        selectedFilter = uiState.libraryFilter,
                        searchQuery = uiState.librarySearchQuery,
                        renamingTask = uiState.renamingTask,
                        playbackPreviewTask = uiState.playbackPreviewTask,
                        onFilterChange = viewModel::setLibraryFilter,
                        onSearchChange = viewModel::setLibrarySearchQuery,
                        onOpenRename = viewModel::openRenameDialog,
                        onDismissRename = viewModel::dismissRenameDialog,
                        onConfirmRename = viewModel::confirmRename,
                        onOpenPreview = viewModel::openPlaybackPreview,
                        onClosePreview = viewModel::closePlaybackPreview,
                        onDelete = viewModel::deleteDownload
                    )
                    3 -> SettingsScreen(
                        uiState = uiState,
                        onToggleWifiOnly = viewModel::toggleWifiOnly,
                        onToggleAutoRetry = viewModel::toggleAutoRetry,
                        onSetMaxConcurrent = viewModel::setMaxConcurrentDownloads,
                        onCheckAppUpdates = viewModel::checkAppUpdates,
                        onToggleAppLock = viewModel::toggleAppLockEnabled,
                        onSetPrimaryLockType = viewModel::setPrimaryLockType,
                        onToggleBiometric = viewModel::setBiometricEnabled,
                        onSetLockTimeout = viewModel::setLockTimeoutSeconds,
                        onLockAppNow = viewModel::lockAppNow,
                        onOpenAppLockSetup = viewModel::openAppLockSetup,
                        onCloseAppLockSetup = viewModel::closeAppLockSetup,
                        onSavePin = viewModel::saveNewPin,
                        onSavePattern = viewModel::saveNewPattern
                    )
                }
            }

            // Quality Selection Sheet
            if (uiState.showQualitySheet && uiState.currentAnalysis != null) {
                QualitySelectorSheet(
                    analysis = uiState.currentAnalysis!!,
                    selectedOption = uiState.selectedQualityOption,
                    onSelectOption = viewModel::selectQualityOption,
                    onDismiss = viewModel::closeQualitySelector
                )
            }

            // Active Progress Dialog Modal
            if (uiState.activeModalJobId != null) {
                ActiveDownloadProgressModal(
                    task = activeModalTask,
                    onPause = { activeModalTask?.let { viewModel.pauseDownload(it.jobId) } },
                    onResume = { activeModalTask?.let { viewModel.resumeDownload(it.jobId) } },
                    onCancel = {
                        activeModalTask?.let { viewModel.cancelDownload(it.jobId) }
                        viewModel.dismissActiveProgressModal()
                    },
                    onDismiss = viewModel::dismissActiveProgressModal
                )
            }

            // Fullscreen Native AppLock Gate
            if (uiState.isAppLocked) {
                AppLockGateScreen(
                    currentLockType = uiState.primaryLockType,
                    hasPinConfigured = uiState.hasPinConfigured,
                    hasPatternConfigured = uiState.hasPatternConfigured,
                    isBiometricAvailable = uiState.isBiometricAvailable,
                    isBiometricEnabled = uiState.isBiometricEnabled,
                    onVerifyPin = viewModel::verifyPin,
                    onVerifyPattern = viewModel::verifyPattern,
                    onTriggerBiometric = { activity?.let { viewModel.triggerBiometricPrompt(it) } },
                    onUnlockSuccess = viewModel::unlockApp
                )
            }
        }
    }
}
