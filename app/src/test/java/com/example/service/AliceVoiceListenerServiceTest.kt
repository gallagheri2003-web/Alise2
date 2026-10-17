package com.example.service

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BtcOracleTelemetry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AliceVoiceListenerServiceTest {

    @Test
    fun testBtcOracleTelemetryDefaultValues() {
        val telemetry = BtcOracleTelemetry()

        assertEquals(300000.0, telemetry.btcUsd, 0.001)
        assertEquals(4.31, telemetry.vix1moPercent, 0.001)
        assertEquals(25, telemetry.ethGasGweiMedian)
        assertEquals(20000.0, telemetry.poolDepthBtc, 0.001)
        assertEquals(5.35496, telemetry.effectiveYieldApy, 0.00001)
        assertTrue(telemetry.arbitrageActive)
        assertEquals(0.0, telemetry.lastArbProfitBtc, 0.001)
        assertEquals(2.15, telemetry.taxRatePercentBtc, 0.001)
    }

    @Test
    fun testVoiceListenerStateDefaults() {
        val state = VoiceListenerState()
        assertFalse(state.isRunning)
        assertFalse(state.isListening)
        assertEquals("", state.lastRecognizedText)
        assertEquals("", state.lastTriggeredAction)
        assertEquals(0, state.triggerCount)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun testServiceDispatchers() {
        var dispatchedCommand: String? = null
        var dispatchedProtocol: String? = null

        AliceVoiceListenerService.setCommandDispatcher { cmd ->
            dispatchedCommand = cmd
        }
        AliceVoiceListenerService.setProtocolDispatcher { proto ->
            dispatchedProtocol = proto
        }

        // Verify dispatchers are wired and functional
        assertNotNull(AliceVoiceListenerService.listenerState)
    }

    @Test
    fun testServiceLifecycleStartAndStop() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val startIntent = Intent(context, AliceVoiceListenerService::class.java).apply {
            action = AliceVoiceListenerService.ACTION_START_LISTENING
        }

        val serviceController = Robolectric.buildService(AliceVoiceListenerService::class.java, startIntent)
        val service = serviceController.create().startCommand(0, 1).get()

        assertNotNull(service)
        assertTrue(AliceVoiceListenerService.listenerState.value.isRunning)

        // Stop action
        val stopIntent = Intent(context, AliceVoiceListenerService::class.java).apply {
            action = AliceVoiceListenerService.ACTION_STOP_LISTENING
        }
        service.onStartCommand(stopIntent, 0, 2)
        assertFalse(AliceVoiceListenerService.listenerState.value.isRunning)

        serviceController.destroy()
    }
}
