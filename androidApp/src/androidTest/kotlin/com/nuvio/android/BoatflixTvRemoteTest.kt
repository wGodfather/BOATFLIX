package com.nuvio.android

import android.content.res.Configuration
import android.graphics.Bitmap
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nuvio.app.BoatflixTvActivity
import com.nuvio.app.core.ui.NuvioTheme
import com.nuvio.app.features.library.LibraryScreen
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Runs on a TV system image and sends real remote key events, without taps. */
@RunWith(AndroidJUnit4::class)
class BoatflixTvRemoteTest {
    @get:Rule val compose = createAndroidComposeRule<BoatflixTvActivity>()

    @Test fun tvLauncherAndLibraryTabsAcceptRemoteInput() {
        assertEquals(Configuration.UI_MODE_TYPE_TELEVISION,
            compose.activity.resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK)
        // The Android test harness starts in touch mode; a real TV remote uses key mode.
        InstrumentationRegistry.getInstrumentation().setInTouchMode(false)
        compose.activityRule.scenario.onActivity { activity ->
            activity.setContent { NuvioTheme { LibraryScreen() } }
        }
        compose.waitForIdle()
        compose.onNodeWithText("Saved").performSemanticsAction(SemanticsActions.RequestFocus)
        compose.onNodeWithText("Saved").assertIsFocused()
        remote(KeyEvent.KEYCODE_DPAD_RIGHT)
        compose.onNodeWithText("Cloud").assertIsFocused()
        remote(KeyEvent.KEYCODE_DPAD_RIGHT)
        compose.onNodeWithText("Downloads").assertIsFocused()
        remote(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.onNodeWithText("No downloads yet").assertIsDisplayed()
        remote(KeyEvent.KEYCODE_DPAD_LEFT)
        compose.onNodeWithText("Cloud").assertIsFocused()
        remote(KeyEvent.KEYCODE_DPAD_CENTER)
        remote(KeyEvent.KEYCODE_DPAD_RIGHT)
        remote(KeyEvent.KEYCODE_DPAD_CENTER)
        compose.onNodeWithText("No downloads yet").assertIsDisplayed()
    }

    @After fun captureTvScreen() {
        val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        val destination = File(compose.activity.getExternalFilesDir(null), "fork-ui-qa/tv-remote-downloads.png")
        destination.parentFile!!.mkdirs()
        destination.outputStream().use { screenshot.compress(Bitmap.CompressFormat.PNG, 100, it) }
        screenshot.recycle()
    }

    private fun remote(code: Int) {
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(code)
        compose.waitForIdle()
    }
}
