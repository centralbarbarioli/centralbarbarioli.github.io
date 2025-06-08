package com.example.physiciannotes.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pointerinput.pointerMoveFilter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.physiciannotes.*

class MainActivity : ComponentActivity() {
    private val service = NoteService(
        InMemoryNoteRepository(),
        PlatformVoiceRecorder(),
        AndroidTranscriber(),
        LocalAISummarizer()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App(service) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(service: NoteService) {
    var notes by remember { mutableStateOf(listOf<Note>()) }
    var recording by remember { mutableStateOf(false) }
    var drawerOpen by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<Note?>(null) }

    LaunchedEffect(Unit) {
        notes = service.listNotes().sortedByDescending { it.date }
    }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(24.dp))
                Text("User Options", modifier = Modifier.padding(start = 16.dp))
                NavigationDrawerItem(
                    label = { Text("Profile") },
                    selected = false,
                    onClick = {}
                )
                NavigationDrawerItem(
                    label = { Text("Personalization") },
                    selected = false,
                    onClick = {}
                )
            }
        },
        drawerState = rememberDrawerState(if (drawerOpen) DrawerValue.Open else DrawerValue.Closed),
        gesturesEnabled = drawerOpen,
        onDismiss = { drawerOpen = false }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Physician Notes") },
                    navigationIcon = {
                        IconButton(onClick = { drawerOpen = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    recording = !recording
                    if (recording) {
                        service.startVoiceNote()
                    } else {
                        service.saveVoiceNote()
                        notes = service.listNotes().sortedByDescending { it.date }
                    }
                }) {
                    Icon(Icons.Default.Mic, contentDescription = "Record")
                }
            }
        ) { padding ->
            if (selectedNote == null) {
                LazyColumn(
                    modifier = Modifier.padding(padding).padding(16.dp)
                ) {
                    items(notes) { note ->
                        NoteCard(note, onAdd = {
                            recording = true
                            service.startVoiceNote()
                        }, onClick = { selectedNote = note })
                        Spacer(Modifier.height(8.dp))
                    }
                }
            } else {
                NoteDetail(selectedNote!!, onBack = { selectedNote = null })
            }
        }
    }
}

@Composable
fun NoteCard(note: Note, onAdd: () -> Unit, onClick: () -> Unit) {
    var hovered by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerMoveFilter(
                onEnter = {
                    hovered = true
                    false
                },
                onExit = {
                    hovered = false
                    false
                }
            ),
        onClick = onClick
    ) {
        Box(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(note.summary ?: note.text)
                Text(note.date.toString(), style = MaterialTheme.typography.labelSmall)
            }
            if (hovered) {
                IconButton(
                    onClick = onAdd,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    }
}

@Composable
fun NoteDetail(note: Note, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Menu, contentDescription = "Back")
            }
            Text("Note Details", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(16.dp))
        Text(note.text)
        note.summary?.let {
            Spacer(Modifier.height(8.dp))
            Text(it)
        }
    }
}
