package com.example

import android.app.Application
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.example.ui.VoiceScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.voice.VoiceViewModel
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun app_main_screen_screenshot() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = VoiceViewModel(application)

    composeTestRule.setContent {
      MyApplicationTheme {
        VoiceScreen(
          viewModel = viewModel,
          innerPadding = PaddingValues()
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/voice_commander_main.png")
  }
}
