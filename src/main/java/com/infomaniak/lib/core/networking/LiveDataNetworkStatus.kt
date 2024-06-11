/*
 * Infomaniak Core - Android
 * Copyright (C) 2022-2024 Infomaniak Network SA
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
package com.infomaniak.lib.core.networking

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import io.sentry.Sentry
import java.net.UnknownHostException

class LiveDataNetworkStatus(context: Context) : LiveData<Boolean>() {

    companion object {
        const val ROOT_SERVER_CHECK_URL = "a.root-servers.net"
    }

    private val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networks = mutableListOf<Network>()

    private val networkStateObject = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            networks.remove(network)
            postValue(hasAvailableNetwork())
        }

        override fun onAvailable(network: Network) {
            networks.add(network)
            postValue(hasAvailableNetwork())
        }

        private fun hasAvailableNetwork() = networks.any { checkInternetConnectivity(it) }

        fun checkInternetConnectivity(network: Network): Boolean {
            return try {
                network.getByName(ROOT_SERVER_CHECK_URL) != null
            } catch (e: UnknownHostException) {
                false
            }
        }
    }

    override fun onActive() {
        runCatching {
            connectivityManager.registerNetworkCallback(networkRequestBuilder(), networkStateObject)
        }.onFailure { exception ->
            // Fix potential Exception thrown by ConnectivityManager on Android 11
            // Already fixed in Android S and above
            // https://issuetracker.google.com/issues/175055271
            Log.e("LiveDataNetworkStatus", "onActive: registerNetworkCallback failed", exception)
            Sentry.captureException(exception)
        }
        postValue(false) // Consider all networks "unavailable" on start of listening
    }

    override fun onInactive() {
        runCatching {
            connectivityManager.unregisterNetworkCallback(networkStateObject)
        }.onFailure { exception ->
            Log.e("LiveDataNetworkStatus", "onInactive: unregisterNetworkCallback failed", exception)
            Sentry.captureException(exception)
        }
    }

    private fun networkRequestBuilder(): NetworkRequest {
        return NetworkRequest.Builder().apply {
            addTransportType(NetworkCapabilities.TRANSPORT_BLUETOOTH)
            addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) addTransportType(NetworkCapabilities.TRANSPORT_LOWPAN)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) addTransportType(NetworkCapabilities.TRANSPORT_USB)
            addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
        }.build()
    }
}
