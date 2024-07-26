/*
 * Infomaniak Core - Android
 * Copyright (C) 2024 Infomaniak Network SA
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
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

fun Context.networkStatusFlow(): Flow<Boolean> {
    val networks = mutableSetOf<Network>()
    val rootServerUrl = "a.root-servers.net"

    fun networkRequestBuilder(): NetworkRequest {
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

    fun hasInternetConnectivity(network: Network): Boolean = runCatching {
        network.getByName(rootServerUrl) != null
    }.getOrDefault(false)

    fun hasAvailableNetwork() = networks.any(::hasInternetConnectivity)

    return callbackFlow {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                networks.add(network)
                launch { send(hasAvailableNetwork()) }
            }

            override fun onLost(network: Network) {
                networks.remove(network)
                launch { send(hasAvailableNetwork()) }
            }
        }

        connectivityManager.registerNetworkCallback(networkRequestBuilder(), callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}
