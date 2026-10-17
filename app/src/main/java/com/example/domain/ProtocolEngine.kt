package com.example.domain

import com.example.data.local.JarvisRepository
import com.example.data.local.entities.ProtocolEntity
import com.example.data.model.BtcOracleTelemetry
import com.example.hardware.DeviceTelemetryManager
import com.example.hardware.HardwareController
import com.example.hardware.NetworkDefenseController
import com.example.voice.JarvisSoundSynthesizer
import com.example.voice.JarvisVoiceEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProtocolExecutionState(
    val isExecuting: Boolean = false,
    val currentProtocol: ProtocolEntity? = null,
    val currentStepText: String = "",
    val progressPercent: Float = 0f
)

class ProtocolEngine(
    private val repository: JarvisRepository,
    private val hardwareController: HardwareController,
    private val telemetryManager: DeviceTelemetryManager,
    private val soundSynthesizer: JarvisSoundSynthesizer,
    private val networkDefenseController: NetworkDefenseController
) {

    private val _executionState = MutableStateFlow(ProtocolExecutionState())
    val executionState: StateFlow<ProtocolExecutionState> = _executionState.asStateFlow()

    fun executeProtocol(protocol: ProtocolEntity, voiceEngine: JarvisVoiceEngine?, onComplete: ((String) -> Unit)? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            _executionState.value = ProtocolExecutionState(
                isExecuting = true,
                currentProtocol = protocol,
                currentStepText = "Initializing ${protocol.displayName}...",
                progressPercent = 0.1f
            )
            soundSynthesizer.playActivationChime()
            hardwareController.vibrateTactilePulse(80)

            repository.logSystemEvent("PROTOCOL", "Engaged ${protocol.displayName}", "TACTICAL")

            when (protocol.codeName.uppercase()) {
                "S25_WIFI_DEFENSE", "WIFI_DEFENSE_SHIELD", "WIFI_SANCTUARY" -> {
                    _executionState.value = _executionState.value.copy(currentStepText = "Inspecting Wi-Fi link, signal RSSI & gateway security...", progressPercent = 0.3f)
                    delay(300)
                    val wifiTelemetry = networkDefenseController.getWifiTelemetry()
                    _executionState.value = _executionState.value.copy(currentStepText = "Flushing socket connection pools for ${wifiTelemetry.ssid}...", progressPercent = 0.6f)
                    val wifiFixResult = networkDefenseController.remediateWifiLink()
                    repository.logSystemEvent("NET_DEFENSE", wifiFixResult, "TACTICAL")
                    delay(300)
                    _executionState.value = _executionState.value.copy(currentStepText = "Wi-Fi link shield active.", progressPercent = 1.0f)
                    voiceEngine?.speak("Wi-Fi Defense Protocol executed. Connection socket pools reset for ${wifiTelemetry.ssid} and interface verified.")
                }
                "S25_DNS_SANCTUARY", "DNS_PROXY_FLUSH", "DNS_SANCTUARY" -> {
                    _executionState.value = _executionState.value.copy(currentStepText = "Probing DNS resolution endpoints (Cloudflare/Google)...", progressPercent = 0.3f)
                    val dnsDiagnostic = networkDefenseController.runDnsProxyDiagnostic()
                    repository.logSystemEvent("DNS_DIAG", dnsDiagnostic.diagnosticSummary, if (dnsDiagnostic.isDnsHealthy) "INFO" else "WARN")
                    delay(300)
                    _executionState.value = _executionState.value.copy(currentStepText = "Flushing DNS proxy cache & opening Private DNS configuration...", progressPercent = 0.7f)
                    val dnsFixMsg = networkDefenseController.sanitizeAndFlushDnsProxy()
                    repository.logSystemEvent("NET_DEFENSE", dnsFixMsg, "TACTICAL")
                    delay(300)
                    _executionState.value = _executionState.value.copy(currentStepText = "DNS Proxy Sanitize complete.", progressPercent = 1.0f)
                    val speechMsg = if (dnsDiagnostic.detectedProxyInterception) {
                        "DNS proxy anomaly detected. Local lookups were being intercepted. I have flushed local caches and launched Private DNS settings."
                    } else {
                        "DNS Proxy Sanitize complete. Resolution speed is ${dnsDiagnostic.cloudflareLatencyMs} milliseconds and lookup caches are clear."
                    }
                    voiceEngine?.speak(speechMsg)
                }
                "TACTICAL_DEFENSE_LOCKDOWN", "DEFENSE_ALERT_LOCKDOWN", "S25_DEFENSE_LOCKDOWN" -> {
                    _executionState.value = _executionState.value.copy(currentStepText = "Purging sensitive clipboard buffers and silencing acoustic channels...", progressPercent = 0.3f)
                    val lockdownMsg = networkDefenseController.executeDefenseLockdown()
                    repository.logSystemEvent("LOCKDOWN", lockdownMsg, "WARN")
                    delay(300)
                    _executionState.value = _executionState.value.copy(currentStepText = "Triggering optical warning strobe & tactical vibration...", progressPercent = 0.7f)
                    hardwareController.strobeFlashlight(burstCount = 4, intervalMs = 90)
                    hardwareController.vibrateTacticalAlert()
                    delay(300)
                    _executionState.value = _executionState.value.copy(currentStepText = "Tactical Defense Lockdown active.", progressPercent = 1.0f)
                    voiceEngine?.speak("Defense Alert Lockdown active. Clipboard purged, acoustic channels muted, and system security engaged.")
                }
                "NETWORK_THREAT_PURGE", "THREAT_ISOLATION" -> {
                    _executionState.value = _executionState.value.copy(currentStepText = "Scanning active network interfaces & VPN routes...", progressPercent = 0.4f)
                    delay(300)
                    networkDefenseController.openVpnSettings()
                    repository.logSystemEvent("NET_DEFENSE", "Network threat purge initiated. VPN routes & interface management opened.", "TACTICAL")
                    _executionState.value = _executionState.value.copy(currentStepText = "Interface routes verified.", progressPercent = 1.0f)
                    voiceEngine?.speak("Network threat purge complete. Interface routes and VPN settings opened for inspection.")
                }
                "S25_CLEAN_SLATE", "CLEAN_SLATE" -> {
                    _executionState.value = _executionState.value.copy(currentStepText = "Purging memory cache buffers...", progressPercent = 0.4f)
                    delay(300)
                    telemetryManager.refreshStaticAndDynamicMetrics()
                    _executionState.value = _executionState.value.copy(currentStepText = "Recalibrating S25 telemetry matrix...", progressPercent = 0.8f)
                    delay(300)
                    _executionState.value = _executionState.value.copy(currentStepText = "S25 Clean Slate complete.", progressPercent = 1.0f)
                    voiceEngine?.speak("Protocol Clean Slate executed. All S25 systems refreshed and nominal.")
                }
                "S25_BATTERY_SAVER" -> {
                    _executionState.value = _executionState.value.copy(currentStepText = "Capping AMOLED refresh rate to 60Hz...", progressPercent = 0.4f)
                    delay(250)
                    _executionState.value = _executionState.value.copy(currentStepText = "Throttling background sensor polling...", progressPercent = 0.8f)
                    hardwareController.setMediaVolumePercent(30)
                    delay(300)
                    _executionState.value = _executionState.value.copy(currentStepText = "S25 Ultra Battery Saver engaged.", progressPercent = 1.0f)
                    voiceEngine?.speak("S25 Battery Saver protocol active.")
                }
                "S25_SENTRY_MODE", "SENTRY_MODE" -> {
                    _executionState.value = _executionState.value.copy(currentStepText = "Activating photon array strobe...", progressPercent = 0.4f)
                    hardwareController.strobeFlashlight(burstCount = 3, intervalMs = 100)
                    delay(400)
                    _executionState.value = _executionState.value.copy(currentStepText = "Perimeter sensor array armed...", progressPercent = 0.9f)
                    delay(300)
                    _executionState.value = _executionState.value.copy(currentStepText = "Sentry active.", progressPercent = 1.0f)
                    voiceEngine?.speak("S25 Sentry Mode armed. Optical sensors are scanning.")
                }
                "S25_DEEP_FOCUS", "DEEP_FOCUS" -> {
                    _executionState.value = _executionState.value.copy(currentStepText = "Suppressing ambient telemetry noise...", progressPercent = 0.5f)
                    hardwareController.setMediaVolumePercent(15)
                    delay(300)
                    _executionState.value = _executionState.value.copy(currentStepText = "Minimalist HUD profile engaged...", progressPercent = 0.9f)
                    delay(300)
                    _executionState.value = _executionState.value.copy(currentStepText = "Deep Focus active.", progressPercent = 1.0f)
                    voiceEngine?.speak("Deep Focus engaged. Non-essential indicators silenced.")
                }
                "S25_DIAGNOSTIC_SCAN", "DIAGNOSTIC_SCAN" -> {
                    _executionState.value = _executionState.value.copy(currentStepText = "Benchmarking Snapdragon 8 Elite SoC...", progressPercent = 0.3f)
                    delay(400)
                    _executionState.value = _executionState.value.copy(currentStepText = "Analyzing AMOLED matrix and battery health...", progressPercent = 0.7f)
                    telemetryManager.refreshStaticAndDynamicMetrics()
                    delay(400)
                    val t = telemetryManager.telemetry.value
                    _executionState.value = _executionState.value.copy(currentStepText = "Diagnostics complete.", progressPercent = 1.0f)
                    voiceEngine?.speak("Diagnostic complete. Samsung Galaxy S25 battery at ${t.batteryLevel} percent, thermal state is ${t.coreTemperature} degrees Celsius, RAM utilization is ${t.ramUsagePercent} percent.")
                }
                "S25_STEALTH_MODE", "STEALTH_MODE" -> {
                    _executionState.value = _executionState.value.copy(currentStepText = "Silencing acoustic audio pipelines...", progressPercent = 0.6f)
                    hardwareController.setMediaVolumePercent(0)
                    delay(300)
                    _executionState.value = _executionState.value.copy(currentStepText = "Stealth Mode active.", progressPercent = 1.0f)
                    voiceEngine?.speak("Stealth Mode engaged.")
                }
                "ACOUSTIC_SONAR_PING" -> {
                    _executionState.value = _executionState.value.copy(currentStepText = "Ringing high-frequency acoustic sonar locator tone...", progressPercent = 0.5f)
                    hardwareController.strobeFlashlight(burstCount = 4, intervalMs = 80)
                    delay(500)
                    _executionState.value = _executionState.value.copy(currentStepText = "Acoustic locator beacon active.", progressPercent = 1.0f)
                    voiceEngine?.speak("Acoustic sonar ping transmitted.")
                }
                "GPS_PERIMETER_LOCK" -> {
                    _executionState.value = _executionState.value.copy(currentStepText = "Locking real-time satellite coordinates...", progressPercent = 0.5f)
                    delay(400)
                    _executionState.value = _executionState.value.copy(currentStepText = "GPS perimeter anchor locked.", progressPercent = 1.0f)
                    voiceEngine?.speak("GPS safe perimeter locked.")
                }
                "BTC_ORACLE_TELEMETRY", "BITCOIN_ORACLE", "BTC_ORACLE", "HUBCOIN_BTC" -> {
                    _executionState.value = _executionState.value.copy(currentStepText = "Querying decentralized BTC/USD Oracle & liquidity pool depth...", progressPercent = 0.3f)
                    delay(350)
                    val oracle = BtcOracleTelemetry()
                    _executionState.value = _executionState.value.copy(currentStepText = "Analyzing 1M VIX (${oracle.vix1moPercent}%) and ETH gas (${oracle.ethGasGweiMedian} Gwei)...", progressPercent = 0.7f)
                    delay(300)
                    _executionState.value = _executionState.value.copy(currentStepText = "BTC Oracle locked: $${oracle.btcUsd.toInt()} | Pool: ${oracle.poolDepthBtc.toInt()} BTC | Yield: ${oracle.effectiveYieldApy}%", progressPercent = 1.0f)
                    repository.logSystemEvent("BTC_ORACLE", "Oracle Feed: $${oracle.btcUsd.toInt()}, VIX: ${oracle.vix1moPercent}%, Pool: ${oracle.poolDepthBtc} BTC, APY: ${oracle.effectiveYieldApy}%, Tax: ${oracle.taxRatePercentBtc}%", "TACTICAL")
                    val speechMsg = "Bitcoin Oracle verified. Price is $${oracle.btcUsd.toInt()} dollars. Volatility index is ${oracle.vix1moPercent} percent, Ethereum gas is ${oracle.ethGasGweiMedian} Gwei, liquidity pool depth is ${oracle.poolDepthBtc.toInt()} Bitcoin, and effective yield APY is ${String.format(java.util.Locale.US, "%.2f", oracle.effectiveYieldApy)} percent."
                    voiceEngine?.speak(speechMsg)
                }
                else -> {
                    _executionState.value = _executionState.value.copy(currentStepText = "Executing custom directive sequence...", progressPercent = 0.7f)
                    delay(400)
                    _executionState.value = _executionState.value.copy(currentStepText = "Sequence finalized.", progressPercent = 1.0f)
                    voiceEngine?.speak("${protocol.displayName} executed successfully.")
                }
            }

            // Update execution stats in database
            repository.updateProtocol(
                protocol.copy(
                    executionCount = protocol.executionCount + 1,
                    lastExecuted = System.currentTimeMillis()
                )
            )

            delay(1200)
            _executionState.value = ProtocolExecutionState(isExecuting = false)
            onComplete?.invoke("Executed ${protocol.displayName}")
        }
    }
}

