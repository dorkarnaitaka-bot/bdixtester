package com.jubayerhossain.bdixtester

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NavigationUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bottomNavigationTraversesAllPrimaryScreensAndCustomServerFlow() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dashboard_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("catalog_audit_banner").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_servers").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("servers_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("servers_add_custom_fab").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("custom_server_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("custom_server_name_input").performTextInput("My BDIX Node")
        composeTestRule.onNodeWithTag("custom_server_url_input").performTextInput("http://10.16.100.244")
        composeTestRule.onNodeWithTag("custom_server_save_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("nav_scan").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("scan_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("scan_start_button").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_favorites").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("favorites_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("favorites_empty_state").assertIsDisplayed()

        composeTestRule.onNodeWithTag("tab_history").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("history_empty_state").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_theme_selector").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_language_selector").performScrollTo().assertIsDisplayed()
    }
}
