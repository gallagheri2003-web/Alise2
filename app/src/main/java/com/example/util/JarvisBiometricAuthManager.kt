package com.example.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

data class BiometricCapability(
    val isAvailable: Boolean,
    val statusCode: Int,
    val statusText: String,
    val hardwareSensorType: String,
    val isEnrolled: Boolean,
    val supportsDeviceCredential: Boolean
)

object JarvisBiometricAuthManager {

    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun checkBiometricCapability(context: Context): BiometricCapability {
        val biometricManager = BiometricManager.from(context)
        val strongOnlyCode = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        val comboCode = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        val isEnrolled = comboCode != BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED &&
                strongOnlyCode != BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
        val isAvailable = comboCode == BiometricManager.BIOMETRIC_SUCCESS

        val statusText = when (comboCode) {
            BiometricManager.BIOMETRIC_SUCCESS -> "BIOMETRIC SENSORS ARMED & SECURE"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "NO BIOMETRICS OR DEVICE PASSCODE ENROLLED"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "NO BIOMETRIC HARDWARE DETECTED"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "BIOMETRIC HARDWARE TEMPORARILY UNAVAILABLE"
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "SECURITY UPDATE REQUIRED FOR BIOMETRIC ENCLAVE"
            else -> "BIOMETRIC STATUS CODE $comboCode"
        }

        val sensorType = when {
            strongOnlyCode == BiometricManager.BIOMETRIC_SUCCESS -> "Class 3 (Strong / Ultrasonic Enclave)"
            comboCode == BiometricManager.BIOMETRIC_SUCCESS -> "Biometric Strong + Device Credential"
            else -> "Standby / Hardware Emulation Mode"
        }

        return BiometricCapability(
            isAvailable = isAvailable,
            statusCode = comboCode,
            statusText = statusText,
            hardwareSensorType = sensorType,
            isEnrolled = isEnrolled,
            supportsDeviceCredential = true
        )
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "JARVIS Core Interface Lockdown",
        subtitle: String = "Biometric Security Verification",
        description: String = "Scan fingerprint or enter system passcode to access JARVIS Core interface.",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                val friendlyMessage = when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_CANCELED -> "Authentication cancelled by operator."
                    BiometricPrompt.ERROR_LOCKOUT -> "Too many failed attempts. Biometric sensor locked for 30s."
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> "Biometric sensor locked. Use device passcode override."
                    BiometricPrompt.ERROR_NO_BIOMETRICS -> "No biometric templates enrolled on this device."
                    BiometricPrompt.ERROR_HW_NOT_PRESENT -> "No biometric hardware detected."
                    BiometricPrompt.ERROR_HW_UNAVAILABLE -> "Biometric sensor hardware currently unavailable."
                    else -> errString.toString()
                }
                onError(friendlyMessage)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }
        }

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)

        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val biometricManager = BiometricManager.from(activity)
        val canAuthStatus = biometricManager.canAuthenticate(authenticators)

        try {
            if (canAuthStatus == BiometricManager.BIOMETRIC_SUCCESS ||
                canAuthStatus == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                promptInfoBuilder.setAllowedAuthenticators(authenticators)
            } else {
                promptInfoBuilder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                promptInfoBuilder.setNegativeButtonText("Cancel")
            }

            val promptInfo = promptInfoBuilder.build()
            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            // Fallback attempt with Negative Button if allowed authenticators caused incompatibility
            try {
                val fallbackPromptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setDescription(description)
                    .setNegativeButtonText("Cancel")
                    .build()
                val fallbackPrompt = BiometricPrompt(activity, executor, callback)
                fallbackPrompt.authenticate(fallbackPromptInfo)
            } catch (fallbackEx: Exception) {
                onError("Biometric authentication error: ${fallbackEx.message ?: e.message}")
            }
        }
    }
}
