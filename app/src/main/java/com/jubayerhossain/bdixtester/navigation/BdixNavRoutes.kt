package com.jubayerhossain.bdixtester.navigation

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.jubayerhossain.bdixtester.R

object BdixRoutes {
    const val HOME = "dashboard"
    const val SERVERS = "servers"
    const val SCAN = "scan"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
    const val SERVER_DETAILS = "server_details/{serverId}"

    fun serverDetailsRoute(serverId: String): String {
        return "server_details/${Uri.encode(serverId)}"
    }
}

enum class TopLevelDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME(
        route = BdixRoutes.HOME,
        labelRes = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_home"
    ),
    SERVERS(
        route = BdixRoutes.SERVERS,
        labelRes = R.string.nav_servers,
        selectedIcon = Icons.Filled.Dns,
        unselectedIcon = Icons.Outlined.Dns,
        testTag = "nav_servers"
    ),
    SCAN(
        route = BdixRoutes.SCAN,
        labelRes = R.string.nav_scan,
        selectedIcon = Icons.Filled.Radar,
        unselectedIcon = Icons.Outlined.Radar,
        testTag = "nav_scan"
    ),
    FAVORITES(
        route = BdixRoutes.FAVORITES,
        labelRes = R.string.nav_favorites,
        selectedIcon = Icons.Filled.Bookmark,
        unselectedIcon = Icons.Outlined.BookmarkBorder,
        testTag = "nav_favorites"
    ),
    SETTINGS(
        route = BdixRoutes.SETTINGS,
        labelRes = R.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_settings"
    )
}
