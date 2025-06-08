package com.example.physiciannotes

import kotlinx.datetime.LocalDateTime

class InMemoryNoteRepository : NoteRepository {
    private val notes = mutableListOf<Note>()

    override suspend fun save(note: Note) {
        notes.removeAll { it.id == note.id }
        notes.add(note)
    }

    override suspend fun getAll(): List<Note> = notes.toList()

    override suspend fun getByDate(date: LocalDateTime): List<Note> =
        notes.filter { it.date.date == date.date }
}
