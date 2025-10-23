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
import androidx.activity.result.ActivityResultLauncher
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import com.infomaniak.lib.login.ext.await
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
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
    private val appUID: String,
    private val accessType: AccessType? = AccessType.OFFLINE,
    sentryCallback: ((message: String) -> Unit)? = null
) {

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

    private val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
            useAlternativeNames = false
        }
    }

    init {
        Companion.sentryCallback = sentryCallback
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
     * @param resultLauncher Send back the result
     * @param removeCookies Remove all cookies if needed, by default is true
     */
    fun startWebViewLogin(resultLauncher: ActivityResultLauncher<Intent>, removeCookies: Boolean = true) {
        val codeChallenge = generatePkceCodes()
        val url = generateUrl(codeChallenge)
        val intent = Intent(context, WebViewLoginActivity::class.java).apply {
            putExtra(LOGIN_URL_TAG, url)
            putExtra(REMOVE_COOKIES_TAG, removeCookies)
            putExtra(WebViewLoginActivity.APPLICATION_ID_TAG, appUID)
        }
        resultLauncher.launch(intent)
    }

    /**
     * Start a WebView for the creation of a new account
     * @param resultLauncher Send back the result
     * @param createAccountUrl The url of the account creation page
     * @param successHost The host name when the account creation was successful
     * @param cancelHost The host name to connect, cancel will be invoke
     */
    fun startCreateAccountWebView(
        resultLauncher: ActivityResultLauncher<Intent>,
        createAccountUrl: String,
        successHost: String,
        cancelHost: String,
    ) {
        val intent = Intent(context, WebViewCreateAccountActivity::class.java).apply {
            putExtra(CREATE_ACCOUNT_URL_TAG, createAccountUrl)
            putExtra(SUCCESS_HOST_TAG, successHost)
            putExtra(CANCEL_HOST_TAG, cancelHost)
        }
        resultLauncher.launch(intent)
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
        val resolveInfos: MutableList<ResolveInfo> = context.packageManager.queryIntentServices(serviceIntent, 0)
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
        return loginUrl + "authorize" +
                "?response_type=$DEFAULT_RESPONSE_TYPE" +
                (if (accessType == null) "" else "&access_type=${accessType.apiValue}") +
                "&client_id=$clientID" +
                "&redirect_uri=${getRedirectURI()}" +
                "&code_challenge_method=$DEFAULT_HASH_MODE_SHORT" +
                "&code_challenge=$codeChallenge"
    }

    /**
     * Generate a verifier code for PKCE challenge (rfc7636 4.1.)
     */
    private fun generateCodeVerifier(): String {
        val secureRandom = SecureRandom()
        val code = ByteArray(33)
        secureRandom.nextBytes(code)
        return Base64.encodeToString(code, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    /**
     * Generate a challenge code for PKCE challenge (rfc7636 4.2.)
     */
    private fun generateCodeChallenge(codeVerifier: String): String {
        val bytes = codeVerifier.toByteArray(Charsets.US_ASCII)
        val messageDigest = MessageDigest.getInstance(DEFAULT_HASH_MODE)
        messageDigest.update(bytes, 0, bytes.size)
        val digest = messageDigest.digest()
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

    suspend fun getToken(okHttpClient: OkHttpClient, code: String): TokenResult {
        val formBuilder: MultipartBody.Builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("grant_type", "authorization_code")
            .addFormDataPart("client_id", clientID)
            .addFormDataPart("code", code)
            .addFormDataPart("code_verifier", getCodeVerifier())
            .addFormDataPart("redirect_uri", getRedirectURI())

        return getToken(okHttpClient = okHttpClient, body = formBuilder.build())
    }

    suspend fun getToken(okHttpClient: OkHttpClient, username: String, password: String): TokenResult {
        val formBuilder: MultipartBody.Builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("grant_type", "password")
            .addFormDataPart("client_id", clientID)
            .addFormDataPart("username", username)
            .addFormDataPart("password", password)

        accessType?.let { formBuilder.addFormDataPart("access_type", it.apiValue) }

        return getToken(okHttpClient = okHttpClient, body = formBuilder.build())
    }

    private suspend fun getToken(okHttpClient: OkHttpClient, body: RequestBody): TokenResult {
        return runCatching {
            val request = Request.Builder()
                .url("${loginUrl}token")
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).await()
            val bodyResponse = Dispatchers.IO { response.body?.string() }

            if (response.isSuccessful && bodyResponse != null) {
                val apiToken = json.decodeFromString<ApiToken>(bodyResponse)

                // Set the token expiration date (with margin-delay)
                apiToken.expiresAt = System.currentTimeMillis() + ((apiToken.expiresIn - 60) * 1000)

                TokenResult.Success(apiToken)
            } else {
                TokenResult.Error(handleErrorResponse(response.code, bodyResponse))
            }

        }.getOrElse { exception ->
            if (exception is CancellationException) throw exception
            return@getOrElse TokenResult.Error(getErrorStatusFromException(exception))
        }
    }

    /**
     *  Delete the token from the api and return **null** otherwise an [ErrorStatus]
     */
    suspend fun deleteToken(okHttpClient: OkHttpClient, token: ApiToken): ErrorStatus? {
        return runCatching {
            val request = Request.Builder()
                .url("${loginUrl}token")
                .addHeader("Authorization", "Bearer ${token.accessToken}")
                .delete()
                .build()

            val response = okHttpClient.newCall(request).await()
            val bodyResponse = Dispatchers.IO { response.body?.string() }

            if (response.isSuccessful && bodyResponse != null) {
                val apiResponse = json.decodeFromString<ApiResponse>(bodyResponse)
                if (apiResponse.result == "error") ErrorStatus.UNKNOWN else null
            } else {
                handleErrorResponse(response.code, bodyResponse)
            }

        }.getOrElse { exception ->
            if (exception is CancellationException) throw exception
            getErrorStatusFromException(exception)
        }
    }

    private fun handleErrorResponse(statusCode: Int, bodyResponse: String?): ErrorStatus = when {
        statusCode >= 500 -> ErrorStatus.SERVER
        statusCode >= 400 -> ErrorStatus.AUTH
        bodyResponse.isNullOrBlank() -> ErrorStatus.CONNECTION
        else -> ErrorStatus.UNKNOWN
    }

    private fun getErrorStatusFromException(exception: Throwable): ErrorStatus {
        return if (
            exception.javaClass.name.contains("java.net.", ignoreCase = true) ||
            exception.javaClass.name.contains("javax.net.", ignoreCase = true)
        ) {
            ErrorStatus.CONNECTION
        } else {
            ErrorStatus.UNKNOWN
        }
    }

    @Serializable
    private data class ApiResponse(
        val result: String,
        val error: String? = null,
    )

    enum class ErrorStatus {
        SERVER,
        AUTH,
        CONNECTION,
        UNKNOWN,
    }

    enum class AccessType(val apiValue: String) {
        OFFLINE("offline"),
    }

    sealed interface TokenResult {
        data class Success(val apiToken: ApiToken) : TokenResult
        data class Error(val errorStatus: ErrorStatus) : TokenResult
    }

    companion object {
        private const val CHROME_STABLE_PACKAGE = "com.android.chrome"
        private const val SERVICE_ACTION = "android.support.customtabs.action.CustomTabsService"
        private const val DEFAULT_HASH_MODE = "SHA-256"
        private const val DEFAULT_HASH_MODE_SHORT = "S256"
        private const val DEFAULT_LOGIN_URL = "https://login.infomaniak.com/"
        private const val DEFAULT_REDIRECT_URI = "://oauth2redirect"
        private const val DEFAULT_RESPONSE_TYPE = "code"
        private const val preferenceName = "pkce_step_codes"
        private const val verifierKey = "code_verifier"

        const val LOGIN_URL_TAG = "login_url"
        const val REMOVE_COOKIES_TAG = "remove_cookies_tag"
        const val CODE_TAG = "code"
        const val ERROR_TRANSLATED_TAG = "translated_error"
        const val ERROR_CODE_TAG = "error_code"

        const val CANCEL_HOST_TAG = "cancel_url"
        const val CREATE_ACCOUNT_URL_TAG = "create_account_url"
        const val SUCCESS_HOST_TAG = "success_url"

        const val WEBVIEW_ERROR_CODE_INTERNET_DISCONNECTED = "net::ERR_INTERNET_DISCONNECTED"
        const val WEBVIEW_ERROR_CODE_CONNECTION_REFUSED = "net::ERR_CONNECTION_REFUSED"
        const val WEBVIEW_ERROR_CODE_NAME_NOT_RESOLVED = "net::ERR_NAME_NOT_RESOLVED"

        const val ERROR_ACCESS_DENIED = "access_denied"

        const val SSL_ERROR_CODE = "ssl_error_code"
        const val HTTP_ERROR_CODE = "http_error_code"

        internal var sentryCallback: ((message: String) -> Unit)? = null
    }
}
