package com.infomaniak.lib.core

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.infomaniak.lib.core.networking.HttpUtils
import okhttp3.Headers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

/**
 * Http Utils class test
 */
@RunWith(AndroidJUnit4::class)
class HttpUtilsTest {
    @Mock
    lateinit var mockContext: Context

    @Mock
    private val mockContextResources: Resources? = null

    @Mock
    private val mockSharedPreferences: SharedPreferences? = null

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        mockContext = mock(Context::class.java)
        `when`(mockContext.resources).thenReturn(mockContextResources)
        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences)
        if (mockContextResources != null) {
            `when`(mockContextResources.getString(anyInt())).thenReturn("mocked string")
            `when`(mockContextResources.getStringArray(anyInt()))
                .thenReturn(arrayOf("mocked string 1", "mocked string 2"))
            `when`(mockContextResources.getColor(anyInt())).thenReturn(Color.BLACK)
            `when`(mockContextResources.getBoolean(anyInt())).thenReturn(false)
            `when`(mockContextResources.getDimension(anyInt())).thenReturn(100f)
            `when`(mockContextResources.getIntArray(anyInt())).thenReturn(intArrayOf(1, 2, 3))
        }
        if (mockSharedPreferences != null) {
            `when`(mockSharedPreferences.getString(anyString(), anyString())).thenReturn("mocked preference string")
        }
        `when`(mockContext.applicationContext).thenReturn(mockContext)

        // Initializing globally Infomaniak core lib
        InfomaniakCore.init(
            appId = "com.infomaniak.core",
            appVersionName = "0.0.5",
            appVersionCode = 5,
            clientId = ""
        )
    }

    @Test
    fun getHeadersTest() {
        // Arrange (Already in setup)

        // Act
        val headers: Headers = HttpUtils.getHeaders()

        // Assert
        Assert.assertEquals("Bearer testToken", headers["Authorization"])
        Assert.assertEquals("application/json; charset=UTF-8", headers["Content-type"])
        Assert.assertEquals("no-cache", headers["Cache-Control"])
        Assert.assertEquals("mocked preference string", headers["Device-Identifier"])
        Assert.assertEquals("Android 0.0.5", headers["App-Version"])
    }
}
