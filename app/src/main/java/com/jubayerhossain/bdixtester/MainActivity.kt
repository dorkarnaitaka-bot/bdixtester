package com.jubayerhossain.bdixtester

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jubayerhossain.bdixtester.core.util.LocaleHelper
import com.jubayerhossain.bdixtester.navigation.BdixTesterAppRoot
import com.jubayerhossain.bdixtester.ui.theme.BdixTesterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as BdixtesterApplication).appContainer
        setContent {
            val themePreference by appContainer.themePreference.collectAsStateWithLifecycle()
            val appLanguage by appContainer.appLanguage.collectAsStateWithLifecycle()
            val baseContext = LocalContext.current
            val localizedContext = remember(baseContext, appLanguage) {
                LocaleHelper.localizedContext(baseContext, appLanguage)
            }
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalActivityResultRegistryOwner provides this@MainActivity,
                LocalOnBackPressedDispatcherOwner provides this@MainActivity
            ) {
                BdixTesterTheme(themePreference = themePreference) {
                    BdixTesterAppRoot(appContainer = appContainer)
                }
            }
        }
    }
}
