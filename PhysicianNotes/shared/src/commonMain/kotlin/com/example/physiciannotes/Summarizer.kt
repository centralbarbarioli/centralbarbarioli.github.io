package com.example.physiciannotes

/** Uses a local AI model to generate summaries */
interface Summarizer {
    suspend fun summarize(text: String): String
}
