package com.example.android.politicalpreparedness.utils

import androidx.navigation.NavDirections

/**
 * @DrStart: NavigationCommand.kt - NavigationCommand sealed class to manage navigation with LiveData
 */
sealed class NavigationCommand {
    //  navigate to a destination
    data class To(val directions: NavDirections) : NavigationCommand()

    //    navigate back to previous destination
    object Back : NavigationCommand()

    //    navigate back to a specific destination
    data class BackTo(val destinationId: Int) : NavigationCommand()
}
