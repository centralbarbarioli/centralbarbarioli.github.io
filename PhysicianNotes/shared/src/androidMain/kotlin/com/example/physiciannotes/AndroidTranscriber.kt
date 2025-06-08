package com.example.physiciannotes

class AndroidTranscriber : Transcriber {
    override suspend fun transcribe(path: String): String {
        // Implementation would use SpeechRecognizer or other on-device API
        return "" // placeholder
    }
}
