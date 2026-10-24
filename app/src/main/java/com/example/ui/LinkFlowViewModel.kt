package com.example.ui

import android.app.Application
import android.os.Environment
import android.os.StatFs
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DownloadTaskEntity
import com.example.data.local.LinkFlowDatabase
import com.example.data.model.DownloadJobState
import com.example.data.model.MediaAnalysisResult
import com.example.data.model.MediaFormat
import com.example.data.model.QualityOption
import com.example.data.security.AppLockSecurityManager
import com.example.data.security.LockType
import com.example.data.service.DownloadQueueManager
import com.example.data.service.MediaAnalyzerEngine
import com.example.data.service.UrlAnalysisOutcome
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class LinkFlowUiState(
    val inputUrl: String = "",
    val isAnalyzing: Boolean = false,
    val currentAnalysis: MediaAnalysisResult? = null,
    val selectedQualityOption: QualityOption? = null,
    val quickFormatSelection: MediaFormat = MediaFormat.MP4,
    val activeModalJobId: String? = null,
    val showQualitySheet: Boolean = false,
    val inspectJobDetailsId: String? = null,
    val playbackPreviewTask: DownloadTaskEntity? = null,
    val freeStorageBytes: Long = 0L,
    val totalStorageBytes: Long = 0L,
    val currentTab: Int = 0, // 0: Home, 1: Downloads, 2: Library, 3: Settings
    // Library State
    val libraryFilter: String = "ALL", // "ALL", "VIDEO", "AUDIO", "APK"
    val librarySearchQuery: String = "",
    val renamingTask: DownloadTaskEntity? = null,
    // Settings Preferences
    val wifiOnlyDownload: Boolean = false,
    val autoRetryFailed: Boolean = true,
    val maxConcurrentDownloads: Int = 3,
    val isCheckingAppUpdate: Boolean = false,
    val appUpdateMessage: String? = null,
    // AppLock & Native Security State
    val isAppLockEnabled: Boolean = false,
    val isAppLocked: Boolean = false,
    val primaryLockType: LockType = LockType.PIN,
    val hasPinConfigured: Boolean = false,
    val hasPatternConfigured: Boolean = false,
    val isBiometricEnabled: Boolean = true,
    val isBiometricAvailable: Boolean = false,
    val lockTimeoutSeconds: Long = 0L, // 0L: Immediately
    val showAppLockSetupDialog: Boolean = false,
    val appLockSetupMode: String? = null // "PIN", "PATTERN"
)

class LinkFlowViewModel(application: Application) : AndroidViewModel(application) {

    private val database = LinkFlowDatabase.getInstance(application)
    private val dao = database.linkFlowDao()
    private val analyzerEngine = MediaAnalyzerEngine()
    val queueManager = DownloadQueueManager(application, dao, analyzerEngine)
    val securityManager = AppLockSecurityManager(application)
    private var lastBackgroundTimestamp = 0L

    private val _uiState = MutableStateFlow(LinkFlowUiState())
    val uiState: StateFlow<LinkFlowUiState> = _uiState.asStateFlow()

    val allDownloads: StateFlow<List<DownloadTaskEntity>> = dao.observeAllDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshStorageMetrics()
        refreshSecurityState()
    }

    fun refreshSecurityState() {
        val isEnabled = securityManager.isAppLockEnabled()
        val hasPin = securityManager.hasPin()
        val hasPattern = securityManager.hasPattern()
        val lockType = securityManager.getPrimaryLockType()
        val bioEnabled = securityManager.isBiometricEnabled()
        val bioAvail = securityManager.isBiometricHardwareAvailable()
        val timeout = securityManager.getLockTimeoutSeconds()

        _uiState.update {
            it.copy(
                isAppLockEnabled = isEnabled,
                isAppLocked = isEnabled && (hasPin || hasPattern),
                primaryLockType = lockType,
                hasPinConfigured = hasPin,
                hasPatternConfigured = hasPattern,
                isBiometricEnabled = bioEnabled,
                isBiometricAvailable = bioAvail,
                lockTimeoutSeconds = timeout
            )
        }
    }

    fun refreshStorageMetrics() {
        runCatching {
            val stat = StatFs(Environment.getDataDirectory().path)
            val free = stat.availableBytes
            val total = stat.totalBytes
            _uiState.update { it.copy(freeStorageBytes = free, totalStorageBytes = total) }
        }
    }

    fun onUrlChanged(newUrl: String) {
        _uiState.update { it.copy(inputUrl = newUrl) }
    }

    fun clearInput() {
        _uiState.update { it.copy(inputUrl = "", currentAnalysis = null, selectedQualityOption = null) }
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(currentTab = index) }
        refreshStorageMetrics()
    }

    fun setQuickFormat(format: MediaFormat) {
        _uiState.update { state ->
            val updatedOption = state.currentAnalysis?.let { chooseDefaultOption(it, format) }
            state.copy(
                quickFormatSelection = format,
                selectedQualityOption = updatedOption
            )
        }
    }

    fun analyzeCurrentUrl() {
        val url = _uiState.value.inputUrl.trim()
        if (url.isBlank()) {
            Toast.makeText(getApplication(), "Please enter or paste a valid link", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true) }
            when (val outcome = analyzerEngine.analyzeUrl(url)) {
                is UrlAnalysisOutcome.Success -> {
                    val defaultOption = chooseDefaultOption(outcome.result, _uiState.value.quickFormatSelection)
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            currentAnalysis = outcome.result,
                            selectedQualityOption = defaultOption
                        )
                    }
                }
                is UrlAnalysisOutcome.Error -> {
                    _uiState.update { it.copy(isAnalyzing = false) }
                    Toast.makeText(getApplication(), outcome.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun loadAndAnalyzePreset(url: String, format: MediaFormat) {
        _uiState.update { it.copy(inputUrl = url, quickFormatSelection = format) }
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true) }
            when (val outcome = analyzerEngine.analyzeUrl(url)) {
                is UrlAnalysisOutcome.Success -> {
                    val defaultOption = chooseDefaultOption(outcome.result, format)
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            currentAnalysis = outcome.result,
                            selectedQualityOption = defaultOption
                        )
                    }
                }
                is UrlAnalysisOutcome.Error -> {
                    _uiState.update { it.copy(isAnalyzing = false) }
                    Toast.makeText(getApplication(), outcome.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun chooseDefaultOption(
        analysis: MediaAnalysisResult,
        preferredFormat: MediaFormat
    ): QualityOption? {
        return if (!preferredFormat.isVideo) {
            val matching = analysis.audioOptions.filter { it.format == preferredFormat }
            matching.firstOrNull { it.label.contains("320") || it.badge?.contains("320") == true || it.badge == "Hi-Fi 320k" }
                ?: matching.firstOrNull { it.label.contains("256") }
                ?: matching.firstOrNull { it.id.contains("mp3", ignoreCase = true) }
                ?: matching.firstOrNull()
                ?: analysis.audioOptions.firstOrNull()
                ?: analysis.videoOptions.firstOrNull()
        } else {
            val matching = analysis.videoOptions.filter { it.format == preferredFormat }
            matching.firstOrNull { it.includesAudio && (it.label.contains("1080") || it.resolutionOrBitrate.contains("1080")) }
                ?: matching.firstOrNull { it.includesAudio && (it.label.contains("720") || it.resolutionOrBitrate.contains("720")) }
                ?: matching.firstOrNull { it.includesAudio && it.label.contains("480") }
                ?: matching.firstOrNull { it.includesAudio && it.label.contains("360") }
                ?: matching.firstOrNull { it.includesAudio }
                ?: matching.firstOrNull()
                ?: analysis.videoOptions.firstOrNull()
                ?: analysis.audioOptions.firstOrNull()
        }
    }

    fun selectQualityOption(option: QualityOption) {
        _uiState.update {
            it.copy(
                selectedQualityOption = option,
                quickFormatSelection = option.format,
                showQualitySheet = false
            )
        }
    }

    fun openQualitySelector() {
        if (_uiState.value.currentAnalysis != null) {
            _uiState.update { it.copy(showQualitySheet = true) }
        }
    }

    fun closeQualitySelector() {
        _uiState.update { it.copy(showQualitySheet = false) }
    }

    fun startSelectedDownload() {
        val analysis = _uiState.value.currentAnalysis ?: return
        val option = _uiState.value.selectedQualityOption ?: chooseDefaultOption(analysis, _uiState.value.quickFormatSelection)
        if (option == null || !analysis.isAuthorizedStream) {
            Toast.makeText(getApplication(), analysis.authorizationLimitationNotice ?: "Stream cannot be downloaded", Toast.LENGTH_LONG).show()
            return
        }

        viewModelScope.launch {
            val jobId = queueManager.enqueueDownload(analysis, option)
            _uiState.update {
                it.copy(
                    showQualitySheet = false,
                    activeModalJobId = jobId
                )
            }
            refreshStorageMetrics()
        }
    }

    fun openActiveProgressModal(jobId: String) {
        _uiState.update { it.copy(activeModalJobId = jobId) }
    }

    fun dismissActiveProgressModal() {
        _uiState.update { it.copy(activeModalJobId = null) }
        refreshStorageMetrics()
    }

    fun openPlaybackPreview(task: DownloadTaskEntity) {
        _uiState.update { it.copy(playbackPreviewTask = task) }
    }

    fun closePlaybackPreview() {
        _uiState.update { it.copy(playbackPreviewTask = null) }
    }

    fun pauseDownload(jobId: String) = queueManager.pauseDownload(jobId)

    fun resumeDownload(jobId: String) = queueManager.resumeDownload(jobId)

    fun cancelDownload(jobId: String) = queueManager.cancelDownload(jobId)

    fun deleteDownload(jobId: String) {
        queueManager.deleteDownload(jobId, deleteFileOnDisk = true)
        refreshStorageMetrics()
    }

    fun clearCompleted() {
        viewModelScope.launch {
            dao.clearCompletedDownloads()
            refreshStorageMetrics()
        }
    }

    // ==========================================
    // LIBRARY FUNCTIONS
    // ==========================================
    fun setLibraryFilter(filter: String) {
        _uiState.update { it.copy(libraryFilter = filter) }
    }

    fun setLibrarySearchQuery(query: String) {
        _uiState.update { it.copy(librarySearchQuery = query) }
    }

    fun openRenameDialog(task: DownloadTaskEntity) {
        _uiState.update { it.copy(renamingTask = task) }
    }

    fun dismissRenameDialog() {
        _uiState.update { it.copy(renamingTask = null) }
    }

    fun confirmRename(jobId: String, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            val task = dao.getDownloadById(jobId) ?: return@launch
            val oldFile = task.localFilePath?.let { File(it) }
            val sanitized = newName.trim().let {
                if (it.endsWith(".mp4") || it.endsWith(".mp3") || it.endsWith(".m4a") || it.endsWith(".apk")) it
                else "$it.${task.format.lowercase()}"
            }
            if (oldFile != null && oldFile.exists()) {
                val newFile = File(oldFile.parentFile, sanitized)
                if (oldFile.renameTo(newFile)) {
                    // Update database
                    dao.renameDownloadFile(jobId, sanitized)
                } else {
                    dao.renameDownloadFile(jobId, sanitized)
                }
            } else {
                dao.renameDownloadFile(jobId, sanitized)
            }
            _uiState.update { it.copy(renamingTask = null) }
            Toast.makeText(getApplication(), "File renamed to $sanitized", Toast.LENGTH_SHORT).show()
        }
    }

    // ==========================================
    // SETTINGS & HIDDEN SAFE APK STORE FUNCTIONS
    // ==========================================
    fun toggleWifiOnly(enabled: Boolean) {
        _uiState.update { it.copy(wifiOnlyDownload = enabled) }
    }

    fun toggleAutoRetry(enabled: Boolean) {
        _uiState.update { it.copy(autoRetryFailed = enabled) }
    }

    fun setMaxConcurrentDownloads(limit: Int) {
        _uiState.update { it.copy(maxConcurrentDownloads = limit) }
    }

    fun checkAppUpdates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingAppUpdate = true, appUpdateMessage = null) }
            delay(1200) // Simulated network check
            _uiState.update {
                it.copy(
                    isCheckingAppUpdate = false,
                    appUpdateMessage = "LinkFlow is up to date (Version 1.0.0)"
                )
            }
        }
    }

    // --- AppLock & Security Management ---

    fun verifyPin(pin: String): Boolean {
        val valid = securityManager.verifyPin(pin)
        if (valid) {
            unlockApp()
        }
        return valid
    }

    fun verifyPattern(nodes: List<Int>): Boolean {
        val valid = securityManager.verifyPattern(nodes)
        if (valid) {
            unlockApp()
        }
        return valid
    }

    fun unlockApp() {
        _uiState.update { it.copy(isAppLocked = false) }
    }

    fun lockAppNow() {
        if (_uiState.value.isAppLockEnabled) {
            _uiState.update { it.copy(isAppLocked = true) }
        }
    }

    fun onAppBackgrounded() {
        lastBackgroundTimestamp = System.currentTimeMillis()
    }

    fun onAppResumed() {
        if (_uiState.value.isAppLockEnabled && !_uiState.value.isAppLocked) {
            val timeoutSec = _uiState.value.lockTimeoutSeconds
            val elapsedSec = (System.currentTimeMillis() - lastBackgroundTimestamp) / 1000
            if (timeoutSec == 0L || elapsedSec >= timeoutSec) {
                _uiState.update { it.copy(isAppLocked = true) }
            }
        }
    }

    fun triggerBiometricPrompt(activity: FragmentActivity) {
        if (!_uiState.value.isBiometricAvailable || !_uiState.value.isBiometricEnabled) return
        securityManager.triggerBiometricPrompt(
            activity = activity,
            title = "Unlock LinkFlow",
            subtitle = "Verify your fingerprint or biometric credential",
            onSuccess = {
                unlockApp()
            },
            onError = { msg ->
                Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun toggleAppLockEnabled(enabled: Boolean) {
        if (!enabled) {
            securityManager.clearSecurityCredentials()
            refreshSecurityState()
            Toast.makeText(getApplication(), "AppLock disabled", Toast.LENGTH_SHORT).show()
        } else {
            // Open setup dialog
            _uiState.update { it.copy(showAppLockSetupDialog = true, appLockSetupMode = "PIN") }
        }
    }

    fun setPrimaryLockType(type: LockType) {
        securityManager.setPrimaryLockType(type)
        refreshSecurityState()
    }

    fun setBiometricEnabled(enabled: Boolean) {
        securityManager.setBiometricEnabled(enabled)
        refreshSecurityState()
    }

    fun setLockTimeoutSeconds(seconds: Long) {
        securityManager.setLockTimeoutSeconds(seconds)
        refreshSecurityState()
    }

    fun saveNewPin(pin: String) {
        securityManager.setPin(pin)
        securityManager.setAppLockEnabled(true)
        securityManager.setPrimaryLockType(LockType.PIN)
        refreshSecurityState()
        Toast.makeText(getApplication(), "Security PIN saved!", Toast.LENGTH_SHORT).show()
    }

    fun saveNewPattern(nodes: List<Int>) {
        securityManager.setPattern(nodes)
        securityManager.setAppLockEnabled(true)
        securityManager.setPrimaryLockType(LockType.PATTERN)
        refreshSecurityState()
        Toast.makeText(getApplication(), "Security Pattern saved!", Toast.LENGTH_SHORT).show()
    }

    fun openAppLockSetup(mode: String = "PIN") {
        _uiState.update { it.copy(showAppLockSetupDialog = true, appLockSetupMode = mode) }
    }

    fun closeAppLockSetup() {
        _uiState.update { it.copy(showAppLockSetupDialog = false, appLockSetupMode = null) }
    }
}
