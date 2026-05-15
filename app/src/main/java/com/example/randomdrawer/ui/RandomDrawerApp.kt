package com.example.randomdrawer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.randomdrawer.domain.DrawMode
import com.example.randomdrawer.domain.ItemKind
import com.example.randomdrawer.domain.ThemeMode

@Composable
fun RandomDrawerApp(
    state: RandomDrawerUiState,
    onToggleDrawer: () -> Unit,
    onNewSpace: () -> Unit,
    onSelectSpace: (Long) -> Unit,
    onToggleTheme: () -> Unit,
    onDeleteAllCache: () -> Unit,
    onSetDrawMode: (DrawMode) -> Unit,
    onSetDrawCount: (Int) -> Unit,
    onDraw: () -> Unit,
    onToggleResultExpanded: () -> Unit,
    onAddText: (String) -> Unit,
    onAddFile: () -> Unit,
    onAddFileWithName: () -> Unit,
    onConfirmFileWithName: (PendingPickedFile, String) -> Unit,
    onCancelFileWithName: () -> Unit,
    onOpenFile: (String, String?) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var textDialogOpen by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf("") }

    LaunchedEffect(state.drawerOpen) {
        if (state.drawerOpen) drawerState.open() else drawerState.close()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Draw spaces", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                state.spaces.forEach { space ->
                    NavigationDrawerItem(
                        label = { Text(space.title) },
                        selected = space.id == state.selectedSpaceId,
                        onClick = { onSelectSpace(space.id) }
                    )
                }
                TextButton(onClick = onNewSpace) { Text("+ New") }
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("AMOLED black")
                    Switch(checked = state.themeMode == ThemeMode.AMOLED, onCheckedChange = { onToggleTheme() })
                }
                OutlinedButton(onClick = onDeleteAllCache, modifier = Modifier.padding(16.dp)) {
                    Text("Delete All Cache")
                }
            }
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onToggleDrawer) { Text("Menu") }
                    Column {
                        Text("Random Drawer", style = MaterialTheme.typography.titleLarge)
                        Text(state.selectedSpaceTitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("${state.savedItemCount} items")
                }

                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${state.savedItemCount}\nsaved entries")
                        Text("${state.cachedFileCount}\nfiles cached")
                    }
                }

                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = state.drawMode == DrawMode.SINGLE,
                        onClick = { onSetDrawMode(DrawMode.SINGLE) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) { Text("Single") }
                    SegmentedButton(
                        selected = state.drawMode == DrawMode.MULTIPLE,
                        onClick = { onSetDrawMode(DrawMode.MULTIPLE) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) { Text("Multiple") }
                }

                if (state.drawMode == DrawMode.MULTIPLE) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { onSetDrawCount(state.drawCount - 1) }) { Text("-") }
                        Text(state.cappedDrawCount.toString(), modifier = Modifier.padding(12.dp))
                        OutlinedButton(onClick = { onSetDrawCount(state.drawCount + 1) }) { Text("+") }
                    }
                }

                ResultCard(state, onToggleResultExpanded, onOpenFile)

                Button(onClick = onDraw, modifier = Modifier.fillMaxWidth()) {
                    val label = if (state.drawMode == DrawMode.MULTIPLE) {
                        "Draw ${state.cappedDrawCount} Random"
                    } else {
                        "Draw Random"
                    }
                    Text(label)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { textDialogOpen = true }, modifier = Modifier.weight(1f)) {
                        Text("Add Text")
                    }
                    OutlinedButton(onClick = onAddFile, modifier = Modifier.weight(1f)) { Text("Add File") }
                }
                OutlinedButton(onClick = onAddFileWithName, modifier = Modifier.fillMaxWidth()) {
                    Text("Add File With Name")
                }

                Text("Saved entries", style = MaterialTheme.typography.titleMedium)
                state.items.forEach { item ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(item.displayName)
                            Text(
                                if (item.kind == ItemKind.TEXT) {
                                    "Text item"
                                } else if (item.cachedFilePath == null) {
                                    "Cache deleted"
                                } else {
                                    "File cache ready"
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val cachedPath = item.cachedFilePath
                            if (item.kind == ItemKind.FILE && cachedPath != null) {
                                TextButton(onClick = { onOpenFile(cachedPath, item.mimeType) }) {
                                    Text("Open")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (textDialogOpen) {
        AlertDialog(
            onDismissRequest = { textDialogOpen = false },
            title = { Text("Add Text") },
            text = {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = { Text("Text") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = textValue.trim()
                    if (trimmed.isNotEmpty()) {
                        onAddText(trimmed)
                        textValue = ""
                        textDialogOpen = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { textDialogOpen = false }) { Text("Cancel") }
            }
        )
    }

    state.pendingPickedFile?.let { pendingFile ->
        var displayName by remember(pendingFile.uriString) {
            mutableStateOf(pendingFile.originalFileName)
        }

        AlertDialog(
            onDismissRequest = onCancelFileWithName,
            title = { Text("Display name") },
            text = {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = displayName.trim()
                    if (trimmed.isNotEmpty()) {
                        onConfirmFileWithName(pendingFile, trimmed)
                    }
                }) { Text("Save File") }
            },
            dismissButton = {
                TextButton(onClick = onCancelFileWithName) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ResultCard(
    state: RandomDrawerUiState,
    onToggleResultExpanded: () -> Unit,
    onOpenFile: (String, String?) -> Unit
) {
    val result = state.lastResult
    if (result.items.isEmpty()) {
        Card(Modifier.fillMaxWidth()) {
            Text("No result yet", Modifier.padding(16.dp))
        }
        return
    }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (result.items.size > 1) {
                    TextButton(onClick = onToggleResultExpanded) {
                        Text(if (result.expanded) "v" else "<")
                    }
                }
                Column {
                    val firstItem = result.items.first()
                    Text(if (result.items.size > 1) "${result.items.size} random results" else "Random result")
                    Text(firstItem.displayName, style = MaterialTheme.typography.titleMedium)
                    if (firstItem.kind == ItemKind.FILE && firstItem.cachedFilePath == null) {
                        Text("Cache deleted", color = MaterialTheme.colorScheme.error)
                    }
                    val cachedPath = firstItem.cachedFilePath
                    if (firstItem.kind == ItemKind.FILE && cachedPath != null) {
                        TextButton(onClick = { onOpenFile(cachedPath, firstItem.mimeType) }) {
                            Text("Open")
                        }
                    }
                }
            }
            if (result.expanded) {
                result.items.forEachIndexed { index, item ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${index + 1}. ${item.displayName}")
                        val cachedPath = item.cachedFilePath
                        if (item.kind == ItemKind.FILE && cachedPath != null) {
                            TextButton(onClick = { onOpenFile(cachedPath, item.mimeType) }) {
                                Text("Open")
                            }
                        }
                    }
                }
            }
        }
    }
}
