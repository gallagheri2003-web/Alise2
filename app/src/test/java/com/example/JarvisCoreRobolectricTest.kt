package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.JarvisDatabase
import com.example.data.local.entities.DirectiveEntity
import com.example.data.local.entities.ProtocolEntity
import com.example.data.local.entities.SystemLogEntity
import com.example.data.local.entities.VoiceNoteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class JarvisCoreRobolectricTest {

    private lateinit var db: JarvisDatabase
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, JarvisDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testDirectivePersistence() = runBlocking {
        val directive = DirectiveEntity(
            title = "Calibrate Repulsors",
            content = "Ensure energy balance is maintained across all thrusters.",
            priority = "HIGH",
            isActive = true
        )
        db.directiveDao().insertDirective(directive)

        val list = db.directiveDao().getAllDirectives().first()
        assertEquals(1, list.size)
        assertEquals("Calibrate Repulsors", list[0].title)
        assertEquals("HIGH", list[0].priority)

        // Toggle directive
        db.directiveDao().updateDirective(list[0].copy(isActive = false))
        val updatedList = db.directiveDao().getAllDirectives().first()
        assertEquals(1, updatedList.size)
        assertEquals(false, updatedList[0].isActive)
    }

    @Test
    fun testProtocolPersistence() = runBlocking {
        val protocol = ProtocolEntity(
            codeName = "STEALTH_ENGAGE",
            displayName = "Stealth Protocol",
            description = "Silent all emissions and dim HUD.",
            actionsJson = "[\"MUTE_MEDIA\", \"DIM_LIGHTS\"]",
            category = "TACTICAL",
            isPreset = true
        )
        db.protocolDao().insertProtocol(protocol)

        val protocols = db.protocolDao().getAllProtocols().first()
        assertEquals(1, protocols.size)
        assertEquals("STEALTH_ENGAGE", protocols[0].codeName)
    }

    @Test
    fun testVoiceNoteVaultPersistence() = runBlocking {
        val note = VoiceNoteEntity(
            title = "Operator Flight Log 01",
            transcript = "Mark-LVII atmospheric tests operating within nominal parameters.",
            aiSummary = "Nominal test"
        )
        db.voiceNoteDao().insertVoiceNote(note)

        val notes = db.voiceNoteDao().getAllVoiceNotes().first()
        assertEquals(1, notes.size)
        assertEquals("Operator Flight Log 01", notes[0].title)
    }

    @Test
    fun testSystemAuditTrailLogging() = runBlocking {
        val log = SystemLogEntity(
            tag = "HARDWARE",
            message = "Kinematic sensor bus calibrated successfully.",
            level = "INFO"
        )
        db.systemLogDao().insertLog(log)

        val logs = db.systemLogDao().getRecentLogs().first()
        assertEquals(1, logs.size)
        assertEquals("HARDWARE", logs[0].tag)
    }
}
