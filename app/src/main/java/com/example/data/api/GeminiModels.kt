package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @Json(name = "tools") val tools: List<GeminiTool>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiTool(
    @Json(name = "googleSearch") val googleSearch: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "role") val role: String? = "user",
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Float = 0.4f,
    @Json(name = "topK") val topK: Int = 40,
    @Json(name = "topP") val topP: Float = 0.95f,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int = 4000
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null,
    @Json(name = "finishReason") val finishReason: String? = null,
    @Json(name = "groundingMetadata") val groundingMetadata: GeminiGroundingMetadata? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGroundingMetadata(
    @Json(name = "webSearchQueries") val webSearchQueries: List<String>? = null,
    @Json(name = "groundingChunks") val groundingChunks: List<GeminiGroundingChunk>? = null,
    @Json(name = "groundingSupports") val groundingSupports: List<GeminiGroundingSupport>? = null,
    @Json(name = "searchEntryPoint") val searchEntryPoint: GeminiSearchEntryPoint? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGroundingChunk(
    @Json(name = "web") val web: GeminiWebChunk? = null
)

@JsonClass(generateAdapter = true)
data class GeminiWebChunk(
    @Json(name = "uri") val uri: String? = null,
    @Json(name = "title") val title: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGroundingSupport(
    @Json(name = "groundingChunkIndices") val groundingChunkIndices: List<Int>? = null,
    @Json(name = "confidenceScores") val confidenceScores: List<Float>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiSearchEntryPoint(
    @Json(name = "renderedContent") val renderedContent: String? = null
)
