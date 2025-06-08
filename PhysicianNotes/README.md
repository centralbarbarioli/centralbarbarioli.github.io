# PhysicianNotes

A Kotlin Multiplatform Mobile (KMM) application designed for physicians to record notes during hospital visits. The app supports Android and iOS and keeps all patient data private by storing notes locally with optional end‑to‑end encryption. Voice memos are recorded on device, transcribed automatically, and summarized by a local AI model.

## Features

- Create text notes or record a voice memo
- Automatic speech transcription
- Local AI model generates a summary from the transcription and any text
- Notes stored locally and can be synced using end‑to‑end encryption
- View notes from previous days

This folder contains only the shared Kotlin code. Android and iOS specific projects can be added using Android Studio with the KMM plugin.
