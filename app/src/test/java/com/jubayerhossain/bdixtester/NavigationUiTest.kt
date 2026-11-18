package com.jubayerhossain.bdixtester

import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.jubayerhossain.bdixtester.data.catalog.BdOttPlatforms
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NavigationUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeCleanupBottomNavOttDirectoryAndModernSettingsWorkEndToEnd() {
        composeTestRule.waitForIdle()

        // Home screen displays Hero Ring & Start Test; permanently deleted blocks must NOT exist
        composeTestRule.onNodeWithTag("dashboard_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_menu_button").assertDoesNotExist()
        composeTestRule.onNodeWithTag("home_scan_mode_selector").assertDoesNotExist()
        composeTestRule.onNodeWithTag("home_network_summary_card").assertDoesNotExist()
        composeTestRule.onNodeWithTag("home_tester_ring").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_start_test_button").assertIsDisplayed()

        // Bottom Navigation Bar displays all 5 primary destinations
        composeTestRule.onNodeWithTag("main_bottom_navigation_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_home").assertIsDisplayed().assertIsSelected()
        composeTestRule.onNodeWithTag("nav_servers").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_saved").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_history").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_settings").assertIsDisplayed()

        // Navigate to Servers via Bottom Navigation
        composeTestRule.onNodeWithTag("nav_servers").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_servers").assertIsSelected()
        composeTestRule.onNodeWithTag("servers_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("servers_tab_working").assertIsDisplayed()
        composeTestRule.onNodeWithTag("servers_tab_not_tested").assertIsDisplayed()
        composeTestRule.onNodeWithTag("servers_tab_not_working").assertIsDisplayed()
        composeTestRule.onNodeWithTag("servers_category_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("servers_category_ott").assertIsDisplayed()

        // Switch to curated Bangladesh OTT Directory (26 verified platforms)
        assertEquals(26, BdOttPlatforms.all.size)
        assertEquals(17, BdOttPlatforms.bangladeshCount)
        assertEquals(9, BdOttPlatforms.globalCount)
        assertTrue(BdOttPlatforms.all.all { it.url.startsWith("https://") })

        composeTestRule.onNodeWithTag("servers_category_ott").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("ott_region_filter_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ott_directory_list").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ott_card_ott_chorki").assertIsDisplayed()

        // Filter to Global in BD OTTs
        composeTestRule.onNodeWithTag("ott_region_global_in_bd").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("ott_card_ott_netflix").assertIsDisplayed()

        // Return to All BDIX servers & toggle Search
        composeTestRule.onNodeWithTag("servers_category_all").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("servers_search_toggle").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("servers_search_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("servers_search_toggle").performClick()
        composeTestRule.waitForIdle()

        // Open Add Custom Server dialog from Servers top bar and save
        composeTestRule.onNodeWithTag("servers_add_custom_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("custom_server_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("custom_server_name_input").performTextInput("My BDIX Node")
        composeTestRule.onNodeWithTag("custom_server_url_input").performTextInput("http://10.16.100.244")
        composeTestRule.onNodeWithTag("custom_server_save_button").performClick()
        composeTestRule.waitForIdle()

        // Navigate to Saved tab
        composeTestRule.onNodeWithTag("nav_saved").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_saved").assertIsSelected()
        composeTestRule.onNodeWithTag("favorites_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("favorites_empty_state").assertIsDisplayed()

        // Navigate to History tab
        composeTestRule.onNodeWithTag("nav_history").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_history").assertIsSelected()
        composeTestRule.onNodeWithTag("history_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("history_empty_state").assertIsDisplayed()

        // Navigate to Settings tab and test redesigned M3 grouped rows + modal pickers
        composeTestRule.onNodeWithTag("nav_settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_settings").assertIsSelected()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_theme_row").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_language_row").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_scan_mode_row").performScrollTo().assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("scan_mode_option_full").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        // Return to Home tab
        composeTestRule.onNodeWithTag("nav_home").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_home").assertIsSelected()
        composeTestRule.onNodeWithTag("dashboard_screen").assertIsDisplayed()
    }

    @Test
    fun tappingServerOrOttCardLaunchesExternalBrowserDirectlyWithoutMetadataPage() {
        val activity = composeTestRule.activity
        val browserComponent = ComponentName("com.android.chrome", "com.google.android.apps.chrome.Main")
        val shadowPm = shadowOf(activity.packageManager)
        shadowPm.addActivityIfNotPresent(browserComponent)
        val filter = IntentFilter(Intent.ACTION_VIEW).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addCategory(Intent.CATEGORY_BROWSABLE)
            addDataScheme("http")
            addDataScheme("https")
        }
        shadowPm.addIntentFilterForActivity(browserComponent, filter)

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_view_servers_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("servers_screen").assertIsDisplayed()

        // Built-in entry #0 ("http://ftpbd.net") under Not Tested opens external browser directly
        composeTestRule.onNodeWithTag("servers_tab_not_tested").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("server_url_link_builtin:0").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        // Server Metadata / Details page is permanently removed everywhere
        composeTestRule.onNodeWithTag("server_details_screen").assertDoesNotExist()
        composeTestRule.onNodeWithTag("servers_screen").assertIsDisplayed()

        val firstIntent = shadowOf(activity).nextStartedActivity
        assertNotNull(firstIntent)
        assertEquals(Intent.ACTION_VIEW, firstIntent.action)
        assertEquals(Uri.parse("http://ftpbd.net"), firstIntent.data)
        assertTrue(firstIntent.categories.contains(Intent.CATEGORY_BROWSABLE))

        // Tapping an OTT card also launches external browser directly
        composeTestRule.onNodeWithTag("servers_category_ott").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("ott_card_ott_chorki").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("server_details_screen").assertDoesNotExist()
        val ottIntent = shadowOf(activity).nextStartedActivity
        assertNotNull(ottIntent)
        assertEquals(Intent.ACTION_VIEW, ottIntent.action)
        assertEquals(Uri.parse("https://chorki.net"), ottIntent.data)
        assertTrue(ottIntent.categories.contains(Intent.CATEGORY_BROWSABLE))
    }
}
