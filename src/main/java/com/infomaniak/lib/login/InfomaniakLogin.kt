package com.infomaniak.lib.login

import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ResolveInfo
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.webkit.URLUtil
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Class which consists to create and manage an oauth 2.0 connection through Infomaniak Process
 * Supports PKCE challenge and legacy browser
 */
class InfomaniakLogin(
    private val context: Context,
    private var loginUrl: String = DEFAULT_LOGIN_URL,
    private val clientId: String,
    private val appUID: String,
    private val redirectUri: String
) {

    companion object {
        private const val CHROME_STABLE_PACKAGE = "com.android.chrome"
        private const val SERVICE_ACTION = "android.support.customtabs.action.CustomTabsService"
        private const val DEFAULT_LOGIN_URL = "https://login.infomaniak.com/"
        private const val DEFAULT_RESPONSE_TYPE = "code"
        private const val DEFAULT_ACCESS_TYPE = "offline"
        private const val DEFAULT_HASH_MODE = "SHA-256"
        private const val DEFAULT_HASH_MODE_SHORT = "S256"
    }

    private lateinit var codeChallengeMethod: String
    private lateinit var codeChallenge: String

    lateinit var codeVerifier: String

    private var tabClient: CustomTabsClient? = null
    private var tabConnection: CustomTabsServiceConnection? = null
    private val tabIntent: CustomTabsIntent by lazy {
        CustomTabsIntent.Builder()
            .run {
                build()
            }.also { customTabIntent ->
                customTabIntent.intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
    }

    init {
        // Generate the codes for PKCE Challenge
        generatePkceCodes()
        // Generate the complete login URL based on codes and arguments
        generateUrl()
    }

    /**
     * Officially start the Chrome Tab
     */
    fun start(): Boolean {
        var success = false
        if (URLUtil.isValidUrl(loginUrl)) {
            when {
                isChromeCustomTabsSupported(context) -> bindCustomTabsService(loginUrl)
                else -> {
                    success = showOnDefaultBrowser((loginUrl))
                }
            }
        }
        return success
    }

    /**
     * Unbind the custom tab (close the connection)
     */
    fun unbind() {
        try {
            context.unbindService(tabConnection!!)
        } catch (ignore: Exception) {
            Log.e("kLogin error", "The login service cannot be unbinded")
        }
    }

    /**
     * Instead of Chrome Custom Tab, create a tab in the default browser
     */
    private fun showOnDefaultBrowser(url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("kLogin error", "Unable to start")
            false
        }
    }

    /**
     * Bind the custom tab to the current context (modern method)
     * @url String : URL of the login page
     */
    private fun bindCustomTabsService(url: String) {
        tabConnection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(
                componentName: ComponentName,
                client: CustomTabsClient
            ) {
                tabClient = client
                launchCustomTab(url)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                tabClient = null
            }
        }

        unbind()
        CustomTabsClient.bindCustomTabsService(context, CHROME_STABLE_PACKAGE, tabConnection!!)
    }

    /**
     * Launch the custom tab based on an URL (legacy method)
     * @url String : URL of the login page
     */
    private fun launchCustomTab(url: String) {
        tabClient?.warmup(0L)
        tabIntent.launchUrl(context, Uri.parse(url))
    }

    /**
     * Determine if Custom Chrome tabs are supported on the device
     */
    private fun isChromeCustomTabsSupported(context: Context): Boolean {
        val serviceIntent = Intent(SERVICE_ACTION).apply {
            setPackage(CHROME_STABLE_PACKAGE)
        }
        val resolveInfos: MutableList<ResolveInfo>? =
            context.packageManager.queryIntentServices(serviceIntent, 0)
        return !resolveInfos.isNullOrEmpty()
    }

    /**
     * Will generate the PKCE challenge codes for this object
     */
    private fun generatePkceCodes() {
        codeChallengeMethod = DEFAULT_HASH_MODE_SHORT

        val preferenceName = "pkce_step_codes"
        val verifierTag = "code_verifier"
        val challengeTag = "code_challenge"

        val prefs: SharedPreferences = context.getSharedPreferences(preferenceName, MODE_PRIVATE)
        val verifier = prefs.getString(verifierTag, null)
        val challenge = prefs.getString(challengeTag, null)

        if (challenge == null || verifier == null) {
            codeVerifier = generateCodeVerifier()
            codeChallenge = generateCodeChallenge(codeVerifier)
            val editor = context.getSharedPreferences(preferenceName, MODE_PRIVATE).edit()
            editor.putString(verifierTag, codeVerifier)
            editor.putString(challengeTag, codeChallenge)
            editor.apply()
        } else {
            codeVerifier = verifier
            codeChallenge = challenge
        }
    }

    /**
     * Generate the complete login URL based on parameters and base
     */
    private fun generateUrl() {
        loginUrl = loginUrl + "authorize/" +
                "?response_type=$DEFAULT_RESPONSE_TYPE" +
                "&access_type=$DEFAULT_ACCESS_TYPE" +
                "&client_id=$clientId" +
                "&redirect_uri=$redirectUri" +
                "&code_challenge_method=$codeChallengeMethod" +
                "&code_challenge=$codeChallenge"
    }

    /**
     * Generate a verifier code for PKCE challenge (rfc7636 4.1.)
     */
    private fun generateCodeVerifier(): String {
        val sr = SecureRandom()
        val code = ByteArray(33)
        sr.nextBytes(code)
        return Base64.encodeToString(code, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    /**
     * Generate a challenge code for PKCE challenge (rfc7636 4.2.)
     */
    private fun generateCodeChallenge(codeVerifier: String): String {
        val bytes = codeVerifier.toByteArray(Charsets.US_ASCII)
        val md = MessageDigest.getInstance(DEFAULT_HASH_MODE)
        md.update(bytes, 0, bytes.size)
        val digest = md.digest()
        return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    fun checkResponse(
        intent: Intent,
        onSuccess: (code: String) -> Unit,
        onError: (error: String) -> Unit
    ) {
        val data = intent.data
        if (data != null && appUID == data.scheme) {
            intent.data = null
            val code = data.getQueryParameter("code")
            val error = data.getQueryParameter("error")
            if (!code.isNullOrBlank()) {
                onSuccess(code)
            }
            if (!error.isNullOrBlank()) {
                val errorTitle = if (error == "access_denied") {
                    context.getString(R.string.access_denied)
                } else {
                    context.getString(R.string.an_error_has_occurred)
                }
                onError(errorTitle)
            }
        }
    }
}
