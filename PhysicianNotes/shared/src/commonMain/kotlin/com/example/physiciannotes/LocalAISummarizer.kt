package com.example.physiciannotes

/**
 * Example summarizer that would call a locally hosted AI model.
 * For demonstration this simply returns the first 200 characters.
 */
class LocalAISummarizer : Summarizer {
    override suspend fun summarize(text: String): String =
        if (text.length <= 200) text else text.substring(0, 200)
}
