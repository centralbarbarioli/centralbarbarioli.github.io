package com.example.physiciannotes

import platform.AVFoundation.AVAudioRecorder
import platform.Foundation.NSURL

class IOSVoiceRecorder : VoiceRecorder {
    private var recorder: AVAudioRecorder? = null
    private lateinit var url: NSURL

    override suspend fun startRecording() {
        // TODO: setup AVAudioRecorder
    }

    override suspend fun stopRecording(): String {
        recorder?.stop()
        return url.path ?: ""
    }
}
