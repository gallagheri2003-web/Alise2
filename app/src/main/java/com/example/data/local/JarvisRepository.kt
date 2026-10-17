package com.example.data.local

import com.example.data.local.daos.AutoCodeSnippetDao
import com.example.data.local.daos.CodeTemplateDao
import com.example.data.local.daos.ConversationDao
import com.example.data.local.daos.DirectiveDao
import com.example.data.local.daos.ProtocolDao
import com.example.data.local.daos.SystemLogDao
import com.example.data.local.daos.ThreatIncidentDao
import com.example.data.local.daos.VoiceNoteDao
import com.example.data.local.entities.AutoCodeSnippetEntity
import com.example.data.local.entities.CodeTemplateEntity
import com.example.data.local.entities.ConversationMessageEntity
import com.example.data.local.entities.DirectiveEntity
import com.example.data.local.entities.ProtocolEntity
import com.example.data.local.entities.SystemLogEntity
import com.example.data.local.entities.ThreatIncidentEntity
import com.example.data.local.entities.VoiceNoteEntity
import kotlinx.coroutines.flow.Flow

class JarvisRepository(
    private val directiveDao: DirectiveDao,
    private val protocolDao: ProtocolDao,
    private val systemLogDao: SystemLogDao,
    private val voiceNoteDao: VoiceNoteDao,
    private val threatIncidentDao: ThreatIncidentDao,
    private val autoCodeSnippetDao: AutoCodeSnippetDao,
    private val codeTemplateDao: CodeTemplateDao,
    private val conversationDao: ConversationDao
) {
    val allDirectives: Flow<List<DirectiveEntity>> = directiveDao.getAllDirectives()
    val allProtocols: Flow<List<ProtocolEntity>> = protocolDao.getAllProtocols()
    val recentLogs: Flow<List<SystemLogEntity>> = systemLogDao.getRecentLogs()
    val allVoiceNotes: Flow<List<VoiceNoteEntity>> = voiceNoteDao.getAllVoiceNotes()
    val allThreatIncidents: Flow<List<ThreatIncidentEntity>> = threatIncidentDao.getAllThreatIncidents()
    val activeThreats: Flow<List<ThreatIncidentEntity>> = threatIncidentDao.getActiveThreats()
    val allSavedSnippets: Flow<List<AutoCodeSnippetEntity>> = autoCodeSnippetDao.getAllSnippets()
    val allCodeTemplates: Flow<List<CodeTemplateEntity>> = codeTemplateDao.getAllTemplates()
    val conversationMessages: Flow<List<ConversationMessageEntity>> = conversationDao.getAllMessages()

    suspend fun saveConversationMessage(message: ConversationMessageEntity) = conversationDao.insertMessage(message)
    suspend fun clearConversationHistory() = conversationDao.clearAllMessages()

    suspend fun addDirective(directive: DirectiveEntity) = directiveDao.insertDirective(directive)
    suspend fun updateDirective(directive: DirectiveEntity) = directiveDao.updateDirective(directive)
    suspend fun deleteDirective(directive: DirectiveEntity) = directiveDao.deleteDirective(directive)

    suspend fun addProtocol(protocol: ProtocolEntity) = protocolDao.insertProtocol(protocol)
    suspend fun updateProtocol(protocol: ProtocolEntity) = protocolDao.updateProtocol(protocol)
    suspend fun deleteProtocol(protocol: ProtocolEntity) = protocolDao.deleteProtocol(protocol)
    suspend fun getProtocolByCode(codeName: String) = protocolDao.getProtocolByCodeName(codeName)

    suspend fun logSystemEvent(tag: String, message: String, level: String = "INFO") {
        systemLogDao.insertLog(SystemLogEntity(tag = tag, message = message, level = level))
    }

    suspend fun clearLogs() = systemLogDao.clearAllLogs()

    suspend fun saveVoiceNote(note: VoiceNoteEntity) = voiceNoteDao.insertVoiceNote(note)
    suspend fun deleteVoiceNote(note: VoiceNoteEntity) = voiceNoteDao.deleteVoiceNote(note)

    suspend fun logThreatIncident(incident: ThreatIncidentEntity) = threatIncidentDao.insertThreatIncident(incident)
    suspend fun updateThreatIncident(incident: ThreatIncidentEntity) = threatIncidentDao.updateThreatIncident(incident)
    suspend fun deleteThreatIncident(incident: ThreatIncidentEntity) = threatIncidentDao.deleteThreatIncident(incident)
    suspend fun clearAllThreats() = threatIncidentDao.clearAllThreats()

    suspend fun saveCodeSnippet(snippet: AutoCodeSnippetEntity) = autoCodeSnippetDao.insertSnippet(snippet)
    suspend fun deleteCodeSnippet(snippet: AutoCodeSnippetEntity) = autoCodeSnippetDao.deleteSnippet(snippet)
    suspend fun clearAllSnippets() = autoCodeSnippetDao.clearAllSnippets()

    suspend fun saveCodeTemplate(template: CodeTemplateEntity) = codeTemplateDao.insertTemplate(template)
    suspend fun updateCodeTemplate(template: CodeTemplateEntity) = codeTemplateDao.updateTemplate(template)
    suspend fun deleteCodeTemplate(template: CodeTemplateEntity) = codeTemplateDao.deleteTemplate(template)
    suspend fun ensureDefaultTemplates() {
        if (codeTemplateDao.getTemplateCount() == 0) {
            codeTemplateDao.insertTemplates(BuiltinPythonTemplates.defaultTemplates)
        }
    }
}

