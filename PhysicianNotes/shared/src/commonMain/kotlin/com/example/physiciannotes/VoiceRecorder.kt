package com.example.physiciannotes

/** Platform-specific voice recorder implementation */
interface VoiceRecorder {
    suspend fun startRecording()
    /** Returns path to the recorded audio file */
    suspend fun stopRecording(): String
}
