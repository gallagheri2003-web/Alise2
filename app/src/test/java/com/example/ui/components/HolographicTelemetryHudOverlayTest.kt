package com.example.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.hardware.DeviceTelemetry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HolographicTelemetryHudOverlayTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testPersistentHudDockRendersTelemetry() {
        val testTelemetry = DeviceTelemetry(
            cpuLoadPercent = 42,
            batteryLevel = 85,
            isCharging = false,
            coreTemperature = 35.5f
        )

        var expandedToggled = false

        composeTestRule.setContent {
            HolographicTelemetryHudOverlay(
                telemetry = testTelemetry,
                isPersistent = true,
                isExpanded = false,
                onToggleExpand = { expandedToggled = true }
            )
        }

        composeTestRule.onNodeWithTag("persistent_hud_dock_bar").assertExists()
        composeTestRule.onNodeWithTag("persistent_hud_expand_btn").performClick()
        assertTrue(expandedToggled)
    }

    @Test
    fun testExpandedTacticalHudMatrixRenders() {
        val testTelemetry = DeviceTelemetry(
            cpuLoadPercent = 65,
            batteryLevel = 92,
            isCharging = true,
            coreTemperature = 38.0f
        )

        composeTestRule.setContent {
            HolographicTelemetryHudOverlay(
                telemetry = testTelemetry,
                isPersistent = true,
                isExpanded = true
            )
        }

        composeTestRule.onNodeWithTag("expanded_tactical_hud_matrix").assertExists()
    }
}
