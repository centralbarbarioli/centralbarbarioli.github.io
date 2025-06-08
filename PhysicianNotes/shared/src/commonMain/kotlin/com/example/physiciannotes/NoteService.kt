package com.example.physiciannotes

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random

class NoteService(
    private val repository: NoteRepository,
    private val recorder: VoiceRecorder,
    private val transcriber: Transcriber,
    private val summarizer: Summarizer
) {
    suspend fun startVoiceNote() {
        recorder.startRecording()
    }

    suspend fun saveVoiceNote(text: String = "") {
        val path = recorder.stopRecording()
        val transcription = transcriber.transcribe(path)
        val summary = summarizer.summarize(text + "\n" + transcription)
        val note = Note(
            id = Random.nextBytes(8).joinToString("") { it.toInt().toString(16) },
            date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            text = text,
            voiceFilePath = path,
            transcription = transcription,
            summary = summary
        )
        repository.save(note)
    }

    suspend fun addNote(text: String) {
        val note = Note(
            id = Random.nextBytes(8).joinToString("") { it.toInt().toString(16) },
            date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            text = text
        )
        repository.save(note)
    }

    suspend fun listNotes(): List<Note> = repository.getAll()
}
