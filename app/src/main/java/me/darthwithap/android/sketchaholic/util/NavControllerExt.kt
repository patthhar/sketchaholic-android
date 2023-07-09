package me.darthwithap.android.sketchaholic.util

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator

fun NavController.navigateSafely(
    @IdRes actionId: Int,
    navArgs: Bundle? = null,
    navOptions: NavOptions? = null,
    navExtras: Navigator.Extras? = null
) {
    val action = currentDestination?.getAction(actionId)
    if (action != null && currentDestination?.id != action.destinationId) {
        navigate(actionId, navArgs, navOptions, navExtras)
    }
}