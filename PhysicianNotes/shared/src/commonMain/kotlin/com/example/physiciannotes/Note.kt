package com.example.physiciannotes

import kotlinx.datetime.LocalDateTime

/** A note recorded by the physician. */
data class Note(
    val id: String,
    val date: LocalDateTime,
    val text: String,
    val voiceFilePath: String? = null,
    val transcription: String? = null,
    val summary: String? = null
)
