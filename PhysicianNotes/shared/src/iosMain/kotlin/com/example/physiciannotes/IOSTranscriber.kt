package com.example.physiciannotes

class IOSTranscriber : Transcriber {
    override suspend fun transcribe(path: String): String {
        // Implementation would use Apple's Speech framework
        return ""
    }
}
