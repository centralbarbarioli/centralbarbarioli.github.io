package com.example.physiciannotes

import kotlinx.datetime.LocalDateTime

interface NoteRepository {
    suspend fun save(note: Note)
    suspend fun getAll(): List<Note>
    suspend fun getByDate(date: LocalDateTime): List<Note>
}
