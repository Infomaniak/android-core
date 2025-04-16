/*
 * Infomaniak Core - Android
 * Copyright (C) 2025 Infomaniak Network SA
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
package com.infomaniak.core.fragmentnavigation

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController

fun Fragment.isAtInitialDestination(substituteClassName: String? = null): Boolean {
    return findNavController().isAtInitialDestination(allowedInitialClassName = substituteClassName ?: javaClass.name)
}

fun NavController.isAtInitialDestination(allowedInitialClassName: String): Boolean {
    val currentlyAtClassName = when (val currentlyAtDestination = currentDestination) {
        is FragmentNavigator.Destination -> currentlyAtDestination.className
        is DialogFragmentNavigator.Destination -> currentlyAtDestination.className
        null -> return true
        else -> return false
    }

    return allowedInitialClassName == currentlyAtClassName
}

fun Fragment.safelyNavigate(directions: NavDirections) = findNavController().let { navController ->
    // Checks if the NavDirections still needs to be executed because we can find its actionId in currentDestination
    if (navController.currentDestination?.getAction(directions.actionId) != null) {
        navController.navigate(directions)
    }
}

fun Fragment.safelyNavigate(
    @IdRes resId: Int,
    args: Bundle? = null,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
    substituteClassName: String? = null,
) {
    if (isAtInitialDestination(substituteClassName)) findNavController().navigate(resId, args, navOptions, navigatorExtras)
}
