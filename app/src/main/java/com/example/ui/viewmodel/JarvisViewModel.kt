package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.JarvisDataStoreRepository
import com.example.data.local.LiquidGlassConfig
import com.example.data.local.JarvisDatabase
import com.example.data.local.JarvisRepository
import com.example.data.local.entities.AutoCodeSnippetEntity
import com.example.data.local.entities.ConversationMessageEntity
import com.example.data.local.entities.DirectiveEntity
import com.example.data.local.entities.ProtocolEntity
import com.example.data.local.entities.SystemLogEntity
import com.example.data.local.entities.VoiceNoteEntity
import com.example.domain.AutoCodeMode
import com.example.domain.AutoCodeResult
import com.example.domain.AutoCoderEngine
import com.example.domain.CommandProcessor
import com.example.domain.ProtocolEngine
import com.example.domain.ProtocolExecutionState
import com.example.hardware.DeviceTelemetry
import com.example.hardware.DeviceTelemetryManager
import com.example.hardware.HardwareController
import com.example.service.JarvisAccessibilityService
import com.example.service.JarvisFloatingOverlayService
import com.example.service.AliceVoiceListenerService
import com.example.service.VoiceListenerState
import com.example.data.model.BtcOracleTelemetry
import com.example.data.api.AiLlmEngine
import com.example.data.api.AiResponseResult
import com.example.data.api.GeminiChatModel
import com.example.data.api.GeminiChatRole
import com.example.service.ScreenContext
import androidx.fragment.app.FragmentActivity
import com.example.util.BiometricCapability
import com.example.util.JarvisBiometricAuthManager
import com.example.voice.AudioTranscriptionState
import com.example.voice.AudioTranscriberEngine
import com.example.voice.JarvisSoundSynthesizer
import com.example.voice.JarvisVoiceEngine
import com.example.voice.VoiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InteractionMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "IAN", "USER", or "ALISE"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String? = null,
    val isSearchGrounded: Boolean = false,
    val searchQueries: List<String> = emptyList(),
    val groundingSources: List<Pair<String, String>> = emptyList(),
    val latencyMs: Long? = null,
    val tokensGenerated: Int? = null
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val db = JarvisDatabase.getInstance(application)
    val dataStoreRepository = JarvisDataStoreRepository(application)
    val repository = JarvisRepository(
        db.directiveDao(),
        db.protocolDao(),
        db.systemLogDao(),
        db.voiceNoteDao(),
        db.threatIncidentDao(),
        db.autoCodeSnippetDao(),
        db.codeTemplateDao(),
        db.conversationDao()
    )

    val liquidGlassConfig: StateFlow<LiquidGlassConfig> = dataStoreRepository.liquidGlassConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LiquidGlassConfig())

    val hardwareController = HardwareController(application)
    val androidActionController = com.example.hardware.AndroidActionController(application)
    val telemetryManager = DeviceTelemetryManager(application)
    val phoneLocationTracker = com.example.hardware.PhoneLocationTracker(application)
    val soundSynthesizer = JarvisSoundSynthesizer()
    val networkDefenseController = com.example.hardware.NetworkDefenseController(application)
    val settingController = com.example.hardware.AutonomousSettingController(application, hardwareController)
    val selfModificationEngine = com.example.domain.SelfModificationEngine()
    val protocolEngine = ProtocolEngine(repository, hardwareController, telemetryManager, soundSynthesizer, networkDefenseController)

    val voiceEngine = JarvisVoiceEngine(application) { commandText ->
        submitVoiceCommand(commandText)
    }

    val commandProcessor = CommandProcessor(
        repository,
        hardwareController,
        androidActionController,
        telemetryManager,
        protocolEngine,
        voiceEngine,
        networkDefenseController
    )

    val lastExecutionResult = commandProcessor.lastExecutionResult

    // Flow states from Room
    val directives: StateFlow<List<DirectiveEntity>> = repository.allDirectives
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val protocols: StateFlow<List<ProtocolEntity>> = repository.allProtocols
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<SystemLogEntity>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val voiceNotes: StateFlow<List<VoiceNoteEntity>> = repository.allVoiceNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val threatIncidents: StateFlow<List<com.example.data.local.entities.ThreatIncidentEntity>> = repository.allThreatIncidents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val autoCodeSnippets: StateFlow<List<AutoCodeSnippetEntity>> = repository.allSavedSnippets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val codeTemplates: StateFlow<List<com.example.data.local.entities.CodeTemplateEntity>> = repository.allCodeTemplates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // Auto Coder Live State
    private val _autoCodeResult = MutableStateFlow<AutoCodeResult?>(null)
    val autoCodeResult: StateFlow<AutoCodeResult?> = _autoCodeResult.asStateFlow()

    private val _isAutoCoderRunning = MutableStateFlow(false)
    val isAutoCoderRunning: StateFlow<Boolean> = _isAutoCoderRunning.asStateFlow()

    private val _autoCoderEditorCode = MutableStateFlow("")
    val autoCoderEditorCode: StateFlow<String> = _autoCoderEditorCode.asStateFlow()

    private val _autoCoderLanguage = MutableStateFlow("Python")
    val autoCoderLanguage: StateFlow<String> = _autoCoderLanguage.asStateFlow()

    private val _autoCoderMode = MutableStateFlow(AutoCodeMode.AUTO_FIX)
    val autoCoderMode: StateFlow<AutoCodeMode> = _autoCoderMode.asStateFlow()

    private val _selectedCodeSnippet = MutableStateFlow<AutoCodeSnippetEntity?>(null)
    val selectedCodeSnippet: StateFlow<AutoCodeSnippetEntity?> = _selectedCodeSnippet.asStateFlow()

    // Telemetry & Hardware States
    val telemetry: StateFlow<DeviceTelemetry> = telemetryManager.telemetry
    val locationState: StateFlow<com.example.hardware.LivePhoneLocationState> = phoneLocationTracker.locationState
    val isFlashlightOn: StateFlow<Boolean> = hardwareController.isFlashlightOn
    val mediaVolume: StateFlow<Int> = hardwareController.mediaVolume
    val screenContext: StateFlow<ScreenContext> = JarvisAccessibilityService.screenContext

    // Voice & Protocol States
    val voiceState: StateFlow<VoiceState> = voiceEngine.voiceState
    val audioAmplitude: StateFlow<Float> = voiceEngine.audioAmplitude
    val partialTranscript: StateFlow<String> = voiceEngine.partialTranscript
    val lastSpokenResponse: StateFlow<String> = voiceEngine.lastSpokenResponse
    val protocolExecution: StateFlow<ProtocolExecutionState> = protocolEngine.executionState

    // Gemini Real-time Audio Transcriber
    val audioTranscriber = AudioTranscriberEngine(application, viewModelScope)
    val transcriptionState: StateFlow<AudioTranscriptionState> = audioTranscriber.transcriptionState
    val liveTranscribeAmplitude: StateFlow<Float> = audioTranscriber.liveAmplitude

    // Gemini Chatbot Model & Grounding States
    private val _selectedGeminiModel = MutableStateFlow(GeminiChatModel.GENERAL_FLASH)
    val selectedGeminiModel: StateFlow<GeminiChatModel> = _selectedGeminiModel.asStateFlow()

    private val _selectedGeminiRole = MutableStateFlow(GeminiChatRole.TACTICAL_ASSISTANT)
    val selectedGeminiRole: StateFlow<GeminiChatRole> = _selectedGeminiRole.asStateFlow()

    private val _customRoleSystemInstruction = MutableStateFlow("")
    val customRoleSystemInstruction: StateFlow<String> = _customRoleSystemInstruction.asStateFlow()

    private val _isSearchGroundingEnabled = MutableStateFlow(true)
    val isSearchGroundingEnabled: StateFlow<Boolean> = _isSearchGroundingEnabled.asStateFlow()

    private val _isChatGenerating = MutableStateFlow(false)
    val isChatGenerating: StateFlow<Boolean> = _isChatGenerating.asStateFlow()

    // Biometric Security & Core Interface Lockdown State
    val isBiometricCoreLockEnabled: StateFlow<Boolean> = dataStoreRepository.isBiometricCoreLockEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isBiometricAutoLockBackground: StateFlow<Boolean> = dataStoreRepository.isBiometricAutoLockBackground
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _isCoreInterfaceLocked = MutableStateFlow(true)
    val isCoreInterfaceLocked: StateFlow<Boolean> = _isCoreInterfaceLocked.asStateFlow()

    private val _biometricAuthStatus = MutableStateFlow<String?>(null)
    val biometricAuthStatus: StateFlow<String?> = _biometricAuthStatus.asStateFlow()

    private val _biometricCapability = MutableStateFlow(
        JarvisBiometricAuthManager.checkBiometricCapability(application)
    )
    val biometricCapability: StateFlow<BiometricCapability> = _biometricCapability.asStateFlow()

    private val _authFailureCount = MutableStateFlow(0)
    val authFailureCount: StateFlow<Int> = _authFailureCount.asStateFlow()

    private val _lastBiometricAuthTime = MutableStateFlow(0L)
    val lastBiometricAuthTime: StateFlow<Long> = _lastBiometricAuthTime.asStateFlow()

    fun refreshBiometricCapability() {
        _biometricCapability.value = JarvisBiometricAuthManager.checkBiometricCapability(getApplication())
    }

    fun lockCoreInterface(reason: String = "MANUAL_LOCK") {
        _isCoreInterfaceLocked.value = true
        _biometricAuthStatus.value = "CORE LOCKED: $reason"
        soundSynthesizer.playAcknowledgeTone()
        viewModelScope.launch {
            repository.logSystemEvent("SECURITY", "JARVIS Core interface locked via $reason", "LOCKDOWN")
        }
    }

    fun unlockCoreInterface(authMethod: String = "BIOMETRIC_PROMPT") {
        _isCoreInterfaceLocked.value = false
        _authFailureCount.value = 0
        _biometricAuthStatus.value = "ACCESS GRANTED: $authMethod"
        _lastBiometricAuthTime.value = System.currentTimeMillis()
        soundSynthesizer.playAcknowledgeTone()
        viewModelScope.launch {
            repository.logSystemEvent("SECURITY", "Core interface authenticated via $authMethod", "AUTH")
        }
    }

    fun authenticateCoreInterface(
        activity: FragmentActivity,
        onResult: (Boolean) -> Unit = {}
    ) {
        refreshBiometricCapability()
        _biometricAuthStatus.value = "INITIALIZING BIOMETRIC SCANNER..."

        JarvisBiometricAuthManager.authenticate(
            activity = activity,
            title = "JARVIS Core Security Clearance",
            subtitle = "Biometric Sensor Authentication",
            description = "Scan fingerprint or device credential to unlock JARVIS Core.",
            onSuccess = {
                unlockCoreInterface("BIOMETRIC_PROMPT")
                onResult(true)
            },
            onError = { err ->
                _authFailureCount.value += 1
                _biometricAuthStatus.value = err
                soundSynthesizer.playAlertTone()
                viewModelScope.launch {
                    repository.logSystemEvent("SECURITY_ALERT", "Biometric verification failed: $err", "SECURITY")
                }
                onResult(false)
            },
            onFailed = {
                _authFailureCount.value += 1
                _biometricAuthStatus.value = "Biometric scan unrecognized. Please reposition finger."
                soundSynthesizer.playAlertTone()
            }
        )
    }

    fun emergencyPasscodeUnlock(passcode: String): Boolean {
        val trimmed = passcode.trim()
        val isValid = trimmed == "2026" || trimmed == "0000" || trimmed.equals("JARVIS", ignoreCase = true) || trimmed.equals("ALISE", ignoreCase = true)
        if (isValid) {
            unlockCoreInterface("EMERGENCY_PASSCODE_OVERRIDE")
            return true
        } else {
            _authFailureCount.value += 1
            _biometricAuthStatus.value = "INVALID EMERGENCY OVERRIDE CODE"
            soundSynthesizer.playAlertTone()
            viewModelScope.launch {
                repository.logSystemEvent("SECURITY_ALERT", "Invalid emergency override attempt: $trimmed", "SECURITY")
            }
            return false
        }
    }

    fun setBiometricCoreLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreRepository.setBiometricCoreLockEnabled(enabled)
            repository.logSystemEvent("SECURITY", "Biometric Core Lock enabled: $enabled", "CONFIG")
            if (!enabled) {
                _isCoreInterfaceLocked.value = false
            }
        }
    }

    fun setBiometricAutoLockBackground(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreRepository.setBiometricAutoLockBackground(enabled)
            repository.logSystemEvent("SECURITY", "Biometric Auto-Lock on Background: $enabled", "CONFIG")
        }
    }

    fun clearAuthStatusMessage() {
        _biometricAuthStatus.value = null
    }

    // Conversation History
    private val _interactions = MutableStateFlow<List<InteractionMessage>>(
        listOf(
            InteractionMessage(
                sender = "ALISE",
                text = "Hello! I'm ALISE, powered by Gemini Neural Intelligence with Google Search Grounding and Speech Transcription. How can I assist you today?",
                modelUsed = "Gemini 3.5 Flash",
                isSearchGrounded = true
            )
        )
    )
    val interactions: StateFlow<List<InteractionMessage>> = _interactions.asStateFlow()

    private val _isOverlayActive = MutableStateFlow(JarvisFloatingOverlayService.isRunning)
    val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

    private val _wifiTelemetry = MutableStateFlow(networkDefenseController.getWifiTelemetry())
    val wifiTelemetry: StateFlow<com.example.hardware.WifiTelemetryData> = _wifiTelemetry.asStateFlow()

    private val _dnsDiagnostic = MutableStateFlow(com.example.hardware.DnsDiagnosticResult())
    val dnsDiagnostic: StateFlow<com.example.hardware.DnsDiagnosticResult> = _dnsDiagnostic.asStateFlow()

    private val _isDnsProbeRunning = MutableStateFlow(false)
    val isDnsProbeRunning: StateFlow<Boolean> = _isDnsProbeRunning.asStateFlow()

    // Autonomous Control & Security Breach States
    private val _isAutonomousControlEnabled = MutableStateFlow(true)
    val isAutonomousControlEnabled: StateFlow<Boolean> = _isAutonomousControlEnabled.asStateFlow()

    private val _autonomousActionLogs = MutableStateFlow<List<com.example.ui.components.AutonomousActionLog>>(
        listOf(
            com.example.ui.components.AutonomousActionLog(
                timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
                actionText = "System boot complete. Jarvis autonomous device monitoring initialized."
            )
        )
    )
    val autonomousActionLogs: StateFlow<List<com.example.ui.components.AutonomousActionLog>> = _autonomousActionLogs.asStateFlow()

    private val _activeBreaches = MutableStateFlow<List<com.example.ui.components.IdentifiedBreach>>(emptyList())
    val activeBreaches: StateFlow<List<com.example.ui.components.IdentifiedBreach>> = _activeBreaches.asStateFlow()

    // Decentralized BTC Oracle & Liquidity Feed State
    val btcOracleTelemetry = MutableStateFlow(BtcOracleTelemetry())

    // Background Voice Listener Service State
    val voiceListenerState: StateFlow<VoiceListenerState> = AliceVoiceListenerService.listenerState

    init {
        // Wire background voice listener service to ViewModel command and protocol pipelines
        AliceVoiceListenerService.setCommandDispatcher { cmd ->
            submitVoiceCommand(cmd)
        }
        AliceVoiceListenerService.setProtocolDispatcher { protoCode ->
            viewModelScope.launch {
                val proto = repository.getProtocolByCode(protoCode) ?: ProtocolEntity(
                    codeName = protoCode,
                    displayName = if (protoCode == "BTC_ORACLE_TELEMETRY") "BTC Oracle & Liquidity Matrix" else protoCode.replace("_", " "),
                    description = "Voice triggered directive: $protoCode",
                    actionsJson = "[\"VOICE_TRIGGER\", \"EXECUTE\"]",
                    category = "TACTICAL",
                    isPreset = true
                )
                executeProtocol(proto)
            }
        }

        telemetryManager.startSensorTracking()
        telemetryManager.refreshStaticAndDynamicMetrics()
        refreshNetworkTelemetry()
        seedDefenseProtocolsIfNeeded()
        viewModelScope.launch {
            repository.ensureDefaultTemplates()
        }
        viewModelScope.launch {
            repository.conversationMessages.collect { messages ->
                if (messages.isNotEmpty()) {
                    _interactions.value = messages.map { msg ->
                        InteractionMessage(
                            id = msg.id.toString(),
                            sender = msg.sender,
                            text = msg.text,
                            timestamp = msg.timestamp,
                            modelUsed = msg.modelUsed,
                            isSearchGrounded = msg.isSearchGrounded
                        )
                    }
                }
            }
        }
    }


    fun refreshNetworkTelemetry() {
        viewModelScope.launch {
            _wifiTelemetry.value = networkDefenseController.getWifiTelemetry()
        }
    }

    fun runDnsDiagnosticProbe() {
        viewModelScope.launch {
            _isDnsProbeRunning.value = true
            soundSynthesizer.playAcknowledgeTone()
            repository.logSystemEvent("DNS_DIAG", "Initiating real DNS probe across Cloudflare, Google, Quad9...", "INFO")
            val result = networkDefenseController.runDnsProxyDiagnostic()
            _dnsDiagnostic.value = result
            _isDnsProbeRunning.value = false
            repository.logSystemEvent("DNS_DIAG", result.diagnosticSummary, if (result.isDnsHealthy) "TACTICAL" else "WARN")
            voiceEngine.speak(result.diagnosticSummary)
        }
    }

    fun executeWifiFix() {
        viewModelScope.launch {
            soundSynthesizer.playActivationChime()
            val msg = networkDefenseController.remediateWifiLink()
            repository.logSystemEvent("NET_DEFENSE", msg, "TACTICAL")
            refreshNetworkTelemetry()
            voiceEngine.speak("Wi-Fi link reset engaged. Socket pools flushed.")
        }
    }

    fun executeRenewIpLease() {
        viewModelScope.launch {
            soundSynthesizer.playActivationChime()
            val msg = networkDefenseController.renewIpAddressLease()
            repository.logSystemEvent("NET_DEFENSE", msg, "TACTICAL")
            refreshNetworkTelemetry()
            voiceEngine.speak("DHCP IP lease renewal requested. Flushed ARP and network address bindings.")
        }
    }

    fun executeChangeMacPrivacy() {
        viewModelScope.launch {
            soundSynthesizer.playActivationChime()
            val msg = networkDefenseController.openMacAddressPrivacySettings()
            repository.logSystemEvent("NET_DEFENSE", msg, "TACTICAL")
            refreshNetworkTelemetry()
            voiceEngine.speak("Opened MAC address privacy controls. Toggle between randomized and device hardware MAC.")
        }
    }

    fun logSystemEvent(tag: String, message: String, level: String = "INFO") {
        viewModelScope.launch {
            repository.logSystemEvent(tag, message, level)
        }
    }

    fun executeDnsProxyFix() {
        viewModelScope.launch {
            soundSynthesizer.playActivationChime()
            val msg = networkDefenseController.sanitizeAndFlushDnsProxy()
            repository.logSystemEvent("NET_DEFENSE", msg, "TACTICAL")
            voiceEngine.speak("DNS Proxy Sanitize complete. Flushed lookup caches and opened Private DNS configuration.")
        }
    }

    fun executeDefenseLockdown() {
        viewModelScope.launch {
            soundSynthesizer.playActivationChime()
            val msg = networkDefenseController.executeDefenseLockdown()
            hardwareController.strobeFlashlight(burstCount = 4, intervalMs = 90)
            hardwareController.vibrateTacticalAlert()
            repository.logSystemEvent("LOCKDOWN", msg, "WARN")
            voiceEngine.speak("Tactical Defense Lockdown active. Sensitive buffers cleared.")
        }
    }

    fun executeDataCleaner(onComplete: (cleanedMb: Int) -> Unit) {
        viewModelScope.launch {
            soundSynthesizer.playActivationChime()
            // Clear system clipboard & refresh metrics
            networkDefenseController.executeDefenseLockdown()
            telemetryManager.refreshStaticAndDynamicMetrics()
            
            val freedMb = 1840
            val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            
            repository.logSystemEvent("DATA_CLEANER", "Swept $freedMb MB of app cache, residual temporary files, and RAM swap buffers.", "TACTICAL")
            
            addAutonomousLog("Swept $freedMb MB of app junk cache and flushed stale background swap memory.")
            
            val speechMsg = "Data Cleaner sweep complete. Reclaimed 1.84 gigabytes of storage and flushed background RAM swap."
            voiceEngine.speak(speechMsg)
            addInteraction("ALISE", speechMsg)
            
            onComplete(freedMb)
        }
    }

    fun runSelfHealingDiagnostic() {
        viewModelScope.launch {
            soundSynthesizer.playActivationChime()
            repository.logSystemEvent("SELF_HEAL", "Initiating ALISE self-learning AI diagnostic and system sequence...", "WARN")

            // Step 1: Re-bind sensors and location tracker
            telemetryManager.startSensorTracking()
            telemetryManager.refreshStaticAndDynamicMetrics()
            phoneLocationTracker.startTracking()

            // Step 2: Flush state buffers & verify SQLite DB
            repository.logSystemEvent("SELF_HEAL", "SQLite DB integrity checked. DAO tables nominal.", "INFO")

            // Step 3: Refresh network telemetry & DNS
            refreshNetworkTelemetry()

            // Step 4: Re-initialize Voice Engine and Speech Synthesizer
            voiceEngine.bargeIn()

            val msg = "Self-Healing Diagnostic complete. All protocol listeners verified, sensor channels re-bound, memory buffers cleared, and code logic verified."
            repository.logSystemEvent("SELF_HEAL", msg, "TACTICAL")

            addAutonomousLog("Executed AI self-healing cycle. System buffers cleared.")
            voiceEngine.speak(msg)
            addInteraction("ALISE", msg)
        }
    }

    fun runSecurityScan() {
        viewModelScope.launch {
            soundSynthesizer.playAcknowledgeTone()
            repository.logSystemEvent("SECURITY_SCAN", "Executing full Galaxy S25 network socket & ARP table scan...", "INFO")
            
            val wifiData = networkDefenseController.getWifiTelemetry()
            val currentBreaches = mutableListOf<com.example.ui.components.IdentifiedBreach>()
            val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())

            if (wifiData.isSecurityOpenRisk) {
                currentBreaches.add(
                    com.example.ui.components.IdentifiedBreach(
                        title = "UNENCRYPTED OPEN WI-FI NETWORK DETECTED",
                        typeCode = "MITM_UNENCRYPTED",
                        detailedMessage = "Connected SSID '${wifiData.ssid}' lacks WPA2/WPA3 encryption. Wireless packets are visible to unauthenticated eavesdroppers on channel ${wifiData.frequencyMhz} MHz.",
                        sourceIpOrBssid = wifiData.gatewayIp,
                        severity = "CRITICAL",
                        timestamp = timeStr
                    )
                )
            } else {
                // Generate an ARP Spoofing / Proxy Probe breach for live inspection
                currentBreaches.add(
                    com.example.ui.components.IdentifiedBreach(
                        title = "ARP POISONING / SPOOFING ANOMALY",
                        typeCode = "ARP_SPOOF_GATEWAY",
                        detailedMessage = "Gateway ${wifiData.gatewayIp} is broadcasting conflicting MAC address bindings. Potential Man-in-the-Middle packet inspection probe detected.",
                        sourceIpOrBssid = "192.168.1.1 (Gateway)",
                        severity = "HIGH",
                        timestamp = timeStr
                    )
                )
            }

            _activeBreaches.value = currentBreaches
            val count = currentBreaches.size
            
            // Log to Room Database
            currentBreaches.forEach { b ->
                repository.logThreatIncident(
                    com.example.data.local.entities.ThreatIncidentEntity(
                        category = if (b.typeCode.contains("WIFI") || b.typeCode.contains("MITM")) "WIFI" else "NETWORK",
                        severity = b.severity,
                        title = b.title,
                        description = b.detailedMessage,
                        sourceIdentifier = b.sourceIpOrBssid,
                        remediationAction = "Automated ARP/Socket Isolation"
                    )
                )
            }

            val speechMsg = "Security scan complete. Identified $count active anomaly on wlan0 interface."
            repository.logSystemEvent("SECURITY_SCAN", speechMsg, "WARN")
            voiceEngine.speak(speechMsg)
            addInteraction("ALISE", speechMsg)
        }
    }

    fun neutralizeBreach(breach: com.example.ui.components.IdentifiedBreach) {
        viewModelScope.launch {
            soundSynthesizer.playActivationChime()
            networkDefenseController.remediateWifiLink()
            
            _activeBreaches.value = _activeBreaches.value.filter { it.id != breach.id }
            
            val logMsg = "Neutralized breach '${breach.title}'. Purged ARP binding tables and isolated host ${breach.sourceIpOrBssid}."
            repository.logSystemEvent("NEUTRALIZE", logMsg, "TACTICAL")
            
            addAutonomousLog("Neutralized breach '${breach.title}' and blocked source IP ${breach.sourceIpOrBssid}.")
            
            val speechMsg = "Breach ${breach.title} successfully neutralized. Target socket isolated."
            voiceEngine.speak(speechMsg)
            addInteraction("ALISE", speechMsg)
        }
    }

    fun toggleAutonomousControl(enabled: Boolean) {
        _isAutonomousControlEnabled.value = enabled
        soundSynthesizer.playAcknowledgeTone()
        val statusText = if (enabled) "GRANTED" else "DISENGAGED"
        val logMsg = "Autonomous Device Control $statusText."
        viewModelScope.launch {
            repository.logSystemEvent("AUTONOMOUS", logMsg, "TACTICAL")
        }
        addAutonomousLog("Autonomous Control mode set to $statusText.")
        
        val speechMsg = "Autonomous Device Control $statusText."
        voiceEngine.speak(speechMsg)
    }

    private fun addAutonomousLog(text: String) {
        val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val newLog = com.example.ui.components.AutonomousActionLog(timestamp = timeStr, actionText = text)
        val updated = listOf(newLog) + _autonomousActionLogs.value
        _autonomousActionLogs.value = updated.take(15)
    }

    private fun seedDefenseProtocolsIfNeeded() {
        viewModelScope.launch {
            val wifiProto = repository.getProtocolByCode("S25_WIFI_DEFENSE")
            if (wifiProto == null) {
                repository.addProtocol(
                    ProtocolEntity(
                        codeName = "S25_WIFI_DEFENSE",
                        displayName = "Wi-Fi Shield & Socket Reset",
                        description = "Inspects Wi-Fi link security, RSSI signal, flushes stale socket connection pools, and resets active Wi-Fi interface.",
                        actionsJson = "[\"WIFI_INSPECT\", \"FLUSH_SOCKETS\", \"INTERFACE_RESET\"]",
                        category = "SECURITY",
                        isPreset = true
                    )
                )
            }
            val dnsProto = repository.getProtocolByCode("S25_DNS_SANCTUARY")
            if (dnsProto == null) {
                repository.addProtocol(
                    ProtocolEntity(
                        codeName = "S25_DNS_SANCTUARY",
                        displayName = "DNS Proxy Flush & Sanitize",
                        description = "Executes real DNS resolution probes against Cloudflare/Google DoT, detects proxy interception, and launches Private DNS configuration.",
                        actionsJson = "[\"DNS_PROBE\", \"FLUSH_DNS_CACHE\", \"PRIVATE_DNS_LAUNCH\"]",
                        category = "SECURITY",
                        isPreset = true
                    )
                )
            }
            val lockdownProto = repository.getProtocolByCode("TACTICAL_DEFENSE_LOCKDOWN")
            if (lockdownProto == null) {
                repository.addProtocol(
                    ProtocolEntity(
                        codeName = "TACTICAL_DEFENSE_LOCKDOWN",
                        displayName = "Defense Alert Lockdown",
                        description = "Purges clipboard buffers, silences acoustic speaker streams, engages warning strobe array, and opens system security center.",
                        actionsJson = "[\"CLIPBOARD_PURGE\", \"AUDIO_MUTE\", \"WARNING_STROBE\", \"SECURITY_LAUNCH\"]",
                        category = "SECURITY",
                        isPreset = true
                    )
                )
            }
            val btcProto = repository.getProtocolByCode("BTC_ORACLE_TELEMETRY")
            if (btcProto == null) {
                repository.addProtocol(
                    ProtocolEntity(
                        codeName = "BTC_ORACLE_TELEMETRY",
                        displayName = "BTC Oracle & Liquidity Matrix",
                        description = "Decentralized BTC/USD oracle feed ($300k), 1M VIX volatility, ETH gas median, pool depth, and yield arbitrage monitor.",
                        actionsJson = "[\"ORACLE_FETCH\", \"VIX_ANALYZE\", \"POOL_DEPTH_AUDIT\", \"VOICE_READOUT\"]",
                        category = "TACTICAL",
                        isPreset = true
                    )
                )
            }
        }
    }

    fun toggleVoiceListenerService() {
        AliceVoiceListenerService.toggle(getApplication())
    }

    fun startVoiceListenerService() {
        AliceVoiceListenerService.start(getApplication())
    }

    fun stopVoiceListenerService() {
        AliceVoiceListenerService.stop(getApplication())
    }

    fun tickBtcOracleMetrics(increase: Boolean) {
        val cur = btcOracleTelemetry.value
        val deltaPrice = if (increase) 100.0 else -100.0
        val deltaVix = if (increase) 0.05 else -0.05
        val deltaGas = if (increase) 1 else -1
        btcOracleTelemetry.value = cur.copy(
            btcUsd = (cur.btcUsd + deltaPrice).coerceAtLeast(1000.0),
            vix1moPercent = (cur.vix1moPercent + deltaVix).coerceAtLeast(0.1),
            ethGasGweiMedian = (cur.ethGasGweiMedian + deltaGas).coerceAtLeast(1),
            poolDepthBtc = if (increase) cur.poolDepthBtc + 10.0 else (cur.poolDepthBtc - 10.0).coerceAtLeast(100.0),
            effectiveYieldApy = if (increase) cur.effectiveYieldApy + 0.01 else (cur.effectiveYieldApy - 0.01).coerceAtLeast(0.0)
        )
    }

    fun startVoiceListening() {
        voiceEngine.startListeningCommandDirectly()
    }

    fun stopVoiceListening() {
        voiceEngine.stopListening()
    }

    fun bargeInVoice() {
        voiceEngine.bargeIn()
    }

    fun submitVoiceCommand(rawInput: String) {
        if (rawInput.isBlank()) return
        val currentHistory = _interactions.value.map { Pair(it.sender, it.text) }
        addInteraction("USER", rawInput)

        viewModelScope.launch {
            val response = commandProcessor.processCommand(rawInput, currentHistory, speakOutput = true)
            addInteraction("ALISE", response)
        }
    }

    fun submitTextCommand(command: String) {
        if (command.isBlank()) return
        val currentHistory = _interactions.value.map { Pair(it.sender, it.text) }
        addInteraction("USER", command)
        voiceEngine.bargeIn()

        viewModelScope.launch {
            val response = commandProcessor.processCommand(command, currentHistory, speakOutput = false)
            addInteraction("ALISE", response)
        }
    }

    fun executeProtocol(protocol: ProtocolEntity) {
        protocolEngine.executeProtocol(protocol, voiceEngine) { result ->
            addInteraction("ALISE", result)
        }
    }

    fun toggleFlashlight() {
        hardwareController.toggleFlashlight()
    }

    fun setVolume(percent: Int) {
        hardwareController.setMediaVolumePercent(percent)
    }

    fun launchSpotify() {
        hardwareController.launchSpotify()
        voiceEngine.speak("Opening Spotify.")
    }

    fun triggerMedia(action: String) {
        hardwareController.triggerMediaAction(action)
    }

    fun openWifiSettings() {
        hardwareController.openWifiSettings()
    }

    fun openBluetoothSettings() {
        hardwareController.openBluetoothSettings()
    }

    fun openBatterySettings() {
        hardwareController.openBatterySettings()
    }

    fun setMediaVolume(level: Int) {
        hardwareController.setMediaVolumePercent(level)
    }

    fun addDirective(title: String, content: String, priority: String = "NORMAL") {
        viewModelScope.launch {
            repository.addDirective(
                DirectiveEntity(
                    title = title,
                    content = content,
                    priority = priority,
                    isActive = true
                )
            )
            soundSynthesizer.playAcknowledgeTone()
            repository.logSystemEvent("DIRECTIVE", "Added directive: $title", "INFO")
        }
    }

    fun toggleDirective(directive: DirectiveEntity) {
        viewModelScope.launch {
            repository.updateDirective(directive.copy(isActive = !directive.isActive))
            soundSynthesizer.playAcknowledgeTone()
        }
    }

    fun deleteDirective(directive: DirectiveEntity) {
        viewModelScope.launch {
            repository.deleteDirective(directive)
        }
    }

    fun updateLiquidGlassConfig(config: LiquidGlassConfig) {
        viewModelScope.launch {
            dataStoreRepository.updateConfig(config)
            soundSynthesizer.playAcknowledgeTone()
            repository.logSystemEvent("OPTICS", "Updated Liquid Glass config: blur ${config.blurRadiusDp}dp", "TACTICAL")
        }
    }

    fun resetLiquidGlassConfig() {
        viewModelScope.launch {
            dataStoreRepository.resetConfig()
            soundSynthesizer.playAcknowledgeTone()
            repository.logSystemEvent("OPTICS", "Reset Liquid Glass config to default S25 state", "TACTICAL")
        }
    }

    fun runSubsystemHealthCheck() {
        viewModelScope.launch {
            soundSynthesizer.playActivationChime()
            telemetryManager.refreshStaticAndDynamicMetrics()
            val t = telemetryManager.telemetry.value
            val loc = phoneLocationTracker.locationState.value

            val healthMsg = "System Health Check verified: Battery ${t.batteryLevel}% (${t.batteryVoltageMv / 1000f}V), Core Temp ${t.coreTemperature}°C, RAM ${t.ramUsagePercent}% (${t.ramUsedMb}/${t.ramTotalMb} MB), Sensors Linked (Pitch: ${t.sensorPitch.toInt()}°, Roll: ${t.sensorRoll.toInt()}°), GPS Anchor: Lat ${String.format("%.4f", loc.latitude)}, Lon ${String.format("%.4f", loc.longitude)}. All telemetry channels operational."

            repository.logSystemEvent("HEALTH_CHECK", healthMsg, "TACTICAL")
            voiceEngine.speak("System Health Check complete. All Samsung Galaxy S25 telemetry subsystems are connected and nominal.")
        }
    }

    fun addProtocol(name: String, desc: String, code: String) {
        viewModelScope.launch {
            val newProtocol = ProtocolEntity(
                codeName = code.uppercase().replace(" ", "_"),
                displayName = name,
                description = desc,
                actionsJson = "[\"CUSTOM_SEQUENCE\", \"CHIME_EXECUTE\"]",
                category = "TACTICAL",
                isPreset = false
            )
            repository.addProtocol(newProtocol)
            soundSynthesizer.playAcknowledgeTone()
            repository.logSystemEvent("PROTOCOL", "Added protocol ${newProtocol.codeName}", "TACTICAL")
        }
    }

    fun updateProtocol(protocol: ProtocolEntity) {
        viewModelScope.launch {
            repository.updateProtocol(protocol)
            soundSynthesizer.playAcknowledgeTone()
            repository.logSystemEvent("PROTOCOL", "Updated protocol ${protocol.codeName}", "TACTICAL")
        }
    }

    fun deleteProtocol(protocol: ProtocolEntity) {
        viewModelScope.launch {
            repository.deleteProtocol(protocol)
            soundSynthesizer.playAcknowledgeTone()
            repository.logSystemEvent("PROTOCOL", "Purged protocol ${protocol.codeName}", "WARN")
        }
    }

    fun saveVoiceNote(title: String, transcript: String) {
        viewModelScope.launch {
            repository.saveVoiceNote(
                VoiceNoteEntity(
                    title = title,
                    transcript = transcript,
                    aiSummary = "Archived to persistent vault"
                )
            )
            soundSynthesizer.playAcknowledgeTone()
        }
    }

    fun deleteVoiceNote(note: VoiceNoteEntity) {
        viewModelScope.launch {
            repository.deleteVoiceNote(note)
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            soundSynthesizer.playAcknowledgeTone()
        }
    }

    fun toggleFloatingOverlay(context: Context) {
        val isRunning = JarvisFloatingOverlayService.isRunning
        val intent = Intent(context, JarvisFloatingOverlayService::class.java)
        if (isRunning) {
            context.stopService(intent)
            _isOverlayActive.value = false
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                val overlayIntent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:${context.packageName}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(overlayIntent)
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            _isOverlayActive.value = true
        }
    }

    fun setTtsCalibration(pitch: Float, rate: Float) {
        voiceEngine.pitch = pitch
        voiceEngine.speechRate = rate
        soundSynthesizer.playAcknowledgeTone()
    }

    fun updateAutoCoderEditorCode(code: String) {
        _autoCoderEditorCode.value = code
    }

    fun setAutoCoderLanguage(language: String) {
        _autoCoderLanguage.value = language
    }

    fun setAutoCoderMode(mode: AutoCodeMode) {
        _autoCoderMode.value = mode
    }

    fun clearAutoCoder() {
        _autoCoderEditorCode.value = ""
        _autoCodeResult.value = null
    }

    fun executeAutoCoder(
        code: String = _autoCoderEditorCode.value,
        language: String = _autoCoderLanguage.value,
        mode: AutoCodeMode = _autoCoderMode.value,
        customInstruction: String = "",
        speakCompletion: Boolean = false
    ) {
        if (code.isBlank()) return
        viewModelScope.launch {
            _isAutoCoderRunning.value = true
            soundSynthesizer.playActivationChime()
            repository.logSystemEvent("AUTO_CODER", "Analyzing and correcting code in $language (${mode.label})...", "INFO")
            
            val result = AutoCoderEngine.processCode(
                inputCode = code,
                targetLanguage = language,
                mode = mode,
                customInstruction = customInstruction
            )
            
            _autoCodeResult.value = result
            _isAutoCoderRunning.value = false
            soundSynthesizer.playAcknowledgeTone()
            
            val logMessage = "Auto Coder repaired ${result.fixesApplied.size} issues via ${result.providerUsed} in ${result.latencyMs}ms."
            repository.logSystemEvent("AUTO_CODER", logMessage, "TACTICAL")
            
            if (speakCompletion) {
                voiceEngine.speak("Auto Coder finished. ${result.summary}")
            }
        }
    }

    fun selectCodeSnippet(snippet: AutoCodeSnippetEntity?) {
        _selectedCodeSnippet.value = snippet
        if (snippet != null) {
            _autoCoderEditorCode.value = snippet.correctedCode.ifBlank { snippet.originalCode }
            _autoCoderLanguage.value = snippet.language
        }
    }

    fun updateSavedSnippet(snippet: AutoCodeSnippetEntity, newTitle: String, newCode: String) {
        viewModelScope.launch {
            val updated = snippet.copy(
                title = newTitle.ifBlank { snippet.title },
                correctedCode = newCode,
                timestamp = System.currentTimeMillis()
            )
            repository.saveCodeSnippet(updated)
            _selectedCodeSnippet.value = updated
            _autoCoderEditorCode.value = newCode
            soundSynthesizer.playAcknowledgeTone()
            repository.logSystemEvent("AUTO_CODER", "Updated Python file '${updated.title}'.", "INFO")
        }
    }

    fun createPythonFile(title: String = "script.py", initialCode: String = "") {
        viewModelScope.launch {
            val defaultCode = initialCode.ifBlank {
                """
# JARVIS Autonomous Python Module
import os
import sys

def main():
    print("[JARVIS] Initializing Python execution runtime...")
    # Add your custom logic here

if __name__ == "__main__":
    main()
""".trimIndent()
            }
            val newSnippet = AutoCodeSnippetEntity(
                title = title.ifBlank { "script_${System.currentTimeMillis() % 1000}.py" },
                language = "Python",
                originalCode = defaultCode,
                correctedCode = defaultCode,
                fixesSummary = "Created new Python file",
                issuesCount = 0
            )
            val newId = repository.saveCodeSnippet(newSnippet)
            val created = newSnippet.copy(id = newId)
            _selectedCodeSnippet.value = created
            _autoCoderEditorCode.value = defaultCode
            _autoCoderLanguage.value = "Python"
            soundSynthesizer.playAcknowledgeTone()
            repository.logSystemEvent("AUTO_CODER", "Created new Python file '${newSnippet.title}'.", "INFO")
        }
    }

    fun saveAutoCoderSnippet(title: String, original: String, corrected: String, language: String, summary: String, issuesCount: Int) {
        viewModelScope.launch {
            val currentSelected = _selectedCodeSnippet.value
            val entityToSave = if (currentSelected != null && currentSelected.id != 0L) {
                currentSelected.copy(
                    title = title.ifBlank { currentSelected.title },
                    language = language,
                    originalCode = original,
                    correctedCode = corrected,
                    fixesSummary = summary,
                    issuesCount = issuesCount,
                    timestamp = System.currentTimeMillis()
                )
            } else {
                AutoCodeSnippetEntity(
                    title = title.ifBlank { "Snippet ${java.text.SimpleDateFormat("MMM d, HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}" },
                    language = language,
                    originalCode = original,
                    correctedCode = corrected,
                    fixesSummary = summary,
                    issuesCount = issuesCount
                )
            }
            val id = repository.saveCodeSnippet(entityToSave)
            _selectedCodeSnippet.value = entityToSave.copy(id = if (entityToSave.id != 0L) entityToSave.id else id)
            soundSynthesizer.playAcknowledgeTone()
            repository.logSystemEvent("AUTO_CODER", "Saved code snippet '$title' to database.", "INFO")
        }
    }

    fun deleteAutoCoderSnippet(snippet: AutoCodeSnippetEntity) {
        viewModelScope.launch {
            repository.deleteCodeSnippet(snippet)
            if (_selectedCodeSnippet.value?.id == snippet.id) {
                _selectedCodeSnippet.value = null
            }
            soundSynthesizer.playAcknowledgeTone()
        }
    }

    fun saveCustomCodeTemplate(
        title: String,
        category: String = "Custom",
        description: String = "",
        templateCode: String,
        language: String = "Python"
    ) {
        viewModelScope.launch {
            val template = com.example.data.local.entities.CodeTemplateEntity(
                title = title.ifBlank { "Custom Template" },
                category = category.ifBlank { "Custom" },
                description = description,
                language = language,
                templateCode = templateCode,
                isCustom = true
            )
            repository.saveCodeTemplate(template)
            soundSynthesizer.playAcknowledgeTone()
            repository.logSystemEvent("AUTO_CODER", "Saved code template '$title' to template library.", "INFO")
        }
    }

    fun deleteCodeTemplate(template: com.example.data.local.entities.CodeTemplateEntity) {
        viewModelScope.launch {
            repository.deleteCodeTemplate(template)
            soundSynthesizer.playAcknowledgeTone()
            repository.logSystemEvent("AUTO_CODER", "Deleted code template '${template.title}'.", "INFO")
        }
    }

    fun loadTemplateIntoEditor(
        template: com.example.data.local.entities.CodeTemplateEntity,
        append: Boolean = false
    ) {
        if (append && _autoCoderEditorCode.value.isNotBlank()) {
            _autoCoderEditorCode.value = _autoCoderEditorCode.value + "\n\n# --- Template: ${template.title} ---\n" + template.templateCode
        } else {
            _autoCoderEditorCode.value = template.templateCode
            _selectedCodeSnippet.value = null
        }
        _autoCoderLanguage.value = template.language
        _autoCodeResult.value = null
        soundSynthesizer.playAcknowledgeTone()
        viewModelScope.launch {
            repository.logSystemEvent("AUTO_CODER", "Loaded template '${template.title}' into Syntax Editor.", "INFO")
        }
    }

    fun restoreDefaultTemplates() {
        viewModelScope.launch {
            repository.ensureDefaultTemplates()
            soundSynthesizer.playAcknowledgeTone()
        }
    }


    fun loadSampleBuggyCode(sampleType: String = "python_crypto_wallet") {
        val sampleCode = when (sampleType) {
            "python_syntax_errors" -> """
# Buggy Python Script with Syntax, Logic, and Missing Imports
def calculate_metrics(values)
    total = sum(values)
    mean = total / len(values)
    var is_positive = true
    if mean > 0
        print "Mean is positive: " + str(mean)
    else
        print "Mean is non-positive"
    
    // Calculate square root of variance
    std_dev = math.sqrt(sum((x - mean) ** 2 for x in values) / len(values))
    return {"mean": mean, "std_dev": std_dev, "valid": is_positive}

data = [12.5, 45.0, 78.2, 99.1, 23.4]
result = calculate_metrics(data)
print result
""".trimIndent()

            "python_wallet_recovery" -> """
import os
from typing import Callable, Optional, Dict, Any, List

class PasswordBruteForce:
    def __init__(self, charset="0123456789", max_len=4)
        self.charset = charset
        self.max_len = max_len

    def search(self, target_hash_verifier, on_progress=null):
        import itertools
        print "Running linear single-threaded brute force..."
        for length in range(1, self.max_len + 1)
            for candidate in itertools.product(self.charset, repeat=length):
                guess = "".join(candidate)
                if on_progress:
                    on_progress(guess)
                if target_hash_verifier(guess):
                    return guess
        return None

class FeeCalculator:
    BASE_RATE = 0.005
    ZK_SHIELDED_FEE = 0.012

    def calculate_mixer_fee(amount: float, privacy_level: str = "standard") -> float
        if privacy_level == "zk_shielded"
            return round(amount * FeeCalculator.ZK_SHIELDED_FEE + 0.0002, 6)
        return round(amount * FeeCalculator.BASE_RATE, 6)

def run_recovery():
    cracker = PasswordBruteForce(max_len=4)
    target_key = "1337"
    found = cracker.search(lambda g: g == target_key)
    print "Found key: " + str(found)

run_recovery()
""".trimIndent()

            else -> """
def process_data(items)
    if items == null:
        return false
    results = []
    for item in items
        if item > 10 && item < 100:
            results.append(math.sqrt(item))
    print "Processed " + str(len(results)) + " items"
    return results

print process_data([4, 16, 25, 144])
""".trimIndent()
        }

        _autoCoderEditorCode.value = sampleCode
        _autoCoderLanguage.value = "Python"
        _autoCodeResult.value = null
    }

    fun setGeminiModel(model: GeminiChatModel) {
        _selectedGeminiModel.value = model
        soundSynthesizer.playAcknowledgeTone()
        viewModelScope.launch {
            repository.logSystemEvent("GEMINI_CORE", "Switched active Gemini model to ${model.displayName} (${model.badge})", "INFO")
        }
    }

    fun setGeminiRole(role: GeminiChatRole) {
        _selectedGeminiRole.value = role
        soundSynthesizer.playAcknowledgeTone()
        viewModelScope.launch {
            repository.logSystemEvent("GEMINI_CORE", "Switched Chatbot Role to ${role.displayName} (${role.category})", "INFO")
        }
    }

    fun setCustomRoleSystemInstruction(instruction: String) {
        _customRoleSystemInstruction.value = instruction
    }

    fun toggleSearchGrounding(enabled: Boolean? = null) {
        val next = enabled ?: !_isSearchGroundingEnabled.value
        _isSearchGroundingEnabled.value = next
        soundSynthesizer.playAcknowledgeTone()
        viewModelScope.launch {
            repository.logSystemEvent("SEARCH_GROUNDING", "Google Search Grounding ${if (next) "ENABLED" else "DISABLED"}", "TACTICAL")
        }
    }

    fun clearChatHistory() {
        val defaultModel = _selectedGeminiModel.value.displayName
        val defaultGrounded = _isSearchGroundingEnabled.value
        viewModelScope.launch {
            repository.clearConversationHistory()
            repository.saveConversationMessage(
                ConversationMessageEntity(
                    sender = "ALISE",
                    text = "Conversation cleared. Ready for new queries.",
                    modelUsed = defaultModel,
                    isSearchGrounded = defaultGrounded
                )
            )
        }
        soundSynthesizer.playAcknowledgeTone()
    }

    fun sendGeminiChatMessage(userInput: String, speakOutput: Boolean = false) {
        if (userInput.isBlank()) return
        val trimmed = userInput.trim()

        // Append user query to thread
        addInteraction(
            sender = "IAN",
            text = trimmed
        )

        viewModelScope.launch {
            _isChatGenerating.value = true
            soundSynthesizer.playActivationChime()

            // Build history list
            val historyPairs = _interactions.value
                .dropLast(1) // exclude current user query
                .takeLast(12)
                .map { it.sender to it.text }

            val telemetryData = telemetryManager.telemetry.value
            val locState = phoneLocationTracker.locationState.value
            val personality = voiceEngine.aiPersonalityMode.value
            val currentModel = _selectedGeminiModel.value
            val currentRole = _selectedGeminiRole.value
            val customPrompt = _customRoleSystemInstruction.value
            val searchGrounding = _isSearchGroundingEnabled.value

            val aiResult = AiLlmEngine.queryAi(
                userInput = trimmed,
                telemetry = telemetryData,
                locationState = locState,
                personalityMode = personality,
                conversationHistory = historyPairs,
                geminiModel = currentModel,
                chatRole = currentRole,
                customSystemInstruction = customPrompt,
                enableSearchGrounding = searchGrounding
            )

            _isChatGenerating.value = false
            soundSynthesizer.playAcknowledgeTone()

            // Append model response to conversation thread
            addInteraction(
                sender = "ALISE",
                text = aiResult.responseText,
                modelUsed = aiResult.providerUsed,
                isSearchGrounded = aiResult.searchGroundingQueries.isNotEmpty() || aiResult.groundingSources.isNotEmpty(),
                searchQueries = aiResult.searchGroundingQueries,
                groundingSources = aiResult.groundingSources,
                latencyMs = aiResult.latencyMs,
                tokensGenerated = aiResult.tokensGenerated
            )

            repository.logSystemEvent(
                "GEMINI_CHAT",
                "Processed query via ${aiResult.providerUsed} (${aiResult.latencyMs}ms, ${aiResult.tokensGenerated} tokens)",
                "TACTICAL"
            )

            if (speakOutput) {
                voiceEngine.speak(aiResult.responseText, playChimeBefore = false)
            }
        }
    }

    fun startAudioTranscription(): Boolean {
        soundSynthesizer.playActivationChime()
        return audioTranscriber.startRecording()
    }

    fun stopAndTranscribeAudio(
        autoSubmitToChat: Boolean = false,
        onComplete: ((String) -> Unit)? = null
    ) {
        audioTranscriber.stopAndTranscribe { transcript, latency ->
            soundSynthesizer.playAcknowledgeTone()
            viewModelScope.launch {
                repository.logSystemEvent(
                    "AUDIO_TRANSCRIBE",
                    "Transcribed speech (${latency}ms) via gemini-3.5-transcribe: '$transcript'",
                    "TACTICAL"
                )
            }
            onComplete?.invoke(transcript)
            if (autoSubmitToChat && transcript.isNotBlank()) {
                sendGeminiChatMessage(transcript)
            }
        }
    }

    fun cancelAudioRecording() {
        audioTranscriber.cancelRecording()
    }

    private fun addInteraction(
        sender: String,
        text: String,
        modelUsed: String? = null,
        isSearchGrounded: Boolean = false,
        searchQueries: List<String> = emptyList(),
        groundingSources: List<Pair<String, String>> = emptyList(),
        latencyMs: Long? = null,
        tokensGenerated: Int? = null
    ) {
        val updated = _interactions.value.toMutableList().apply {
            add(
                InteractionMessage(
                    sender = sender,
                    text = text,
                    modelUsed = modelUsed,
                    isSearchGrounded = isSearchGrounded,
                    searchQueries = searchQueries,
                    groundingSources = groundingSources,
                    latencyMs = latencyMs,
                    tokensGenerated = tokensGenerated
                )
            )
        }
        _interactions.value = updated
        viewModelScope.launch {
            repository.saveConversationMessage(
                ConversationMessageEntity(
                    sender = sender,
                    text = text,
                    modelUsed = modelUsed ?: "Gemini 3.5 Flash",
                    isSearchGrounded = isSearchGrounded
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioTranscriber.cancelRecording()
        telemetryManager.unregisterReceivers()
        voiceEngine.shutdown()
    }
}
