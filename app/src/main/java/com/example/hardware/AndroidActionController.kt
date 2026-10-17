package com.example.hardware

import android.Manifest
import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.ContextCompat
import java.util.Locale

data class ContactResolution(
    val name: String,
    val phoneNumber: String?,
    val hasContactsPermission: Boolean,
    val isResolvedFromContacts: Boolean
)

enum class CommandStatus {
    RECEIVED,
    UNDERSTOOD,
    EXECUTING,
    SUCCESS,
    REQUIRES_USER_CONFIRMATION,
    PERMISSION_REQUIRED,
    NOT_SUPPORTED,
    FAILED
}

enum class CommandIntent {
    SEND_MESSAGE,
    MAKE_PHONE_CALL,
    OPEN_APPLICATION,
    CONTROL_HARDWARE,
    DEVICE_TELEMETRY,
    SYSTEM_PROTOCOL,
    MEDIA_CONTROL,
    SET_ALARM_TIMER,
    SEARCH_QUERY,
    NOTE_TAKING,
    SYSTEM_DIRECTORY,
    SCREEN_CONTEXT,
    CLIPBOARD,
    GENERAL_AI_QUERY,
    UNKNOWN
}

data class CommandExecutionResult(
    val rawCommand: String,
    val intent: CommandIntent,
    val status: CommandStatus,
    val executionMethod: String,
    val resultDescription: String,
    val speechResponse: String,
    val requiredPermission: String? = null,
    val targetPackageOrUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

class AndroidActionController(private val context: Context) {

    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

    /**
     * Resolves a contact name into a phone number using ContactsContract if permission is granted.
     */
    fun resolveContact(contactQuery: String): ContactResolution {
        val trimmed = contactQuery.trim().removePrefix("@")
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        // Check if query is already a phone number
        val isDigitsOnly = trimmed.replace(Regex("[^0-9+]"), "").length >= 7
        if (isDigitsOnly) {
            return ContactResolution(
                name = trimmed,
                phoneNumber = trimmed,
                hasContactsPermission = hasPermission,
                isResolvedFromContacts = false
            )
        }

        if (!hasPermission) {
            return ContactResolution(
                name = trimmed,
                phoneNumber = null,
                hasContactsPermission = false,
                isResolvedFromContacts = false
            )
        }

        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                arrayOf("%$trimmed%"),
                null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val resolvedName = if (nameIndex >= 0) it.getString(nameIndex) else trimmed
                    val resolvedNumber = if (numberIndex >= 0) it.getString(numberIndex) else null
                    if (resolvedNumber != null) {
                        return ContactResolution(
                            name = resolvedName,
                            phoneNumber = resolvedNumber,
                            hasContactsPermission = true,
                            isResolvedFromContacts = true
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback gracefully
        }

        return ContactResolution(
            name = trimmed,
            phoneNumber = null,
            hasContactsPermission = hasPermission,
            isResolvedFromContacts = false
        )
    }

    /**
     * Prepares and launches real SMS message composer with recipient and body.
     */
    fun prepareSmsMessage(
        contactQuery: String,
        messageBody: String?,
        rawCommand: String
    ): CommandExecutionResult {
        val resolution = resolveContact(contactQuery)
        val targetPhone = resolution.phoneNumber ?: ""

        if (messageBody.isNullOrBlank()) {
            return CommandExecutionResult(
                rawCommand = rawCommand,
                intent = CommandIntent.SEND_MESSAGE,
                status = CommandStatus.REQUIRES_USER_CONFIRMATION,
                executionMethod = "Conversation Workflow: Awaiting Message Content",
                resultDescription = "Contact identified as '${resolution.name}'. Awaiting message body.",
                speechResponse = "What would you like me to say to ${resolution.name}?"
            )
        }

        return try {
            val smsUri = if (targetPhone.isNotBlank()) {
                Uri.parse("smsto:$targetPhone")
            } else {
                Uri.parse("smsto:")
            }

            val intent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
                putExtra("sms_body", messageBody)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Check if handler exists
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                val recipientLabel = if (targetPhone.isNotBlank()) "${resolution.name} ($targetPhone)" else resolution.name
                CommandExecutionResult(
                    rawCommand = rawCommand,
                    intent = CommandIntent.SEND_MESSAGE,
                    status = CommandStatus.SUCCESS,
                    executionMethod = "Android Intent: ACTION_SENDTO ($smsUri)",
                    resultDescription = "Prepared message to $recipientLabel and launched SMS composer.",
                    speechResponse = "Ready to send that message to ${resolution.name}. I have prepared the messaging interface on your Galaxy S25."
                )
            } else {
                // Fallback to general ACTION_SEND
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, messageBody)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(sendIntent, "Send message").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                CommandExecutionResult(
                    rawCommand = rawCommand,
                    intent = CommandIntent.SEND_MESSAGE,
                    status = CommandStatus.SUCCESS,
                    executionMethod = "Android Intent: ACTION_SEND",
                    resultDescription = "Launched message dispatcher with content.",
                    speechResponse = "Message prepared for ${resolution.name}. Opened messaging interface."
                )
            }
        } catch (e: Exception) {
            CommandExecutionResult(
                rawCommand = rawCommand,
                intent = CommandIntent.SEND_MESSAGE,
                status = CommandStatus.FAILED,
                executionMethod = "Android Intent: ACTION_SENDTO",
                resultDescription = "Failed to launch SMS interface: ${e.message}",
                speechResponse = "Unable to launch messaging interface due to system restriction: ${e.message}"
            )
        }
    }

    /**
     * Executes phone call or launches dialer with resolved contact number.
     */
    fun makePhoneCall(contactQuery: String, rawCommand: String): CommandExecutionResult {
        val resolution = resolveContact(contactQuery)
        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val phoneNumber = resolution.phoneNumber ?: contactQuery.filter { it.isDigit() || it == '+' }

        if (phoneNumber.isBlank() && !resolution.hasContactsPermission) {
            // Open general dialer
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(dialIntent)
            return CommandExecutionResult(
                rawCommand = rawCommand,
                intent = CommandIntent.MAKE_PHONE_CALL,
                status = CommandStatus.PERMISSION_REQUIRED,
                executionMethod = "Android Intent: ACTION_DIAL",
                resultDescription = "Contact permission not granted to look up '${resolution.name}'. Opened Phone dialer.",
                speechResponse = "I've opened the phone dialer. Grant Contacts permission in Settings to allow direct contact resolution for ${resolution.name}.",
                requiredPermission = Manifest.permission.READ_CONTACTS
            )
        }

        return try {
            if (hasCallPermission && phoneNumber.isNotBlank()) {
                val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(callIntent)
                CommandExecutionResult(
                    rawCommand = rawCommand,
                    intent = CommandIntent.MAKE_PHONE_CALL,
                    status = CommandStatus.SUCCESS,
                    executionMethod = "Android Intent: ACTION_CALL (tel:$phoneNumber)",
                    resultDescription = "Initiated direct voice call to ${resolution.name} ($phoneNumber).",
                    speechResponse = "Placing call to ${resolution.name} now."
                )
            } else {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)
                CommandExecutionResult(
                    rawCommand = rawCommand,
                    intent = CommandIntent.MAKE_PHONE_CALL,
                    status = CommandStatus.SUCCESS,
                    executionMethod = "Android Intent: ACTION_DIAL (tel:$phoneNumber)",
                    resultDescription = "Loaded ${resolution.name} ($phoneNumber) into Phone dialer.",
                    speechResponse = "Opening Phone dialer for ${resolution.name}."
                )
            }
        } catch (e: Exception) {
            CommandExecutionResult(
                rawCommand = rawCommand,
                intent = CommandIntent.MAKE_PHONE_CALL,
                status = CommandStatus.FAILED,
                executionMethod = "Android Intent: ACTION_DIAL",
                resultDescription = "Failed to initiate call: ${e.message}",
                speechResponse = "Unable to initiate call to ${resolution.name}: ${e.message}"
            )
        }
    }

    /**
     * Real application launcher resolving known apps and package manager launcher intents.
     */
    fun openApplication(appQuery: String, rawCommand: String): CommandExecutionResult {
        val lower = appQuery.lowercase().trim()
        val pm = context.packageManager

        // Known application mappings
        val knownPackages = mapOf(
            "spotify" to listOf("com.spotify.music"),
            "youtube" to listOf("com.google.android.youtube"),
            "maps" to listOf("com.google.android.apps.maps"),
            "camera" to listOf("com.sec.android.app.camera", "com.google.android.GoogleCamera"),
            "chrome" to listOf("com.android.chrome"),
            "browser" to listOf("com.sec.android.app.sbrowser", "com.android.chrome"),
            "messages" to listOf("com.google.android.apps.messaging", "com.samsung.android.messaging"),
            "phone" to listOf("com.google.android.dialer", "com.samsung.android.dialer"),
            "clock" to listOf("com.sec.android.app.clockpackage", "com.google.android.deskclock"),
            "calculator" to listOf("com.sec.android.app.popupcalculator", "com.google.android.calculator"),
            "calendar" to listOf("com.google.android.calendar", "com.samsung.android.calendar"),
            "settings" to listOf("com.android.settings"),
            "files" to listOf("com.sec.android.app.myfiles", "com.google.android.apps.nbu.files"),
            "gmail" to listOf("com.google.android.gm"),
            "mail" to listOf("com.google.android.gm", "com.samsung.android.email.provider")
        )

        // 1. Check special direct intent shortcuts
        if (lower == "camera") {
            return try {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                CommandExecutionResult(
                    rawCommand = rawCommand,
                    intent = CommandIntent.OPEN_APPLICATION,
                    status = CommandStatus.SUCCESS,
                    executionMethod = "Android Intent: MediaStore.ACTION_IMAGE_CAPTURE",
                    resultDescription = "Launched native Android Camera interface.",
                    speechResponse = "Opening Camera."
                )
            } catch (_: Exception) {
                launchPackageFallback(knownPackages["camera"] ?: emptyList(), "Camera", rawCommand)
            }
        }

        if (lower == "settings") {
            return try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                CommandExecutionResult(
                    rawCommand = rawCommand,
                    intent = CommandIntent.OPEN_APPLICATION,
                    status = CommandStatus.SUCCESS,
                    executionMethod = "Android Intent: Settings.ACTION_SETTINGS",
                    resultDescription = "Opened Samsung Galaxy S25 System Settings.",
                    speechResponse = "System settings interface opened."
                )
            } catch (e: Exception) {
                CommandExecutionResult(
                    rawCommand = rawCommand,
                    intent = CommandIntent.OPEN_APPLICATION,
                    status = CommandStatus.FAILED,
                    executionMethod = "Settings.ACTION_SETTINGS",
                    resultDescription = "Failed to launch settings: ${e.message}",
                    speechResponse = "Unable to open Settings."
                )
            }
        }

        if (lower == "clock" || lower == "alarms") {
            return try {
                val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                CommandExecutionResult(
                    rawCommand = rawCommand,
                    intent = CommandIntent.OPEN_APPLICATION,
                    status = CommandStatus.SUCCESS,
                    executionMethod = "Android Intent: AlarmClock.ACTION_SHOW_ALARMS",
                    resultDescription = "Opened Clock & Alarms application.",
                    speechResponse = "Clock interface opened."
                )
            } catch (_: Exception) {
                launchPackageFallback(knownPackages["clock"] ?: emptyList(), "Clock", rawCommand)
            }
        }

        if (lower == "calendar") {
            return try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("content://com.android.calendar/time")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                CommandExecutionResult(
                    rawCommand = rawCommand,
                    intent = CommandIntent.OPEN_APPLICATION,
                    status = CommandStatus.SUCCESS,
                    executionMethod = "Android Intent: CalendarContract View",
                    resultDescription = "Opened Calendar application.",
                    speechResponse = "Calendar schedule opened."
                )
            } catch (_: Exception) {
                launchPackageFallback(knownPackages["calendar"] ?: emptyList(), "Calendar", rawCommand)
            }
        }

        // 2. Try known package mappings
        for ((key, pkgList) in knownPackages) {
            if (lower.contains(key)) {
                return launchPackageFallback(pkgList, key.replaceFirstChar { it.uppercase() }, rawCommand)
            }
        }

        // 3. Search all installed launchable apps
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val matchedApp = resolveInfos.firstOrNull {
            val label = it.loadLabel(pm).toString().lowercase()
            label.contains(lower) || lower.contains(label)
        }

        if (matchedApp != null) {
            val pkg = matchedApp.activityInfo.packageName
            val launchIntent = pm.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                try {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    val label = matchedApp.loadLabel(pm).toString()
                    return CommandExecutionResult(
                        rawCommand = rawCommand,
                        intent = CommandIntent.OPEN_APPLICATION,
                        status = CommandStatus.SUCCESS,
                        executionMethod = "PackageManager: getLaunchIntentForPackage($pkg)",
                        resultDescription = "Launched $label ($pkg).",
                        speechResponse = "Launching $label for you, Ian.",
                        targetPackageOrUri = pkg
                    )
                } catch (e: Exception) {
                    return CommandExecutionResult(
                        rawCommand = rawCommand,
                        intent = CommandIntent.OPEN_APPLICATION,
                        status = CommandStatus.FAILED,
                        executionMethod = "PackageManager: getLaunchIntentForPackage($pkg)",
                        resultDescription = "Failed to launch $pkg: ${e.message}",
                        speechResponse = "Unable to launch $appQuery due to system restrictions."
                    )
                }
            }
        }

        // 4. App not installed on device
        return CommandExecutionResult(
            rawCommand = rawCommand,
            intent = CommandIntent.OPEN_APPLICATION,
            status = CommandStatus.NOT_SUPPORTED,
            executionMethod = "PackageManager query",
            resultDescription = "Application '$appQuery' is not installed on this device.",
            speechResponse = "Application '$appQuery' is not installed."
        )
    }

    private fun launchPackageFallback(pkgList: List<String>, appName: String, rawCommand: String): CommandExecutionResult {
        val pm = context.packageManager
        for (pkg in pkgList) {
            val intent = pm.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                try {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return CommandExecutionResult(
                        rawCommand = rawCommand,
                        intent = CommandIntent.OPEN_APPLICATION,
                        status = CommandStatus.SUCCESS,
                        executionMethod = "PackageManager: getLaunchIntentForPackage($pkg)",
                        resultDescription = "Launched $appName via $pkg.",
                        speechResponse = "Opening $appName, Ian.",
                        targetPackageOrUri = pkg
                    )
                } catch (_: Exception) {
                    continue
                }
            }
        }

        // Special handling for Spotify web fallback if app missing
        if (appName.equals("Spotify", ignoreCase = true)) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            return CommandExecutionResult(
                rawCommand = rawCommand,
                intent = CommandIntent.OPEN_APPLICATION,
                status = CommandStatus.SUCCESS,
                executionMethod = "Browser Fallback: https://open.spotify.com",
                resultDescription = "Spotify native app not found; opened Spotify Web Player.",
                speechResponse = "Opening Spotify web player."
            )
        }

        return CommandExecutionResult(
            rawCommand = rawCommand,
            intent = CommandIntent.OPEN_APPLICATION,
            status = CommandStatus.NOT_SUPPORTED,
            executionMethod = "PackageManager query",
            resultDescription = "$appName is not installed.",
            speechResponse = "$appName is not installed on your device."
        )
    }

    /**
     * Sets a real Android Alarm.
     */
    fun setAlarm(hour: Int, minute: Int, message: String, rawCommand: String): CommandExecutionResult {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            val timeStr = String.format(Locale.US, "%02d:%02d", hour, minute)
            CommandExecutionResult(
                rawCommand = rawCommand,
                intent = CommandIntent.SET_ALARM_TIMER,
                status = CommandStatus.SUCCESS,
                executionMethod = "Android Intent: AlarmClock.ACTION_SET_ALARM ($timeStr)",
                resultDescription = "Configured alarm for $timeStr labeled '$message'.",
                speechResponse = "Alarm set for $timeStr."
            )
        } catch (e: Exception) {
            CommandExecutionResult(
                rawCommand = rawCommand,
                intent = CommandIntent.SET_ALARM_TIMER,
                status = CommandStatus.FAILED,
                executionMethod = "AlarmClock.ACTION_SET_ALARM",
                resultDescription = "Failed to set alarm: ${e.message}",
                speechResponse = "Unable to set alarm: ${e.message}"
            )
        }
    }

    /**
     * Sets a real Android Countdown Timer.
     */
    fun setTimer(seconds: Int, message: String, rawCommand: String): CommandExecutionResult {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            val minutes = seconds / 60
            val label = if (minutes > 0) "$minutes minutes" else "$seconds seconds"
            CommandExecutionResult(
                rawCommand = rawCommand,
                intent = CommandIntent.SET_ALARM_TIMER,
                status = CommandStatus.SUCCESS,
                executionMethod = "Android Intent: AlarmClock.ACTION_SET_TIMER ($seconds s)",
                resultDescription = "Configured countdown timer for $label labeled '$message'.",
                speechResponse = "Timer set for $label."
            )
        } catch (e: Exception) {
            CommandExecutionResult(
                rawCommand = rawCommand,
                intent = CommandIntent.SET_ALARM_TIMER,
                status = CommandStatus.FAILED,
                executionMethod = "AlarmClock.ACTION_SET_TIMER",
                resultDescription = "Failed to set timer: ${e.message}",
                speechResponse = "Unable to set timer: ${e.message}"
            )
        }
    }

    /**
     * Performs a web search.
     */
    fun searchWeb(query: String, rawCommand: String): CommandExecutionResult {
        return try {
            val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (searchIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(searchIntent)
            } else {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            }
            CommandExecutionResult(
                rawCommand = rawCommand,
                intent = CommandIntent.SEARCH_QUERY,
                status = CommandStatus.SUCCESS,
                executionMethod = "Android Intent: ACTION_WEB_SEARCH",
                resultDescription = "Executed search query for: '$query'.",
                speechResponse = "Searching the web for '$query'."
            )
        } catch (e: Exception) {
            CommandExecutionResult(
                rawCommand = rawCommand,
                intent = CommandIntent.SEARCH_QUERY,
                status = CommandStatus.FAILED,
                executionMethod = "ACTION_WEB_SEARCH",
                resultDescription = "Search failed: ${e.message}",
                speechResponse = "Search failed: ${e.message}"
            )
        }
    }

    /**
     * Copies text to system clipboard.
     */
    fun copyToClipboard(text: String, rawCommand: String): CommandExecutionResult {
        return try {
            val clip = ClipData.newPlainText("ALISE Note", text)
            clipboardManager?.setPrimaryClip(clip)
            CommandExecutionResult(
                rawCommand = rawCommand,
                intent = CommandIntent.CLIPBOARD,
                status = CommandStatus.SUCCESS,
                executionMethod = "ClipboardManager.setPrimaryClip()",
                resultDescription = "Copied text to clipboard: \"$text\".",
                speechResponse = "Copied to clipboard."
            )
        } catch (e: Exception) {
            CommandExecutionResult(
                rawCommand = rawCommand,
                intent = CommandIntent.CLIPBOARD,
                status = CommandStatus.FAILED,
                executionMethod = "ClipboardManager",
                resultDescription = "Failed to copy: ${e.message}",
                speechResponse = "Clipboard operation failed."
            )
        }
    }

    /**
     * Reads text from system clipboard.
     */
    fun getClipboardText(rawCommand: String): CommandExecutionResult {
        return try {
            val clip = clipboardManager?.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString() ?: ""
                CommandExecutionResult(
                    rawCommand = rawCommand,
                    intent = CommandIntent.CLIPBOARD,
                    status = CommandStatus.SUCCESS,
                    executionMethod = "ClipboardManager.getPrimaryClip()",
                    resultDescription = "Retrieved clipboard content: \"$text\".",
                    speechResponse = if (text.isNotBlank()) "Your clipboard contains: $text" else "Your clipboard is currently empty."
                )
            } else {
                CommandExecutionResult(
                    rawCommand = rawCommand,
                    intent = CommandIntent.CLIPBOARD,
                    status = CommandStatus.SUCCESS,
                    executionMethod = "ClipboardManager.getPrimaryClip()",
                    resultDescription = "Clipboard is empty.",
                    speechResponse = "Your clipboard is currently empty, Ian."
                )
            }
        } catch (e: Exception) {
            CommandExecutionResult(
                rawCommand = rawCommand,
                intent = CommandIntent.CLIPBOARD,
                status = CommandStatus.FAILED,
                executionMethod = "ClipboardManager",
                resultDescription = "Clipboard access failed: ${e.message}",
                speechResponse = "Could not access clipboard."
            )
        }
    }
}
