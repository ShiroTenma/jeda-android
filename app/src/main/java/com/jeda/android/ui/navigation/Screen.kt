package com.jeda.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Shield : Screen("shield", "Shield", Icons.Rounded.Home)
    data object Rules : Screen("rules", "Rules", Icons.Rounded.List)
    data object Settings : Screen("settings", "Settings", Icons.Rounded.Settings)
}
