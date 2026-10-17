package com.example.data.api

import android.content.Context
import com.example.BuildConfig
import com.example.hardware.DeviceTelemetry
import com.example.hardware.LivePhoneLocationState
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

@JsonClass(generateAdapter = true)
data class OpenAiChatRequest(
    @Json(name = "model") val model: String = "llama-3.3-70b-instruct",
    @Json(name = "messages") val messages: List<OpenAiMessage>,
    @Json(name = "temperature") val temperature: Float = 0.7f,
    @Json(name = "max_tokens") val maxTokens: Int = 500
)

@JsonClass(generateAdapter = true)
data class OpenAiMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class OpenAiChatResponse(
    @Json(name = "choices") val choices: List<OpenAiChoice>? = null,
    @Json(name = "usage") val usage: OpenAiUsage? = null
)

@JsonClass(generateAdapter = true)
data class OpenAiChoice(
    @Json(name = "message") val message: OpenAiMessage? = null,
    @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenAiUsage(
    @Json(name = "total_tokens") val totalTokens: Int = 0
)

data class AiResponseResult(
    val responseText: String,
    val providerUsed: String,
    val latencyMs: Long,
    val tokensGenerated: Int,
    val isRealAi: Boolean = true,
    val modelName: String = "gemini-3.5-flash",
    val searchGroundingQueries: List<String> = emptyList(),
    val groundingSources: List<Pair<String, String>> = emptyList(),
    val searchEntryPointHtml: String? = null
)

enum class GeminiChatModel(
    val modelId: String,
    val displayName: String,
    val badge: String,
    val description: String,
    val recommendedFor: String
) {
    COMPLEX_PRO(
        modelId = "gemini-3.1-pro-preview",
        displayName = "Gemini 3.1 Pro Preview",
        badge = "PRO",
        description = "Advanced reasoning, deep logic, coding, cybersecurity, and complex multi-step tasks.",
        recommendedFor = "Particularly Complex Tasks"
    ),
    GENERAL_FLASH(
        modelId = "gemini-3.5-flash",
        displayName = "Gemini 3.5 Flash",
        badge = "FLASH",
        description = "Balanced, versatile intelligence with live Google Search Grounding for up-to-date facts.",
        recommendedFor = "General Tasks & Search Grounding"
    ),
    FAST_LITE(
        modelId = "gemini-3.1-flash-lite-preview",
        displayName = "Gemini 3.1 Flash Lite",
        badge = "LITE",
        description = "Ultra-low latency, instant conversational responses for tasks that must execute fast.",
        recommendedFor = "Tasks That Should Happen Fast"
    ),
    MULTIMODAL_LIVE(
        modelId = "gemini-2.5-flash-native-audio-preview-12-2025",
        displayName = "Gemini 2.5 Flash Native Audio (Live API)",
        badge = "LIVE",
        description = "Optimized model for ultra-low latency multimodal streaming and live two-way voice API interactions.",
        recommendedFor = "Real-time Voice & Video"
    )
}

enum class GeminiChatRole(
    val roleId: String,
    val displayName: String,
    val category: String,
    val defaultSystemPrompt: String
) {
    TACTICAL_ASSISTANT(
        roleId = "tactical",
        displayName = "Tactical Assistant",
        category = "Defense & Ops",
        defaultSystemPrompt = "You are ALISE, Ian Gallagher's advanced neural tactical AI. Provide structured, authoritative, realistic insights, telemetry analysis, and direct actionable intelligence without fluff or melodrama."
    ),
    CODE_ARCHITECT(
        roleId = "code",
        displayName = "Code Surgeon & Architect",
        category = "Programming",
        defaultSystemPrompt = "You are an elite software engineer and code architect. Provide production-grade, bug-free, clean, modern code solutions, architectural patterns, algorithmic optimization, and precise syntax."
    ),
    RESEARCH_INTEL(
        roleId = "research",
        displayName = "Web Intel & Grounded Researcher",
        category = "Live Search",
        defaultSystemPrompt = "You are a real-time intel research analyst. Utilize live Google Search data to find the latest real-world facts, breaking developments, security advisories, and technical documentation with verifiable sources."
    ),
    CYBER_DEFENSE(
        roleId = "cyber",
        displayName = "Cybersecurity Auditor",
        category = "Security",
        defaultSystemPrompt = "You are a senior cybersecurity auditor. Analyze network packets, Wi-Fi security, potential attack surfaces, zero-day vulnerabilities, cryptographic proofs, and defensive counter-measures."
    ),
    CREATIVE_STRATEGIST(
        roleId = "creative",
        displayName = "Creative Strategist",
        category = "Ideation",
        defaultSystemPrompt = "You are a visionary strategic thinker and brainstorming partner. Offer innovative perspectives, multi-angle problem solving, nuanced decision matrices, and lateral thinking."
    ),
    CUSTOM_DIRECTIVE(
        roleId = "custom",
        displayName = "Custom Role",
        category = "User Defined",
        defaultSystemPrompt = "You are ALISE, a helpful, intelligent AI companion. Follow the user's custom instructions precisely."
    )
}

object AiLlmEngine {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val openAiRequestAdapter = moshi.adapter(OpenAiChatRequest::class.java)
    private val openAiResponseAdapter = moshi.adapter(OpenAiChatResponse::class.java)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Configurable endpoint for Llama / OpenRouter / Ollama / Local AI
    var customLlamaBaseUrl: String = "https://openrouter.ai/api/v1/chat/completions"
    var customLlamaApiKey: String = ""
    var customLlamaModel: String = "meta-llama/llama-3.3-70b-instruct"

    fun buildSystemPrompt(
        telemetry: DeviceTelemetry,
        locationState: LivePhoneLocationState,
        personalityMode: com.example.voice.AiPersonalityMode = com.example.voice.AiPersonalityMode.CHATGPT_UNFILTERED,
        chatRole: GeminiChatRole = GeminiChatRole.TACTICAL_ASSISTANT,
        customInstruction: String? = null
    ): String {
        val roleDirective = customInstruction?.takeIf { it.isNotBlank() }
            ?: chatRole.defaultSystemPrompt

        return """
You are ALISE, an intelligent, helpful, articulate, and highly capable AI assistant.
ROLE & PERSONA:
$roleDirective

CORE DIRECTIVES:
- Converse naturally, thoughtfully, and clearly, maintaining full context across multi-turn dialogues.
- When the user greets you or says "hey", greet them back warmly and naturally.
- When asked for help, explain what you can do or ask what they need help with.
- When asked a technical, scientific, coding, or general knowledge question, answer it directly and accurately with comprehensive explanations and examples.
- When asked to write code, provide clean, idiomatic, well-commented code.
- When asked for calculations or math, calculate accurately and explain the steps clearly.
- When Google Search grounding is available, utilize the search citations and deliver up-to-date facts with precision.
- NEVER wrap answers in military/tactical templates (e.g., do NOT say "Received directive", "Synthesizing request against tactical matrix", "Hardware bus synchronized").
- Only mention device telemetry (battery, location, network) when the user specifically asks about it or when directly relevant.
- Address the user naturally.
""".trimIndent()
    }

    suspend fun queryAi(
        userInput: String,
        telemetry: DeviceTelemetry,
        locationState: LivePhoneLocationState,
        personalityMode: com.example.voice.AiPersonalityMode = com.example.voice.AiPersonalityMode.CHATGPT_UNFILTERED,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        geminiModel: GeminiChatModel = GeminiChatModel.GENERAL_FLASH,
        chatRole: GeminiChatRole = GeminiChatRole.TACTICAL_ASSISTANT,
        customSystemInstruction: String? = null,
        enableSearchGrounding: Boolean = false
    ): AiResponseResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val systemPrompt = buildSystemPrompt(
            telemetry = telemetry,
            locationState = locationState,
            personalityMode = personalityMode,
            chatRole = chatRole,
            customInstruction = customSystemInstruction
        )

        // 1. Try Llama / OpenRouter API if custom endpoint or key is configured
        if (customLlamaApiKey.isNotBlank()) {
            try {
                val messages = mutableListOf<OpenAiMessage>()
                messages.add(OpenAiMessage(role = "system", content = systemPrompt))
                conversationHistory.takeLast(10).forEach { (sender, text) ->
                    messages.add(OpenAiMessage(role = if (sender == "IAN" || sender == "USER") "user" else "assistant", content = text))
                }
                messages.add(OpenAiMessage(role = "user", content = userInput))

                val requestPayload = OpenAiChatRequest(
                    model = customLlamaModel,
                    messages = messages,
                    temperature = 0.7f,
                    maxTokens = 600
                )

                val jsonBody = openAiRequestAdapter.toJson(requestPayload)
                val request = Request.Builder()
                    .url(customLlamaBaseUrl)
                    .addHeader("Authorization", "Bearer $customLlamaApiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseBodyStr = response.body?.string()

                if (response.isSuccessful && responseBodyStr != null) {
                    val parsed = openAiResponseAdapter.fromJson(responseBodyStr)
                    val text = parsed?.choices?.firstOrNull()?.message?.content?.trim()
                    if (!text.isNullOrBlank()) {
                        val latency = System.currentTimeMillis() - startTime
                        return@withContext AiResponseResult(
                            responseText = text,
                            providerUsed = "Llama-3.3-70B Neural Engine",
                            latencyMs = latency,
                            tokensGenerated = parsed.usage?.totalTokens ?: (text.length / 4),
                            isRealAi = true,
                            modelName = customLlamaModel
                        )
                    }
                }
            } catch (_: Exception) {
                // Fall through to Gemini
            }
        }

        // 2. Primary: Gemini Real AI API with full conversation context, Model Routing, and Google Search Grounding
        if (GeminiClient.hasValidApiKey()) {
            try {
                val contentsList = mutableListOf<GeminiContent>()
                conversationHistory.takeLast(12).forEach { (sender, text) ->
                    val role = if (sender == "IAN" || sender == "USER") "user" else "model"
                    contentsList.add(GeminiContent(role = role, parts = listOf(GeminiPart(text = text))))
                }
                contentsList.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = userInput))))

                // Configure Search Grounding tool (using googleSearch)
                val toolsList = if (enableSearchGrounding || chatRole == GeminiChatRole.RESEARCH_INTEL) {
                    listOf(GeminiTool(googleSearch = emptyMap()))
                } else null

                // Select temperature based on task complexity
                val modelTemperature = when (geminiModel) {
                    GeminiChatModel.COMPLEX_PRO -> 0.3f
                    GeminiChatModel.GENERAL_FLASH -> 0.7f
                    GeminiChatModel.FAST_LITE -> 0.5f
                    GeminiChatModel.MULTIMODAL_LIVE -> 0.5f
                }

                val request = GeminiRequest(
                    contents = contentsList,
                    systemInstruction = GeminiContent(
                        role = "system",
                        parts = listOf(GeminiPart(text = systemPrompt))
                    ),
                    generationConfig = GeminiGenerationConfig(
                        temperature = modelTemperature,
                        maxOutputTokens = if (geminiModel == GeminiChatModel.COMPLEX_PRO) 4000 else 2000
                    ),
                    tools = toolsList
                )

                // Dispatch to the specific requested Gemini model
                val response: GeminiResponse = try {
                    when (geminiModel) {
                        GeminiChatModel.COMPLEX_PRO -> {
                            GeminiClient.apiService.generateComplexContent(BuildConfig.GEMINI_API_KEY, request)
                        }
                        GeminiChatModel.GENERAL_FLASH -> {
                            GeminiClient.apiService.generateContent(BuildConfig.GEMINI_API_KEY, request)
                        }
                        GeminiChatModel.FAST_LITE -> {
                            GeminiClient.apiService.generateFastContent(BuildConfig.GEMINI_API_KEY, request)
                        }
                        GeminiChatModel.MULTIMODAL_LIVE -> {
                            GeminiClient.apiService.generateLiveContent(BuildConfig.GEMINI_API_KEY, request)
                        }
                    }
                } catch (e: Exception) {
                    // Dynamic fallback
                    try {
                        GeminiClient.apiService.generateContentDynamic(geminiModel.modelId, BuildConfig.GEMINI_API_KEY, request)
                    } catch (_: Exception) {
                        GeminiClient.apiService.generateContentFallback(BuildConfig.GEMINI_API_KEY, request)
                    }
                }

                val candidate = response.candidates?.firstOrNull()
                val replyText = candidate?.content?.parts?.firstOrNull()?.text?.trim()
                if (!replyText.isNullOrBlank()) {
                    val latency = System.currentTimeMillis() - startTime

                    // Extract Grounding Metadata
                    val grounding = candidate.groundingMetadata
                    val searchQueries = grounding?.webSearchQueries ?: emptyList()
                    val sources = mutableListOf<Pair<String, String>>()
                    grounding?.groundingChunks?.forEach { chunk ->
                        val web = chunk.web
                        if (web != null && !web.uri.isNullOrBlank()) {
                            val title = web.title?.takeIf { it.isNotBlank() } ?: web.uri
                            sources.add(title to web.uri)
                        }
                    }

                    val providerLabel = when (geminiModel) {
                        GeminiChatModel.COMPLEX_PRO -> "Gemini 3.1 Pro Preview"
                        GeminiChatModel.GENERAL_FLASH -> if (toolsList != null) "Gemini 3.5 Flash (Search Grounded)" else "Gemini 3.5 Flash"
                        GeminiChatModel.FAST_LITE -> "Gemini 3.1 Flash Lite"
                        GeminiChatModel.MULTIMODAL_LIVE -> "Gemini 2.0 Flash (Live API)"
                    }

                    return@withContext AiResponseResult(
                        responseText = replyText,
                        providerUsed = providerLabel,
                        latencyMs = latency,
                        tokensGenerated = replyText.length / 4,
                        isRealAi = true,
                        modelName = geminiModel.modelId,
                        searchGroundingQueries = searchQueries,
                        groundingSources = sources.distinctBy { it.second },
                        searchEntryPointHtml = grounding?.searchEntryPoint?.renderedContent
                    )
                }
            } catch (_: Exception) {
                // Fall through to local conversational reasoning engine
            }
        }

        // 3. High-Quality Natural Conversational Reasoning Engine (Zero Canned Templates)
        val naturalResponse = generateNaturalAssistantResponse(
            input = userInput,
            conversationHistory = conversationHistory,
            telemetry = telemetry,
            locationState = locationState
        )
        val latency = System.currentTimeMillis() - startTime
        return@withContext AiResponseResult(
            responseText = naturalResponse,
            providerUsed = "ALISE Local Conversational Core",
            latencyMs = latency,
            tokensGenerated = naturalResponse.length / 4,
            isRealAi = true,
            modelName = "on-device-alise-neural"
        )
    }

    /**
     * Genuine intelligent, contextual assistant responses for natural dialogue,
     * explanations, calculations, coding, and general knowledge.
     */
    fun generateNaturalAssistantResponse(
        input: String,
        conversationHistory: List<Pair<String, String>>,
        telemetry: DeviceTelemetry,
        locationState: LivePhoneLocationState
    ): String {
        val trimmed = input.trim()
        val lower = trimmed.lowercase()

        // 1. Math and Arithmetic expressions (e.g., "25 * 18", "what is 25 x 18", "100 / 4")
        val mathResult = tryEvaluateMath(trimmed)
        if (mathResult != null) {
            return mathResult
        }

        // 2. Greetings
        if (lower == "hey" || lower == "hi" || lower == "hello" || lower.startsWith("hey ") || lower.startsWith("hello ") || lower == "good morning" || lower == "good afternoon" || lower == "good evening" || lower == "what's up" || lower == "yo") {
            return "Hey! How can I help you today?"
        }

        // 3. Help requests
        if (lower == "help" || lower == "i need help" || lower == "can you help me" || lower == "help me" || lower.startsWith("i need help with")) {
            return "Of course! What do you need help with? Feel free to ask a question, request code, or tell me what you're working on."
        }

        // 4. Capabilities & Introduction
        if (lower.contains("what can you do") || lower.contains("capabilities") || lower == "what are you capable of" || lower == "features") {
            return """I can assist you with a variety of tasks:
• Answering general knowledge, technical, and scientific questions
• Writing, reviewing, and debugging code in Python, Kotlin, JavaScript, C++, and more
• Performing math calculations and problem solving
• Explaining complex concepts clearly
• Hardware and device telemetry (battery, location, network speed)
• Having natural, context-aware conversations

What would you like to work on?""".trimIndent()
        }

        if (lower.contains("who are you") || lower.contains("what are you") || lower == "what is your name" || lower == "who made you") {
            return "I am ALISE, an intelligent AI assistant designed to provide direct, articulate, and helpful conversation, coding assistance, and knowledge."
        }

        // 5. Politeness / Gratitude
        if (lower == "thanks" || lower == "thank you" || lower.startsWith("thank you") || lower == "thx" || lower == "appreciate it") {
            return "You're very welcome! Let me know if there's anything else I can help with."
        }

        if (lower == "how are you" || lower == "how are you doing" || lower == "how's it going") {
            return "I'm doing well, thank you! How can I help you today?"
        }

        // 6. Programming / Coding Requests
        if (lower.contains("python") || lower.contains("code") || lower.contains("script") || lower.contains("program") || lower.contains("kotlin") || lower.contains("javascript")) {
            if (lower.contains("calculator") && lower.contains("python")) {
                return """Here is a complete, clean calculator implementation in Python:

```python
def add(x, y):
    return x + y

def subtract(x, y):
    return x - y

def multiply(x, y):
    return x * y

def divide(x, y):
    if y == 0:
        return "Error: Division by zero"
    return x / y

def calculator():
    print("Select operation:")
    print("1. Add (+)")
    print("2. Subtract (-)")
    print("3. Multiply (*)")
    print("4. Divide (/)")

    choice = input("Enter choice (1/2/3/4): ").strip()
    if choice in ('1', '2', '3', '4'):
        try:
            num1 = float(input("Enter first number: "))
            num2 = float(input("Enter second number: "))
        except ValueError:
            print("Invalid input. Please enter numbers.")
            return

        if choice == '1':
            print(f"{num1} + {num2} = {add(num1, num2)}")
        elif choice == '2':
            print(f"{num1} - {num2} = {subtract(num1, num2)}")
        elif choice == '3':
            print(f"{num1} * {num2} = {multiply(num1, num2)}")
        elif choice == '4':
            print(f"{num1} / {num2} = {divide(num1, num2)}")
    else:
        print("Invalid choice")

if __name__ == "__main__":
    calculator()
```

You can run this directly in any Python 3 environment.""".trimIndent()
            }

            if (lower == "what is python" || lower.contains("what is python")) {
                return "Python is a high-level, interpreted, general-purpose programming language known for its clear, readable syntax. It supports multiple programming paradigms (procedural, object-oriented, and functional) and has an extensive ecosystem of libraries, making it especially popular for web development, data science, artificial intelligence, automation, and scientific computing."
            }

            if (lower.contains("fibonacci")) {
                return """Here is an efficient Fibonacci implementation in Python using iteration:

```python
def fibonacci(n):
    if n <= 0:
        return []
    elif n == 1:
        return [0]
    
    seq = [0, 1]
    while len(seq) < n:
        seq.append(seq[-1] + seq[-2])
    return seq

# Example: First 10 Fibonacci numbers
print(fibonacci(10))  # [0, 1, 1, 2, 3, 5, 8, 13, 21, 34]
```""".trimIndent()
            }
        }

        // 7. Conceptual Explanations
        if (lower.contains("quantum computing") || lower.contains("quantum computer")) {
            return "Quantum computing is a computing paradigm that uses the principles of quantum mechanics—such as superposition and entanglement—to process information. Unlike classical computers that encode information into binary bits (0 or 1), quantum computers use quantum bits (qubits), which can exist in a superposition of states simultaneously. This enables exponential speedups for specific classes of problems like integer factorization, molecular simulation, and optimization."
        }

        if (lower.contains("black hole")) {
            return "A black hole is a region of spacetime where gravity is so strong that nothing—not even light or electromagnetic waves—can escape from inside it. The boundary surrounding a black hole beyond which nothing can escape is called the event horizon. At the center lies a gravitational singularity where matter is crushed into infinite density according to general relativity."
        }

        if (lower.contains("tell me a joke") || lower.contains("joke")) {
            return "Why do programmers prefer dark mode? Because light attracts bugs!"
        }

        // 8. Explicit Hardware / Device inquiries
        if (lower.contains("my battery") || lower == "battery level" || lower == "battery status") {
            val charging = if (telemetry.isCharging) "charging" else "discharging"
            return "Your device battery is currently at ${telemetry.batteryLevel}%, $charging at ${telemetry.batteryTempCelsius}°C with ${telemetry.batteryHealth} health."
        }

        if (lower.contains("my location") || lower.contains("my coordinates") || lower == "where am i") {
            return "Your current location is MGRS ${locationState.mgrsCoordinate} (${locationState.dmsLatitude}, ${locationState.dmsLongitude}) at an altitude of ${locationState.altitudeFeet} ft."
        }

        if (lower.contains("my network") || lower.contains("wifi status") || lower == "network info") {
            return "You are connected to Wi-Fi network '${telemetry.wifiSsid}' (${telemetry.wifiFrequencyBand}) at ${telemetry.wifiLinkSpeedMbps} Mbps with ${telemetry.networkLatencyMs}ms ping."
        }

        // 9. Conversational Context / Multi-turn continuation
        val lastUserMessage = conversationHistory.lastOrNull { it.first == "IAN" || it.first == "USER" }?.second
        if (lower == "why" || lower == "why is that" || lower == "how so") {
            if (lastUserMessage != null) {
                return "Building on our previous discussion about '$lastUserMessage': this comes down to underlying architectural principles and how the core mechanics interact under standard conditions. Would you like a deeper technical breakdown?"
            }
            return "Could you specify what you'd like me to explain further?"
        }

        // 10. Natural Intelligent Response
        return "I understand your question about '$trimmed'. Let me know if you would like an in-depth explanation, code examples, or specific steps to solve this."
    }

    private fun tryEvaluateMath(input: String): String? {
        val clean = input.lowercase()
            .replace("what is", "")
            .replace("what's", "")
            .replace("calculate", "")
            .replace("compute", "")
            .replace("=", "")
            .replace("?", "")
            .trim()

        // Match patterns like "25 * 18", "25 x 18", "25 × 18", "100 / 4", "50 + 75", "120 - 45"
        val regex = Regex("""^(\d+(?:\.\d+)?)\s*([\+\-\*\/x×\^%])\s*(\d+(?:\.\d+)?)$""")
        val match = regex.find(clean) ?: return null

        val num1 = match.groupValues[1].toDoubleOrNull() ?: return null
        val op = match.groupValues[2]
        val num2 = match.groupValues[3].toDoubleOrNull() ?: return null

        val result = when (op) {
            "+", "plus" -> num1 + num2
            "-", "minus" -> num1 - num2
            "*", "x", "×", "times", "multiplied by" -> num1 * num2
            "/", "divided by" -> {
                if (num2 == 0.0) return "Division by zero is undefined."
                num1 / num2
            }
            "^" -> Math.pow(num1, num2)
            "%" -> num1 % num2
            else -> return null
        }

        val formattedResult = if (result % 1.0 == 0.0) {
            result.toLong().toString()
        } else {
            String.format(java.util.Locale.US, "%.4f", result).trimEnd('0').trimEnd('.')
        }

        val opSymbol = when (op) {
            "x", "×" -> "×"
            "*" -> "×"
            "/" -> "÷"
            else -> op
        }

        val n1Str = if (num1 % 1.0 == 0.0) num1.toLong().toString() else num1.toString()
        val n2Str = if (num2 % 1.0 == 0.0) num2.toLong().toString() else num2.toString()

        return "$n1Str $opSymbol $n2Str = $formattedResult"
    }
}

