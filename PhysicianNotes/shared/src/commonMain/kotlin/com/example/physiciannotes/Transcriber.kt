package com.example.physiciannotes

/** Platform-specific transcription implementation */
interface Transcriber {
    suspend fun transcribe(path: String): String
}
