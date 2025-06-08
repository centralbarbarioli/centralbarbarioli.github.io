package com.example.physiciannotes

import android.media.MediaRecorder
import java.io.File

class PlatformVoiceRecorder : VoiceRecorder {
    private var recorder: MediaRecorder? = null
    private lateinit var file: File

    override suspend fun startRecording() {
        val output = File.createTempFile("voice", ".m4a")
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(output.absolutePath)
            prepare()
            start()
        }
        file = output
    }

    override suspend fun stopRecording(): String {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        return file.absolutePath
    }
}
