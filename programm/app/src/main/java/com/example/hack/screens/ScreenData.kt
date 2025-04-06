package com.example.hack.screens

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpeakerSegment(
    val end: Double,
    val start: Double,
    val text: String
)

@Serializable
data class Speakers(
    @SerialName("speaker_0") val speaker0: List<SpeakerSegment>,
    @SerialName("speaker_1") val speaker1: List<SpeakerSegment>
)

@Serializable
data class ApiResponse(
    val full_text: String,
    val source: String,
    val speakers: Speakers,
    val summary: String
)