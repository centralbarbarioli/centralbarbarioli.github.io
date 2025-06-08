package com.example.physiciannotes

/** Handles optional end-to-end encrypted sync of notes. */
interface EncryptedSync {
    suspend fun upload(note: Note)
    suspend fun downloadAll(): List<Note>
}
