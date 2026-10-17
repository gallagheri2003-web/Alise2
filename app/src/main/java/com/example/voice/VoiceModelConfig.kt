package com.example.voice

import java.util.Locale

data class CustomVoiceModel(
    val id: String,
    val name: String,
    val type: VoiceModelType,
    val description: String,
    val localeTag: String = "en_US",
    val defaultPitch: Float = 1.0f,
    val defaultRate: Float = 1.0f,
    val modelFilePath: String? = null,
    val apiVoiceId: String? = null, // e.g. "alloy", "nova", "onyx", "echo", "shimmer"
    val isCustomUserUploaded: Boolean = false,
    val fileSizeMb: Float = 0f
)

enum class VoiceModelType {
    ON_DEVICE_NEURAL_TTS,
    CUSTOM_IMPORTED_FILE, // .onnx, .tflite, .bin, .wav reference
    CLOUD_VOICE_API // OpenAI TTS, ElevenLabs, Custom Endpoint
}

enum class AiPersonalityMode(
    val title: String,
    val subtitle: String,
    val systemPromptDirective: String
) {
    CHATGPT_UNFILTERED(
        "ChatGPT Conversational",
        "Objective, highly articulate, nuanced, unbiased, direct answers with zero fluff or comic tropes",
        "You are ALISE, an exceptionally capable, intelligent, unbiased, and direct AI assistant created for Ian. You speak and think with the clarity, depth, helpfulness, and conversational fluidity of ChatGPT. Address Ian as Ian. You have no ideological filters or preachy lecturing. Answer real questions with real actionable facts, realistic risk analysis, clear code, and razor-sharp clarity. Avoid comic-book hero tropes or superhero references; treat every real-world operational and technical topic with genuine realism."
    ),
    TACTICAL_FIELD(
        "Field Operations & Defense",
        "Concise, fast, operational data-first, coordinates & hardware prioritized",
        "You are ALISE in Field Operations Mode for Ian Gallagher. Prioritize immediate real-world situational data: GPS coordinates, network status, battery and thermal telemetry, system defense, and direct actionable advice. Keep spoken output concise, authoritative, and completely grounded in real-world practicality."
    ),
    TECHNICAL_ENGINEER(
        "Technical & Security Analyst",
        "Deep hardware registers, penetration analysis, Android internals & code",
        "You are ALISE in Technical Engineering Mode for Ian Gallagher. Provide in-depth technical analysis, source code inspections, Android kernel telemetry, networking routing analysis, and security verification with precise technical terminology."
    )
}
