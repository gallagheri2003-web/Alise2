package com.example.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.AutoCodeSnippetEntity
import com.example.data.local.entities.CodeTemplateEntity
import com.example.data.local.entities.ConversationMessageEntity
import com.example.data.local.entities.DirectiveEntity
import com.example.data.local.entities.ProtocolEntity
import com.example.data.local.entities.SystemLogEntity
import com.example.data.local.entities.ThreatIncidentEntity
import com.example.data.local.entities.VoiceNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DirectiveDao {
    @Query("SELECT * FROM directives ORDER BY priority DESC, timestamp DESC")
    fun getAllDirectives(): Flow<List<DirectiveEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDirective(directive: DirectiveEntity): Long

    @Update
    suspend fun updateDirective(directive: DirectiveEntity)

    @Delete
    suspend fun deleteDirective(directive: DirectiveEntity)
}

@Dao
interface ProtocolDao {
    @Query("SELECT * FROM protocols ORDER BY id ASC")
    fun getAllProtocols(): Flow<List<ProtocolEntity>>

    @Query("SELECT * FROM protocols WHERE codeName = :codeName LIMIT 1")
    suspend fun getProtocolByCodeName(codeName: String): ProtocolEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProtocol(protocol: ProtocolEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProtocols(protocols: List<ProtocolEntity>)

    @Update
    suspend fun updateProtocol(protocol: ProtocolEntity)

    @Delete
    suspend fun deleteProtocol(protocol: ProtocolEntity)
}

@Dao
interface SystemLogDao {
    @Query("SELECT * FROM system_logs ORDER BY timestamp DESC LIMIT 200")
    fun getRecentLogs(): Flow<List<SystemLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SystemLogEntity): Long

    @Query("DELETE FROM system_logs")
    suspend fun clearAllLogs()
}

@Dao
interface VoiceNoteDao {
    @Query("SELECT * FROM voice_notes ORDER BY timestamp DESC")
    fun getAllVoiceNotes(): Flow<List<VoiceNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceNote(note: VoiceNoteEntity): Long

    @Delete
    suspend fun deleteVoiceNote(note: VoiceNoteEntity)
}

@Dao
interface ThreatIncidentDao {
    @Query("SELECT * FROM threat_incidents ORDER BY timestamp DESC")
    fun getAllThreatIncidents(): Flow<List<ThreatIncidentEntity>>

    @Query("SELECT * FROM threat_incidents WHERE category = :category ORDER BY timestamp DESC")
    fun getThreatsByCategory(category: String): Flow<List<ThreatIncidentEntity>>

    @Query("SELECT * FROM threat_incidents WHERE isResolved = 0 ORDER BY timestamp DESC")
    fun getActiveThreats(): Flow<List<ThreatIncidentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreatIncident(incident: ThreatIncidentEntity): Long

    @Update
    suspend fun updateThreatIncident(incident: ThreatIncidentEntity)

    @Delete
    suspend fun deleteThreatIncident(incident: ThreatIncidentEntity)

    @Query("DELETE FROM threat_incidents")
    suspend fun clearAllThreats()
}

@Dao
interface AutoCodeSnippetDao {
    @Query("SELECT * FROM auto_code_snippets ORDER BY timestamp DESC")
    fun getAllSnippets(): Flow<List<AutoCodeSnippetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippet(snippet: AutoCodeSnippetEntity): Long

    @Delete
    suspend fun deleteSnippet(snippet: AutoCodeSnippetEntity)

    @Query("DELETE FROM auto_code_snippets")
    suspend fun clearAllSnippets()
}

@Dao
interface CodeTemplateDao {
    @Query("SELECT * FROM code_templates ORDER BY isCustom DESC, category ASC, title ASC")
    fun getAllTemplates(): Flow<List<CodeTemplateEntity>>

    @Query("SELECT * FROM code_templates WHERE category = :category ORDER BY title ASC")
    fun getTemplatesByCategory(category: String): Flow<List<CodeTemplateEntity>>

    @Query("SELECT COUNT(*) FROM code_templates")
    suspend fun getTemplateCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: CodeTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<CodeTemplateEntity>)

    @Update
    suspend fun updateTemplate(template: CodeTemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: CodeTemplateEntity)

    @Query("DELETE FROM code_templates WHERE isCustom = 1")
    suspend fun clearCustomTemplates()
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversation_history ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ConversationMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ConversationMessageEntity): Long

    @Query("DELETE FROM conversation_history")
    suspend fun clearAllMessages()

    @Query("SELECT COUNT(*) FROM conversation_history")
    suspend fun getMessageCount(): Int
}


