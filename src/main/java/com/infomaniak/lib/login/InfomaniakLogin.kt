package com.infomaniak.lib.login

import android.app.Activity
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
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Class which consists to create and manage an oauth 2.0 connection through Infomaniak Process
 * Supports PKCE challenge and legacy browser
 */
class InfomaniakLogin(
    private val context: Context,
    private var loginUrl: String = DEFAULT_LOGIN_URL,
    private val clientID: String,
    private val appUID: String
) {

    companion object {
        private const val CHROME_STABLE_PACKAGE = "com.android.chrome"
        private const val SERVICE_ACTION = "android.support.customtabs.action.CustomTabsService"
        private const val DEFAULT_ACCESS_TYPE = "offline"
        private const val DEFAULT_HASH_MODE = "SHA-256"
        private const val DEFAULT_HASH_MODE_SHORT = "S256"
        private const val DEFAULT_LOGIN_URL = "https://login.infomaniak.com/"
        private const val DEFAULT_REDIRECT_URI = "://oauth2redirect"
        private const val DEFAULT_RESPONSE_TYPE = "code"
        private const val preferenceName = "pkce_step_codes"
        private const val verifierKey = "code_verifier"

        const val LOGIN_URL_TAG = "login_url"
        const val CODE_TAG = "code"
        const val ERROR_TRANSLATED_TAG = "translated_error"
        const val ERROR_CODE_TAG = "error_code"

        const val WEBVIEW_ERROR_CODE_INTERNET_DISCONNECTED = "net::ERR_INTERNET_DISCONNECTED"
        const val WEBVIEW_ERROR_CODE_CONNECTION_REFUSED = "net::ERR_CONNECTION_REFUSED"

        const val ERROR_ACCESS_DENIED = "access_denied"

        const val SSL_ERROR_CODE = "ssl_error_code"
        const val HTTP_ERROR_CODE = "http_error_code"
    }

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

    /**
     * Officially start the Chrome Tab
     */
    fun start(): Boolean {
        val codeChallenge = generatePkceCodes()
        val url = generateUrl(codeChallenge)
        var success = false
        if (URLUtil.isValidUrl(url)) {
            when {
                isChromeCustomTabsSupported(context) -> bindCustomTabsService(url)
                else -> {
                    success = showOnDefaultBrowser((url))
                }
            }
        }
        return success
    }

    /**
     * Start WebView login
     * @param requestCode : activity for result request code
     */
    fun startWebViewLogin(requestCode: Int, fragment: Fragment? = null) {
        val codeChallenge = generatePkceCodes()
        val url = generateUrl(codeChallenge)
        val intent = Intent(context, WebViewLoginActivity::class.java).apply {
            putExtra(LOGIN_URL_TAG, url)
            putExtra(WebViewLoginActivity.APPLICATION_ID_TAG, appUID)
        }
        if (fragment == null) {
            (context as Activity).startActivityForResult(intent, requestCode)
        } else {
            fragment.startActivityForResult(intent, requestCode)
        }
    }

    fun getCodeVerifier(): String {
        val prefs: SharedPreferences = context.getSharedPreferences(preferenceName, MODE_PRIVATE)
        return prefs.getString(verifierKey, "").toString()
    }

    fun getRedirectURI() = "$appUID$DEFAULT_REDIRECT_URI"

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
    private fun generatePkceCodes(): String {
        val codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier)
        val editor = context.getSharedPreferences(preferenceName, MODE_PRIVATE).edit()
        editor.putString(verifierKey, codeVerifier)
        editor.apply()

        return codeChallenge
    }

    /**
     * Generate the complete login URL based on parameters and base
     */
    private fun generateUrl(codeChallenge: String): String {
        return loginUrl + "authorize/" +
                "?response_type=$DEFAULT_RESPONSE_TYPE" +
                "&access_type=$DEFAULT_ACCESS_TYPE" +
                "&client_id=$clientID" +
                "&redirect_uri=${getRedirectURI()}" +
                "&code_challenge_method=$DEFAULT_HASH_MODE_SHORT" +
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

    enum class ErrorStatus {
        SERVER,
        AUTH,
        CONNECTION,
        UNKNOWN;
    }

    suspend fun getToken(
        okHttpClient: OkHttpClient,
        code: String,
        onSuccess: (apiToken: ApiToken) -> Unit,
        onError: (error: ErrorStatus) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val formBuilder: MultipartBody.Builder = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("grant_type", "authorization_code")
                    .addFormDataPart("client_id", clientID)
                    .addFormDataPart("code", code)
                    .addFormDataPart("code_verifier", getCodeVerifier())
                    .addFormDataPart("redirect_uri", getRedirectURI())

                val request = Request.Builder()
                    .url("${loginUrl}token")
                    .post(formBuilder.build())
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val bodyResponse = response.body?.string()

                when {
                    response.code >= 500 -> {
                        withContext(Dispatchers.Main) {
                            onError(ErrorStatus.SERVER)
                        }
                    }
                    response.code >= 400 -> {
                        withContext(Dispatchers.Main) {
                            onError(ErrorStatus.AUTH)
                        }
                    }
                    bodyResponse.isNullOrBlank() -> {
                        withContext(Dispatchers.Main) {
                            onError(ErrorStatus.CONNECTION)
                        }
                    }
                    else -> {
                        withContext(Dispatchers.Default) {
                            val gson = Gson()
                            val jsonResult = JsonParser.parseString(bodyResponse)
                            val apiToken = gson.fromJson(jsonResult, ApiToken::class.java)

                            // Set the token expiration date (with margin-delay)
                            apiToken.expiresAt =
                                System.currentTimeMillis() + ((apiToken.expiresIn - 60) * 1000)

                            withContext(Dispatchers.Main) {
                                onSuccess(apiToken)
                            }
                        }
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()

                val descriptionError =
                    if (exception.javaClass.name.contains("java.net.", ignoreCase = true) ||
                        exception.javaClass.name.contains("javax.net.", ignoreCase = true)
                    ) {
                        ErrorStatus.CONNECTION
                    } else {
                        ErrorStatus.UNKNOWN
                    }
                withContext(Dispatchers.Main) {
                    onError(descriptionError)
                }
            }
        }
    }
}
