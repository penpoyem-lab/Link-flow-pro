package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.MessageDigest

enum class LockType {
    PIN,
    PATTERN
}

class AppLockSecurityManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("linkflow_security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_PRIMARY_LOCK_TYPE = "primary_lock_type"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PATTERN_HASH = "pattern_hash"
        private const val KEY_LOCK_TIMEOUT_SEC = "lock_timeout_sec"
        private const val SALT = "LinkFlowSecuritySalt_2026_@!"
    }

    fun isAppLockEnabled(): Boolean = prefs.getBoolean(KEY_APP_LOCK_ENABLED, false)

    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply()
    }

    fun getPrimaryLockType(): LockType {
        val name = prefs.getString(KEY_PRIMARY_LOCK_TYPE, LockType.PIN.name) ?: LockType.PIN.name
        return runCatching { LockType.valueOf(name) }.getOrDefault(LockType.PIN)
    }

    fun setPrimaryLockType(type: LockType) {
        prefs.edit().putString(KEY_PRIMARY_LOCK_TYPE, type.name).apply()
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun getLockTimeoutSeconds(): Long = prefs.getLong(KEY_LOCK_TIMEOUT_SEC, 0L) // 0 = Immediately

    fun setLockTimeoutSeconds(seconds: Long) {
        prefs.edit().putLong(KEY_LOCK_TIMEOUT_SEC, seconds).apply()
    }

    fun hasPin(): Boolean = !prefs.getString(KEY_PIN_HASH, "").isNullOrBlank()

    fun setPin(pin: String) {
        val hash = hashString(pin)
        prefs.edit().putString(KEY_PIN_HASH, hash).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return storedHash == hashString(pin)
    }

    fun hasPattern(): Boolean = !prefs.getString(KEY_PATTERN_HASH, "").isNullOrBlank()

    fun setPattern(nodes: List<Int>) {
        val raw = nodes.joinToString(",")
        val hash = hashString(raw)
        prefs.edit().putString(KEY_PATTERN_HASH, hash).apply()
    }

    fun verifyPattern(nodes: List<Int>): Boolean {
        val storedHash = prefs.getString(KEY_PATTERN_HASH, null) ?: return false
        val raw = nodes.joinToString(",")
        return storedHash == hashString(raw)
    }

    fun clearSecurityCredentials() {
        prefs.edit()
            .putBoolean(KEY_APP_LOCK_ENABLED, false)
            .remove(KEY_PIN_HASH)
            .remove(KEY_PATTERN_HASH)
            .apply()
    }

    fun isBiometricHardwareAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun triggerBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Unlock LinkFlow",
        subtitle: String = "Confirm your fingerprint or biometric credential",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isBiometricHardwareAvailable()) {
            onError("Biometric authentication is not supported or not configured on this device")
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // User canceled or negative button clicked
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Fingerprint not recognized. Please try again.")
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Use PIN / Pattern")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .build()

        prompt.authenticate(promptInfo)
    }

    private fun hashString(input: String): String {
        val salted = input + SALT
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(salted.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
