package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.ui.components.glassmorphicSurface
import com.example.ui.components.liquidGlassSurface
import com.example.ui.theme.AlienwareWhiteFrosted
import com.example.ui.theme.AlienwareWhiteIce
import com.example.ui.theme.AlienwareWhitePure
import com.example.ui.theme.JarvisAlertRed
import com.example.ui.theme.JarvisArcGold
import com.example.ui.theme.JarvisBorderActive
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisSuccessGreen
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.JarvisVoid
import com.example.ui.theme.LightBlueCorrection
import com.example.ui.theme.LightBlueGlow
import com.example.ui.theme.LiquidCoolantCyan
import com.example.ui.theme.LiquidCoolantFlow
import com.example.ui.theme.Typography
import com.example.ui.viewmodel.JarvisViewModel
import com.example.util.JarvisBiometricAuthManager

data class ProtocolDefinition(
    val id: Int,
    val codeName: String,
    val name: String,
    val category: String, // CLEAN, ORGANIZE, AUTOMATE, ANALYZE, DEFENSE
    val description: String,
    val defaultEnabled: Boolean = true,
    val requiresBiometric: Boolean = false,
    val paramLabel: String = "INTENSITY %",
    val defaultParamValue: Float = 75f
)

// List of 100 Live Protocols
val QUANTUM_100_PROTOCOLS = buildList {
    // 1-20: System Clean & Memory Optimization
    add(ProtocolDefinition(1, "PURGE_CACHE", "System Junk Cache Sweeper", "CLEAN", "Scans and purges system app cache, temporary file fragments, and stale logs.", paramLabel = "Purge Depth %", defaultParamValue = 85f))
    add(ProtocolDefinition(2, "RAM_DEFRAG", "Dynamic RAM Defragmenter", "CLEAN", "Defragments system RAM, releases unreferenced heap memory, and frees inactive buffers.", paramLabel = "Memory Target %", defaultParamValue = 90f))
    add(ProtocolDefinition(3, "THERMAL_FLUSH", "Thermal Heat Sink Flush", "CLEAN", "Throttles background CPU loops to cool down processor cores and prevent thermal degradation.", paramLabel = "Cooling Power %", defaultParamValue = 80f))
    add(ProtocolDefinition(4, "APK_SWEEPER", "Residual APK Installer Cleaner", "CLEAN", "Identifies leftover .apk installer files, partial downloads, and orphaned packages.", paramLabel = "Min Age (Days)", defaultParamValue = 7f))
    add(ProtocolDefinition(5, "SWAP_PURGE", "ZRAM Swap Space Equalizer", "CLEAN", "Clears swap memory space and redistributes virtual memory pages.", paramLabel = "Threshold %", defaultParamValue = 70f))
    add(ProtocolDefinition(6, "MEDIA_CACHE_TRIM", "Gallery & Media Thumbnail Trimmer", "CLEAN", "Trims duplicate image thumbnail caches and video render previews.", paramLabel = "Cache Max (MB)", defaultParamValue = 500f))
    add(ProtocolDefinition(7, "DOWNLOAD_FLUSH", "Stale Download Directory Collector", "CLEAN", "Archives or deletes old unreferenced files in the system Download directory.", paramLabel = "Retention Days", defaultParamValue = 14f))
    add(ProtocolDefinition(8, "CLIPBOARD_SANITIZER", "Clipboard Data Sanitizer", "CLEAN", "Wipes sensitive passwords, credit card clips, and auth tokens from system clipboard.", paramLabel = "Auto-Wipe Min", defaultParamValue = 5f))
    add(ProtocolDefinition(9, "LOG_VACUUM", "Android System Log Vacuum", "CLEAN", "Purges system logcat buffers and crash dump trace files.", paramLabel = "Log Buffer MB", defaultParamValue = 100f))
    add(ProtocolDefinition(10, "BACKGROUND_FREEZE", "Rogue App Background Freezer", "CLEAN", "Freezes non-essential background processes consuming idle CPU cycles.", paramLabel = "Aggressiveness %", defaultParamValue = 80f))
    add(ProtocolDefinition(11, "FONT_CACHE_RESET", "Typography & Render Cache Cleaner", "CLEAN", "Clears GPU text texture maps and font rendering memory caches.", paramLabel = "Texture Cap", defaultParamValue = 64f))
    add(ProtocolDefinition(12, "NOTIFICATION_SWEEP", "Stale Notification History Sweeper", "CLEAN", "Purges dismissible notification records older than configured hours.", paramLabel = "Age Hours", defaultParamValue = 24f))
    add(ProtocolDefinition(13, "BLUETOOTH_CACHE_WIPE", "Bluetooth Pairing Cache Sanitizer", "CLEAN", "Flushes orphaned Bluetooth GATT socket caches and stale device handshakes.", paramLabel = "Scan Timeout", defaultParamValue = 30f))
    add(ProtocolDefinition(14, "DNS_RESOLVER_FLUSH", "Local DNS Resolver Cache Flush", "CLEAN", "Flushes socket DNS resolve table to accelerate network domain resolution.", paramLabel = "Flush Interval (m)", defaultParamValue = 60f))
    add(ProtocolDefinition(15, "PACKAGE_TRASH_EMPTY", "Uninstalled App Trash Bin Purge", "CLEAN", "Empties hidden system trash bins left over by uninstalled packages.", paramLabel = "Quota MB", defaultParamValue = 250f))
    add(ProtocolDefinition(16, "STORAGE_TRIM", "FSTRIM Storage I/O Flash Optimizer", "CLEAN", "Executes flash memory trim command to maintain high SSD/UFS read speeds.", paramLabel = "Trim Speed %", defaultParamValue = 100f))
    add(ProtocolDefinition(17, "BATTERY_DRAIN_KILLER", "Wakelock & Sensor Drain Suppressor", "CLEAN", "Terminates rogue background wakelocks keeping device CPU awake.", paramLabel = "Timeout Sec", defaultParamValue = 15f))
    add(ProtocolDefinition(18, "WEBVIEW_DATA_CLEAN", "WebView Cookie & Cache Purger", "CLEAN", "Wipes in-app browser WebView caches, temporary HTML5 storage, and tracking cookies.", paramLabel = "Wipe Level", defaultParamValue = 2f))
    add(ProtocolDefinition(19, "AUDIO_BUFFER_FLUSH", "DSP Audio Stream Buffer Reset", "CLEAN", "Resets digital audio signal processor output buffers to eliminate audio stutter.", paramLabel = "Buffer Size ms", defaultParamValue = 120f))
    add(ProtocolDefinition(20, "CAMERA_RESOURCE_RELEASE", "Camera HAL Hardware Release", "CLEAN", "Force-releases locked camera sensor hardware handles from inactive background apps.", paramLabel = "Idle Wait Sec", defaultParamValue = 10f))

    // 21-40: Smart System Organization
    add(ProtocolDefinition(21, "CONTACT_DEDUP", "Smart Contact Deduplicator", "CLEAN", "Scans contact database, merges identical entries, and resolves duplicate phone numbers.", paramLabel = "Match Threshold %", defaultParamValue = 90f))
    add(ProtocolDefinition(22, "GALLERY_AUTO_SORT", "AI Photo Category Organizer", "ORGANIZE", "Tags photo library into Documents, Screenshots, Receipts, and Media.", paramLabel = "Confidence %", defaultParamValue = 85f))
    add(ProtocolDefinition(23, "FILE_TAG_INDEXER", "Deep System File Tagging Indexer", "ORGANIZE", "Indexes internal storage files by content metadata, hash, and extension.", paramLabel = "Index Speed MB/s", defaultParamValue = 50f))
    add(ProtocolDefinition(24, "APP_DRAWER_GROUP", "Smart App Folder Categorizer", "ORGANIZE", "Groups installed applications into Productivity, Games, System, and Social.", paramLabel = "Group Precision", defaultParamValue = 95f))
    add(ProtocolDefinition(25, "DUPLICATE_MEDIA_FINDER", "MD5 Hash Duplicate File Resolver", "ORGANIZE", "Scans storage for identical media files using exact MD5 checksum matching.", paramLabel = "Scan Depth", defaultParamValue = 100f))
    add(ProtocolDefinition(26, "SCREENSHOT_CLEANER", "Old Screenshot Auto-Archiver", "ORGANIZE", "Identifies read/used screenshots and prompts or archives them safely.", paramLabel = "Age Days", defaultParamValue = 30f))
    add(ProtocolDefinition(27, "CALL_LOG_ORGANIZER", "Call History Tag & Spam Sorter", "ORGANIZE", "Tags incoming missed calls, contacts, and suspect spam caller logs.", paramLabel = "Spam Filter %", defaultParamValue = 80f))
    add(ProtocolDefinition(28, "SMS_OTP_AUTO_DELETE", "Expired SMS OTP Code Cleaner", "ORGANIZE", "Detects temporary two-factor SMS texts and auto-purges expired OTP codes.", paramLabel = "Expiry Hours", defaultParamValue = 2f))
    add(ProtocolDefinition(29, "CALENDAR_SYNC_INDEX", "Intelligent Calendar Event Indexer", "ORGANIZE", "Harmonizes work, personal, and system task events into a single timeline.", paramLabel = "Sync Range Days", defaultParamValue = 90f))
    add(ProtocolDefinition(30, "MUSIC_TAG_CORRECTOR", "Local Audio ID3 Tag Repairer", "ORGANIZE", "Fetches track titles, album art, and artist names for untagged audio files.", paramLabel = "Match Strictness", defaultParamValue = 85f))
    add(ProtocolDefinition(31, "BOOKMARK_SYNCHRONIZER", "Cross-Browser Bookmark Harmonizer", "ORGANIZE", "Deduplicates browser bookmarks and categorizes saved Web URLs.", paramLabel = "Folder Max", defaultParamValue = 20f))
    add(ProtocolDefinition(32, "NOTE_TAG_CATEGORIZER", "Jarvis Thought & Note Classifier", "ORGANIZE", "Auto-sorts quick voice notes and terminal logs into actionable project folders.", paramLabel = "Cluster Count", defaultParamValue = 8f))
    add(ProtocolDefinition(33, "ZIP_ARCHIVE_COMPRESSOR", "Idle Document Compression Engine", "ORGANIZE", "Compresses rarely accessed document files into encrypted .zip archives.", paramLabel = "Compression Ratio", defaultParamValue = 7f))
    add(ProtocolDefinition(34, "CLOUD_DRIVE_INDEXER", "Cloud Storage Metadata Indexer", "ORGANIZE", "Builds a unified search index across local phone storage and linked drive accounts.", paramLabel = "Sync Frequency (h)", defaultParamValue = 12f))
    add(ProtocolDefinition(35, "APKS_BACKUP_ARCHIVER", "Installed Application Backup S25", "ORGANIZE", "Creates local offline APK backup packages for critical installed tools.", paramLabel = "Compression %", defaultParamValue = 80f))
    add(ProtocolDefinition(36, "WALLPAPER_SCHEDULER", "Dynamic Sci-Fi Wall Schedule", "ORGANIZE", "Rotates high-tech cyber wallpapers based on time of day and battery level.", paramLabel = "Interval Hours", defaultParamValue = 6f))
    add(ProtocolDefinition(37, "RINGTONE_PROFILE_ALIGN", "Contextual Audio Profile Manager", "ORGANIZE", "Adjusts ringtones and notification tones based on location and meeting calendar.", paramLabel = "Sensitivity", defaultParamValue = 75f))
    add(ProtocolDefinition(38, "STORAGE_MAP_GENERATOR", "Visual Storage Breakdown Graph", "ORGANIZE", "Constructs detailed visual tree map of disk space used by apps, media, and system.", paramLabel = "Detail Level", defaultParamValue = 5f))
    add(ProtocolDefinition(39, "FONT_STYLE_ORGANIZER", "Custom System Font Profile Manager", "ORGANIZE", "Manages Sci-Fi display font profiles and text scaling for S25 120Hz display.", paramLabel = "Scale %", defaultParamValue = 100f))
    add(ProtocolDefinition(40, "SHORTCUT_GRID_ALIGN", "Home Screen Widget Quick Grid", "ORGANIZE", "Auto-arranges system widget tiles and shortcuts for maximum ergonomic reach.", paramLabel = "Grid Density", defaultParamValue = 4f))

    // 41-60: Autonomous Task Completion & Automation
    add(ProtocolDefinition(41, "NIGHT_MUTE_AUTOMATION", "Circadian Night Mute Protocol", "AUTOMATE", "Silences non-priority callers, dims display brightness, and disables LED vibration at night.", paramLabel = "Start Hour", defaultParamValue = 23f))
    add(ProtocolDefinition(42, "SMART_BATTERY_SAVER", "Autonomous Battery Guard (15%)", "AUTOMATE", "Engages ultra power saving mode when device battery hits threshold.", paramLabel = "Trigger %", defaultParamValue = 20f))
    add(ProtocolDefinition(43, "UNKNOWN_WIFI_LOCKDOWN", "Public Wi-Fi Auto VPN & Shield", "AUTOMATE", "Disables auto-connect on open Wi-Fi networks and activates local firewall encryption.", paramLabel = "Security Level", defaultParamValue = 90f))
    add(ProtocolDefinition(44, "SPAM_CALL_SUPPRESSOR", "Real-Time Spam Call Interceptor", "AUTOMATE", "Automatically hangs up or silences robocalls and unverified caller numbers.", paramLabel = "Aggressive %", defaultParamValue = 85f))
    add(ProtocolDefinition(45, "MEETING_AUTO_RESPOND", "Calendar Meeting SMS Auto-Reply", "AUTOMATE", "Sends custom JARVIS automated status text when calls arrive during scheduled meetings.", paramLabel = "Delay Sec", defaultParamValue = 5f))
    add(ProtocolDefinition(46, "CHARGING_FULL_ALERT", "Battery Overcharge Alarm & Cutoff", "AUTOMATE", "Notifies user when battery reaches target charge and stops high-rate power pull.", paramLabel = "Target Charge %", defaultParamValue = 85f))
    add(ProtocolDefinition(47, "DRIVING_MODE_AUTOMATION", "Bluetooth Car Dock Auto-Launch", "AUTOMATE", "Switches HUD to simplified voice interface upon connecting to vehicle Bluetooth.", paramLabel = "Speed Threshold km/h", defaultParamValue = 15f))
    add(ProtocolDefinition(48, "HEADPHONES_PLUG_ACTION", "Audio Jack / Bluetooth Media Launch", "AUTOMATE", "Auto-resumes preferred audio playlist when headphones pair.", paramLabel = "Volume Cap %", defaultParamValue = 70f))
    add(ProtocolDefinition(49, "LOW_STORAGE_WARNING", "Critical Storage Space Auto-Clean", "AUTOMATE", "Trigger automatic temp cache purge if free storage drops below target.", paramLabel = "Min Free GB", defaultParamValue = 5f))
    add(ProtocolDefinition(50, "LOCATION_AIRPLANE_TOGGLE", "Geofenced High Speed Flight Mode", "AUTOMATE", "Optionally toggles network radios at designated flight departure locations.", paramLabel = "Radius Meters", defaultParamValue = 500f))
    add(ProtocolDefinition(51, "AUTO_SYNC_SCHEDULER", "Adaptive Cloud Sync Frequency", "AUTOMATE", "Throttles background cloud sync tasks to Wi-Fi connection times.", paramLabel = "Battery Min %", defaultParamValue = 30f))
    add(ProtocolDefinition(52, "INTELLIGENT_VOL_GUARD", "Acoustic Ear Health Volume Cap", "AUTOMATE", "Prevents sudden high-decibel audio spikes from damaging hearing when wearing buds.", paramLabel = "Max dB Cap", defaultParamValue = 85f))
    add(ProtocolDefinition(53, "HOTSPOT_AUTO_DISCONNECT", "Idle Tethering Auto Timeout", "AUTOMATE", "Turns off Wi-Fi Hotspot when no client devices are connected.", paramLabel = "Timeout Min", defaultParamValue = 10f))
    add(ProtocolDefinition(54, "DARK_MODE_SUNSET", "Astronomical Dark Theme Switcher", "AUTOMATE", "Toggles system and app dark mode based on real-time sunset data.", paramLabel = "Offset Min", defaultParamValue = 0f))
    add(ProtocolDefinition(55, "AUTO_BACKUP_SNAPSHOT", "Daily Database & Vault Snapshot", "AUTOMATE", "Takes encrypted snapshot of user notes, settings, and protocols daily at 03:00.", paramLabel = "Snapshot Keep Count", defaultParamValue = 7f))
    add(ProtocolDefinition(56, "APP_UPDATE_AUTO_DEFER", "Cellular Metered Data Saver", "AUTOMATE", "Defers app play store updates when connected to metered cellular data.", paramLabel = "Data Cap MB", defaultParamValue = 100f))
    add(ProtocolDefinition(57, "DISPLAY_REFRESH_ADAPT", "Variable 120Hz Refresh Rate Guard", "AUTOMATE", "Drops refresh rate to 60Hz during static text reading to conserve battery.", paramLabel = "Idle Wait Sec", defaultParamValue = 3f))
    add(ProtocolDefinition(58, "VOICE_RECOGNITION_BOOST", "Noise Cancellation Mic Enhancer", "AUTOMATE", "Adjusts microphone gain and DSP filters when ambient noise increases.", paramLabel = "Gain Boost dB", defaultParamValue = 6f))
    add(ProtocolDefinition(59, "EMERGENCY_SOS_BEACON", "Hardware Key SOS Location Broadcast", "AUTOMATE", "Broadcasts encrypted location beacon to trusted contact if power button pressed 5x.", paramLabel = "Press Count", defaultParamValue = 5f))
    add(ProtocolDefinition(60, "INTELLIGENT_FOCUS_MODE", "DND Work Focus Session Automator", "AUTOMATE", "Blocks social apps and distracting notifications during active coding sessions.", paramLabel = "Session Duration (m)", defaultParamValue = 45f))

    // 61-80: System Analysis & Diagnostics
    add(ProtocolDefinition(61, "PORT_SCANNER", "Network Socket Port Scanner", "ANALYZE", "Scans active IP connections, listening ports, and identifies open socket risks.", paramLabel = "Max Port Range", defaultParamValue = 1024f))
    add(ProtocolDefinition(62, "BATTERY_HEALTH_PROBE", "Lithium Ion Cell Battery Diagnostic", "ANALYZE", "Calculates battery degradation, cycle count, charge rate, and internal resistance.", paramLabel = "Accuracy Samples", defaultParamValue = 50f))
    add(ProtocolDefinition(63, "STORAGE_IO_BENCHMARK", "Flash Disk Read / Write Speed Benchmark", "ANALYZE", "Measures sequential and random I/O throughput speed in MB/s.", paramLabel = "Block Size KB", defaultParamValue = 4096f))
    add(ProtocolDefinition(64, "WIFI_SPECTRUM_ANALYZER", "2.4GHz / 5GHz Wi-Fi Channel Congestion", "ANALYZE", "Analyzes surrounding access points and identifies optimal Wi-Fi channels.", paramLabel = "Signal Cutoff dBm", defaultParamValue = -85f))
    add(ProtocolDefinition(65, "ENERGY_DRAIN_PROFILER", "Real-Time App Power Consumption Audit", "ANALYZE", "Monitors power draw per app in mAh to pinpoint battery hog applications.", paramLabel = "Audit Window (m)", defaultParamValue = 30f))
    add(ProtocolDefinition(66, "DISPLAY_LATENCY_TEST", "Touch Screen Response Latency Probe", "ANALYZE", "Measures touch input polling frequency and display rendering delay in ms.", paramLabel = "Sample Count", defaultParamValue = 120f))
    add(ProtocolDefinition(67, "SENSOR_CALIBRATION", "Accelerometer & Gyroscope Alignment", "ANALYZE", "Calibrates device inertial sensors, barometer, and geomagnetic compass.", paramLabel = "Precision Tolerance", defaultParamValue = 99f))
    add(ProtocolDefinition(68, "RAM_BANDWIDTH_CHECK", "LPDDR5 Memory Speed Diagnostic", "ANALYZE", "Tests peak memory throughput in GB/s and detects single-bit errors.", paramLabel = "Test Pass Count", defaultParamValue = 3f))
    add(ProtocolDefinition(69, "GPU_SHADING_BENCHMARK", "Adreno GPU Shader Pipeline Test", "ANALYZE", "Renders test 3D scene to calculate real-time GFLOPS and frame stability.", paramLabel = "Target FPS", defaultParamValue = 120f))
    add(ProtocolDefinition(70, "THERMAL_THROTTLE_MAP", "CPU Core Heat Distribution Profile", "ANALYZE", "Plots temperature curve across octa-core CPU under load.", paramLabel = "Load %", defaultParamValue = 80f))
    add(ProtocolDefinition(71, "NETWORK_JITTER_PROBE", "Ping Latency & Packet Loss Inspector", "ANALYZE", "Pings global DNS servers to measure round-trip time, jitter, and packet drop rate.", paramLabel = "Ping Count", defaultParamValue = 20f))
    add(ProtocolDefinition(72, "AUDIO_FREQUENCY_ANALYZER", "Microphone Frequency Response Test", "ANALYZE", "Analyzes mic input frequency response curve from 20Hz to 20kHz.", paramLabel = "FFT Bin Size", defaultParamValue = 1024f))
    add(ProtocolDefinition(73, "CAMERA_OPTIC_DIAGNOSTIC", "Camera Sensor Pixel Defect Scan", "ANALYZE", "Performs dark frame noise calibration to locate dead camera pixels.", paramLabel = "ISO Target", defaultParamValue = 400f))
    add(ProtocolDefinition(74, "BLUETOOTH_SIGNAL_MAPPER", "BLE Beacon RSSI Signal Mapping", "ANALYZE", "Scans nearby Bluetooth Low Energy devices and estimates distance in meters.", paramLabel = "RSSI Threshold", defaultParamValue = -70f))
    add(ProtocolDefinition(75, "GPS_SATELLITE_LOCK_TEST", "GNSS Satellite Signal Strength Probe", "ANALYZE", "Inspects locked GPS, GLONASS, Galileo, and BeiDou satellites.", paramLabel = "Min Satellites", defaultParamValue = 8f))
    add(ProtocolDefinition(76, "SYSTEM_FILE_INTEGRITY", "Android OS Kernel File Checksum", "ANALYZE", "Verifies cryptographic signatures of system binaries and library files.", paramLabel = "Strictness", defaultParamValue = 100f))
    add(ProtocolDefinition(77, "VIRTUAL_MEMORY_AUDIT", "Linux Kernel Virtual Memory Usage", "ANALYZE", "Inspects page table allocation, dirty memory pages, and swap file pressure.", paramLabel = "Page Size KB", defaultParamValue = 4f))
    add(ProtocolDefinition(78, "CELLULAR_SIGNAL_ANALYZER", "5G / LTE Carrier Band Signal Quality", "ANALYZE", "Inspects RSRP, RSRQ, and SINR parameters for cell tower connection.", paramLabel = "Scan Mode", defaultParamValue = 5f))
    add(ProtocolDefinition(79, "HARDWARE_SERAIL_INSPECT", "Samsung Knox & Hardware Fuse Status", "ANALYZE", "Verifies Knox warranty bit, secure element hardware status, and chip revision.", paramLabel = "Fuse Level", defaultParamValue = 1f))
    add(ProtocolDefinition(80, "JARVIS_SELF_DIAGNOSTIC", "JARVIS AI Self-Diagnostic & Code Check", "ANALYZE", "Runs internal AI model health checks, verifies SQLite DB, and tests coroutine loops.", paramLabel = "Check Depth", defaultParamValue = 100f))

    // 81-100: Security, Cryptography & Hardware Defense
    add(ProtocolDefinition(81, "ENCRYPTED_VAULT_LOCK", "AES-256 GCM Memory Vault Shield", "DEFENSE", "Locks and encrypts stored notes, secrets, and keys with hardware keystore.", defaultEnabled = true, requiresBiometric = true, paramLabel = "Cipher Bits", defaultParamValue = 256f))
    add(ProtocolDefinition(82, "BIOMETRIC_HARDWARE_GUARD", "S25 Secure Element Fingerprint Lock", "DEFENSE", "Requires biometric hardware verification before granting sensitive protocol access.", defaultEnabled = true, requiresBiometric = true, paramLabel = "Security Level", defaultParamValue = 100f))
    add(ProtocolDefinition(83, "ROGUE_AP_MITIGATION", "Anti-Evil-Twin Wi-Fi Mitigator", "DEFENSE", "Detects spoofed Wi-Fi access points performing man-in-the-middle attacks.", defaultEnabled = true, requiresBiometric = false, paramLabel = "Probe Sensitivity", defaultParamValue = 90f))
    add(ProtocolDefinition(84, "PACKET_SNIFFER_DETECTOR", "Promiscuous Network Interface Scanner", "DEFENSE", "Monitors local network interface for unauthorized packet sniffing attempts.", defaultEnabled = true, requiresBiometric = false, paramLabel = "Alert Threshold", defaultParamValue = 80f))
    add(ProtocolDefinition(85, "MALWARE_HEURISTIC_PROBE", "Static & Dynamic App Behavior Inspector", "DEFENSE", "Scans installed applications for suspicious permission combinations and API abuse.", defaultEnabled = true, requiresBiometric = false, paramLabel = "Heuristic Rigor", defaultParamValue = 85f))
    add(ProtocolDefinition(86, "USB_DEBUGGING_SHIELD", "ADB Port Unauthorized Access Interceptor", "DEFENSE", "Locks down ADB connection when USB is connected to unknown computer hosts.", defaultEnabled = true, requiresBiometric = true, paramLabel = "Lockout Time (m)", defaultParamValue = 30f))
    add(ProtocolDefinition(87, "SCREEN_RECORD_BLOCKER", "FLAG_SECURE Screen Capture Shield", "DEFENSE", "Prevents unauthorized background apps from capturing screenshots of JARVIS HUD.", defaultEnabled = true, requiresBiometric = false, paramLabel = "Shield Mode", defaultParamValue = 1f))
    add(ProtocolDefinition(88, "CAMERA_MIC_KILLSWITCH", "Hardware Micro-Permission Killswitch", "DEFENSE", "Instantly revokes camera and microphone access system-wide in high security mode.", defaultEnabled = false, requiresBiometric = true, paramLabel = "Override Duration (m)", defaultParamValue = 60f))
    add(ProtocolDefinition(89, "LOCATION_SPOOF_DETECTOR", "Mock Location Provider Inspector", "DEFENSE", "Detects mock GPS location spoofing software running in background.", defaultEnabled = true, requiresBiometric = false, paramLabel = "Tolerance Meters", defaultParamValue = 10f))
    add(ProtocolDefinition(90, "SMS_PHISHING_FILTER", "Smishing & Malicious URL Interceptor", "DEFENSE", "Analyzes incoming SMS message links against known malicious domain blacklists.", defaultEnabled = true, requiresBiometric = false, paramLabel = "Filter Strictness", defaultParamValue = 95f))
    add(ProtocolDefinition(91, "KEYLOGGER_DETECTION", "Accessibility Service Keylogging Probe", "DEFENSE", "Identifies third-party accessibility services monitoring screen taps and keypresses.", defaultEnabled = true, requiresBiometric = true, paramLabel = "Rigor Level", defaultParamValue = 90f))
    add(ProtocolDefinition(92, "ENCRYPTED_BACKUP_BLOB", "AES Zero-Knowledge Snapshot Exporter", "DEFENSE", "Packages app data into encrypted zero-knowledge file blob.", defaultEnabled = true, requiresBiometric = true, paramLabel = "PBKDF2 Rounds", defaultParamValue = 100000f))
    add(ProtocolDefinition(93, "OVERLAY_ATTACK_DEFENSE", "Tapjacking & Window Overlay Shield", "DEFENSE", "Detects invisible overlay windows covering interactive UI buttons.", defaultEnabled = true, requiresBiometric = false, paramLabel = "Alpha Cutoff", defaultParamValue = 0.1f))
    add(ProtocolDefinition(94, "PANIC_WIPE_SEQUENCE", "Emergency Crypto-Key Self-Destruct", "DEFENSE", "Wipes memory vault encryption keys if invalid biometric fails 10 consecutive times.", defaultEnabled = false, requiresBiometric = true, paramLabel = "Max Failed Attempts", defaultParamValue = 10f))
    add(ProtocolDefinition(95, "TOR_ONION_TUNNEL", "SOCKS5 Anonymized Proxy Router", "DEFENSE", "Routes critical command traffic through onion proxy nodes.", defaultEnabled = false, requiresBiometric = false, paramLabel = "Hops", defaultParamValue = 3f))
    add(ProtocolDefinition(96, "BLUETOOTH_BLUEBORNE_GUARD", "Bluetooth Zero-Day Exploit Shield", "DEFENSE", "Filters unauthenticated L2CAP packet requests to prevent silent pairing exploits.", defaultEnabled = true, requiresBiometric = false, paramLabel = "Guard Level", defaultParamValue = 90f))
    add(ProtocolDefinition(97, "ROOT_MAGISK_DETECTOR", "Kernel Integrity & Tamper Detector", "DEFENSE", "Checks for unauthorized root binaries, modified boot images, or Magisk hooks.", defaultEnabled = true, requiresBiometric = false, paramLabel = "Check Rigor", defaultParamValue = 100f))
    add(ProtocolDefinition(98, "SSL_PINNING_ENFORCER", "X.509 Certificate Pinning Validator", "DEFENSE", "Ensures all network calls strictly validate server TLS certificate fingerprints.", defaultEnabled = true, requiresBiometric = false, paramLabel = "Enforce Mode", defaultParamValue = 1f))
    add(ProtocolDefinition(99, "JARVIS_SELF_HEAL_ENGINE", "AI Code Auto-Repair & System Healer", "DEFENSE", "Scans JARVIS code state, fixes broken flow listeners, and patches runtime errors.", defaultEnabled = true, requiresBiometric = false, paramLabel = "Auto-Repair Rigor", defaultParamValue = 100f))
    add(ProtocolDefinition(100, "S25_HARDWARE_LOCKDOWN", "Master Hardware Security Sentinel", "DEFENSE", "Combines all security protocols into a single one-tap military defense shield.", defaultEnabled = true, requiresBiometric = true, paramLabel = "Defense Tier", defaultParamValue = 5f))
}

@Composable
fun QuantumProtocolMatrixScreen(
    viewModel: JarvisViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    // Protocol States
    val enabledStates = remember {
        mutableStateMapOf<Int, Boolean>().apply {
            QUANTUM_100_PROTOCOLS.forEach { put(it.id, it.defaultEnabled) }
        }
    }
    val paramValues = remember {
        mutableStateMapOf<Int, Float>().apply {
            QUANTUM_100_PROTOCOLS.forEach { put(it.id, it.defaultParamValue) }
        }
    }
    val executingProtocols = remember { mutableStateMapOf<Int, Boolean>() }

    val categories = listOf("ALL", "CLEAN", "ORGANIZE", "AUTOMATE", "ANALYZE", "DEFENSE")

    val filteredList = remember(searchQuery, selectedCategory) {
        QUANTUM_100_PROTOCOLS.filter { proto ->
            val matchesCategory = (selectedCategory == "ALL" || proto.category.equals(selectedCategory, ignoreCase = true))
            val matchesQuery = searchQuery.isEmpty() ||
                    proto.name.contains(searchQuery, ignoreCase = true) ||
                    proto.codeName.contains(searchQuery, ignoreCase = true) ||
                    proto.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        JarvisVoid,
                        JarvisSurfaceDark,
                        JarvisVoid
                    )
                )
            )
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Alienware White + Light Blue Top Header Frame
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassSurface(
                        blurRadius = 20.dp,
                        cornerRadius = 20.dp,
                        transparencyAlpha = 0.35f,
                        accentColor = LightBlueCorrection
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(JarvisSurfaceElevated)
                            .border(1.dp, LightBlueCorrection, CircleShape)
                            .testTag("protocol_matrix_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = AlienwareWhitePure
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "QUANTUM PROTOCOL MATRIX",
                            style = Typography.displayMedium,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlienwareWhitePure
                        )
                        Text(
                            text = "100 LIVE AUTONOMOUS DIRECTIVES • S25 HARDWARE",
                            style = Typography.labelSmall,
                            fontSize = 9.sp,
                            color = LightBlueCorrection
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisSurfaceElevated)
                        .border(1.dp, JarvisSuccessGreen, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${enabledStates.count { it.value }}/100 ACTIVE",
                        style = Typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisSuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // PC Liquid Cooling Status Bar
            LiquidCoolingSystemHeaderBar()

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar & Filter Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text("Search 100 protocols...", color = JarvisTextMuted, fontSize = 12.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = LightBlueCorrection, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = JarvisTextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AlienwareWhitePure,
                        unfocusedTextColor = AlienwareWhiteIce,
                        focusedBorderColor = LightBlueCorrection,
                        unfocusedBorderColor = JarvisBorderActive.copy(alpha = 0.5f),
                        focusedContainerColor = JarvisSurfaceDark,
                        unfocusedContainerColor = JarvisSurfaceDark
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("protocol_search_input")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category Selector Horizontal Scroll Pill Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 6.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    val badgeCount = if (cat == "ALL") 100 else QUANTUM_100_PROTOCOLS.count { it.category == cat }
                    val categoryColor = when (cat) {
                        "CLEAN" -> LiquidCoolantFlow
                        "ORGANIZE" -> LightBlueCorrection
                        "AUTOMATE" -> JarvisArcGold
                        "ANALYZE" -> JarvisCyanBright
                        "DEFENSE" -> JarvisAlertRed
                        else -> AlienwareWhitePure
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) categoryColor.copy(alpha = 0.25f) else JarvisSurfaceDark)
                            .border(
                                1.dp,
                                if (isSelected) categoryColor else JarvisBorderActive.copy(alpha = 0.4f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = cat,
                                style = Typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) categoryColor else JarvisTextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "($badgeCount)",
                                style = Typography.labelSmall,
                                fontSize = 9.sp,
                                color = if (isSelected) categoryColor else JarvisTextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 100 Protocol Items List (Scrollable)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filteredList, key = { it.id }) { protocol ->
                    val isEnabled = enabledStates[protocol.id] ?: true
                    val currentParam = paramValues[protocol.id] ?: protocol.defaultParamValue
                    val isRunning = executingProtocols[protocol.id] ?: false

                    ProtocolMatrixItemCard(
                        protocol = protocol,
                        isEnabled = isEnabled,
                        paramValue = currentParam,
                        isRunning = isRunning,
                        onToggleEnabled = { newState ->
                            if (protocol.requiresBiometric && activity != null) {
                                JarvisBiometricAuthManager.authenticate(
                                    activity = activity,
                                    title = "S25 Security: ${protocol.name}",
                                    subtitle = "Biometric Verification Required",
                                    description = "Authenticate fingerprint to modify defense protocol state.",
                                    onSuccess = {
                                        enabledStates[protocol.id] = newState
                                        viewModel.logSystemEvent("PROTOCOL", "Biometric verification granted for ${protocol.codeName} -> $newState", "SECURITY")
                                        Toast.makeText(context, "Biometric Auth Success: ${protocol.codeName}", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { err ->
                                        Toast.makeText(context, "Auth Failed: $err", Toast.LENGTH_LONG).show()
                                    }
                                )
                            } else {
                                enabledStates[protocol.id] = newState
                                viewModel.logSystemEvent("PROTOCOL", "Toggled ${protocol.codeName} to $newState", "CONFIG")
                            }
                        },
                        onParamChange = { newFloat ->
                            paramValues[protocol.id] = newFloat
                        },
                        onExecuteNow = {
                            if (protocol.requiresBiometric && activity != null) {
                                JarvisBiometricAuthManager.authenticate(
                                    activity = activity,
                                    title = "Engage ${protocol.codeName}",
                                    subtitle = "S25 Secure Element Hardware Authorization",
                                    description = "Scan fingerprint to execute high-tier system command.",
                                    onSuccess = {
                                        executeProtocolAction(viewModel, protocol, currentParam, executingProtocols, context)
                                    },
                                    onError = { err ->
                                        Toast.makeText(context, "Biometric Auth Required: $err", Toast.LENGTH_LONG).show()
                                    }
                                )
                            } else {
                                executeProtocolAction(viewModel, protocol, currentParam, executingProtocols, context)
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun executeProtocolAction(
    viewModel: JarvisViewModel,
    protocol: ProtocolDefinition,
    paramValue: Float,
    executingMap: androidx.compose.runtime.snapshots.SnapshotStateMap<Int, Boolean>,
    context: android.content.Context
) {
    executingMap[protocol.id] = true
    viewModel.soundSynthesizer.playEngageSequence()
    viewModel.logSystemEvent("PROTOCOL_ENGAGE", "Executing #${protocol.id} ${protocol.codeName} at ${paramValue.toInt()}%", "TACTICAL")

    // Trigger self-healing if protocol 99 is executed
    if (protocol.id == 99) {
        viewModel.runSelfHealingDiagnostic()
    }

    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
        executingMap[protocol.id] = false
        Toast.makeText(context, "✅ Protocol #${protocol.id} ${protocol.codeName} Completed!", Toast.LENGTH_SHORT).show()
    }, 1800)
}

@Composable
fun LiquidCoolingSystemHeaderBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassSurface(
                blurRadius = 16.dp,
                cornerRadius = 16.dp,
                transparencyAlpha = 0.40f,
                accentColor = LiquidCoolantFlow
            )
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDamage,
                        contentDescription = "Liquid Cooling",
                        tint = LiquidCoolantFlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "JARVIS S25 LIQUID COOLING LOOP",
                        style = Typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AlienwareWhitePure
                    )
                }

                Text(
                    text = "STATUS: OPTIMAL • 31.4°C",
                    style = Typography.labelSmall,
                    fontSize = 9.sp,
                    color = LiquidCoolantFlow
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Liquid Coolant Flow Meter Gauges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CoolingGaugeTile(label = "PUMP SPEED", value = "3,400 RPM", icon = Icons.Default.Speed, color = LiquidCoolantFlow)
                CoolingGaugeTile(label = "COOLANT TEMP", value = "31.4 °C", icon = Icons.Default.Thermostat, color = LightBlueCorrection)
                CoolingGaugeTile(label = "LOOP PRESSURE", value = "14.2 PSI", icon = Icons.Default.Memory, color = AlienwareWhiteIce)
                CoolingGaugeTile(label = "RAD FAN", value = "1,850 RPM", icon = Icons.Default.Refresh, color = JarvisSuccessGreen)
            }
        }
    }
}

@Composable
fun CoolingGaugeTile(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(JarvisSurfaceDark)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(text = label, style = Typography.labelSmall, fontSize = 7.sp, color = JarvisTextMuted)
            Text(text = value, style = Typography.labelSmall, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun ProtocolMatrixItemCard(
    protocol: ProtocolDefinition,
    isEnabled: Boolean,
    paramValue: Float,
    isRunning: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    onParamChange: (Float) -> Unit,
    onExecuteNow: () -> Unit
) {
    val categoryColor = when (protocol.category) {
        "CLEAN" -> LiquidCoolantFlow
        "ORGANIZE" -> LightBlueCorrection
        "AUTOMATE" -> JarvisArcGold
        "ANALYZE" -> JarvisCyanBright
        "DEFENSE" -> JarvisAlertRed
        else -> AlienwareWhitePure
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassSurface(
                blurRadius = 14.dp,
                cornerRadius = 16.dp,
                transparencyAlpha = if (isEnabled) 0.35f else 0.15f,
                accentColor = categoryColor
            )
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(categoryColor.copy(alpha = 0.20f))
                            .border(1.dp, categoryColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "#${"%03d".format(protocol.id)}",
                            style = Typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = protocol.name,
                                style = Typography.titleMedium,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isEnabled) AlienwareWhitePure else JarvisTextMuted
                            )
                            if (protocol.requiresBiometric) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Biometric Guard",
                                    tint = JarvisAlertRed,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Text(
                            text = "${protocol.category} • ${protocol.codeName}",
                            style = Typography.labelSmall,
                            fontSize = 9.sp,
                            color = categoryColor.copy(alpha = 0.8f)
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AlienwareWhitePure,
                        checkedTrackColor = categoryColor,
                        uncheckedThumbColor = JarvisTextMuted,
                        uncheckedTrackColor = JarvisSurfaceDark
                    ),
                    modifier = Modifier.testTag("toggle_protocol_${protocol.id}")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = protocol.description,
                style = Typography.bodyMedium,
                fontSize = 11.sp,
                color = if (isEnabled) JarvisTextSecondary else JarvisTextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Parameter Slider and Quick Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = protocol.paramLabel,
                            style = Typography.labelSmall,
                            fontSize = 9.sp,
                            color = JarvisTextMuted
                        )
                        Text(
                            text = "${paramValue.toInt()}%",
                            style = Typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                    }

                    Slider(
                        value = paramValue,
                        onValueChange = onParamChange,
                        valueRange = 0f..100f,
                        enabled = isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = categoryColor,
                            activeTrackColor = categoryColor,
                            inactiveTrackColor = JarvisSurfaceDark
                        ),
                        modifier = Modifier
                            .height(24.dp)
                            .testTag("slider_protocol_${protocol.id}")
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = onExecuteNow,
                    enabled = isEnabled && !isRunning,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) JarvisArcGold else categoryColor.copy(alpha = 0.25f),
                        contentColor = if (isRunning) JarvisVoid else categoryColor
                    ),
                    modifier = Modifier
                        .border(1.dp, categoryColor, RoundedCornerShape(8.dp))
                        .testTag("run_protocol_${protocol.id}")
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Refresh else Icons.Default.PlayArrow,
                        contentDescription = "Run",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isRunning) "RUNNING..." else "RUN",
                        style = Typography.labelLarge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
