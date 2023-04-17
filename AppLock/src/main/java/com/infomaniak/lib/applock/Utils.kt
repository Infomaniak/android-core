/*
 * Infomaniak Core - Android
 * Copyright (C) 2023 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.lib.applock

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.util.Log
import android.widget.CompoundButton
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.infomaniak.lib.core.utils.getAppName

object Utils {

    const val APP_LOCK_TAG = "App lock"

    fun Context.isKeyguardSecure(): Boolean {
        return (getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager)?.isKeyguardSecure ?: false
    }

    @SuppressLint("NewApi")
    fun FragmentActivity.requestCredentials(onSuccess: () -> Unit) {
        val biometricPrompt = BiometricPrompt(this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getAppName())
            .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun FragmentActivity.silentlyReverseSwitch(switch: CompoundButton, onCredentialSuccessful: (Boolean) -> Unit) = with(switch) {
        if (tag == null) {
            silentClick()
            requestCredentials {
                Log.i(APP_LOCK_TAG, "success")
                silentClick() // Click that doesn't pass in listener
                onCredentialSuccessful(isChecked)
            }
        }
    }

    private fun CompoundButton.silentClick() {
        tag = true
        performClick()
        tag = null
    }
}
