package com.example.randomdrawer.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.randomdrawer.domain.DrawMode
import com.example.randomdrawer.domain.DrawResult
import com.example.randomdrawer.domain.ItemKind
import com.example.randomdrawer.domain.ThemeMode

@Composable
fun RandomDrawerApp(
    state: RandomDrawerUiState,
    onToggleDrawer: () -> Unit,
    onNewSpace: () -> Unit,
    onSelectSpace: (Long) -> Unit,
    onRenameSpace: (Long, String) -> Unit,
    onDeleteSpace: (Long) -> Unit,
    onToggleTheme: () -> Unit,
    onDeleteAllCache: () -> Unit,
    onSetDrawMode: (DrawMode) -> Unit,
    onSetDrawCount: (Int) -> Unit,
    onSetSingleRepeatLimit: (Int) -> Unit,
    onSetMultiRepeatLimit: (Int) -> Unit,
    onDraw: () -> Unit,
    onToggleResultExpanded: () -> Unit,
    onAddText: (String) -> Unit,
    onAddFile: () -> Unit,
    onAddFileWithName: () -> Unit,
    onDeleteItem: (Long) -> Unit,
    onConfirmFileWithName: (PendingPickedFile, String) -> Unit,
    onCancelFileWithName: () -> Unit,
    onOpenFile: (String, String?) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var textDialogOpen by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf("") }
    var renameSpaceId by remember { mutableStateOf<Long?>(null) }
    var renameValue by remember { mutableStateOf("") }

    LaunchedEffect(state.drawerOpen) {
        if (state.drawerOpen) drawerState.open() else drawerState.close()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Draw spaces", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                state.spaces.forEach { space ->
                    SpaceRow(
                        title = space.title,
                        selected = space.id == state.selectedSpaceId,
                        onSelect = { onSelectSpace(space.id) },
                        onRename = {
                            renameSpaceId = space.id
                            renameValue = space.title
                        },
                        onDelete = { onDeleteSpace(space.id) }
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
                Text("Settings", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                RepeatLimitRow(
                    label = "Single repeat limit",
                    value = state.singleRepeatLimit,
                    onValueChange = onSetSingleRepeatLimit
                )
                RepeatLimitRow(
                    label = "Multiple repeat limit",
                    value = state.multiRepeatLimit,
                    onValueChange = onSetMultiRepeatLimit
                )
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
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
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
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val cachedPath = item.cachedFilePath
                                if (item.kind == ItemKind.FILE && cachedPath != null) {
                                    TextButton(onClick = { onOpenFile(cachedPath, item.mimeType) }) {
                                        Text("Open")
                                    }
                                }
                                TextButton(onClick = { onDeleteItem(item.id) }) {
                                    Text("Delete")
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

    renameSpaceId?.let { spaceId ->
        AlertDialog(
            onDismissRequest = { renameSpaceId = null },
            title = { Text("Rename space") },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    label = { Text("Space name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = renameValue.trim()
                    if (trimmed.isNotEmpty()) {
                        onRenameSpace(spaceId, trimmed)
                        renameSpaceId = null
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { renameSpaceId = null }) { Text("Cancel") }
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
private fun SpaceRow(
    title: String,
    selected: Boolean,
    onSelect: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                title,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp)
                    .pointerInput(title) {
                        detectTapGestures(
                            onTap = { onSelect() },
                            onLongPress = { onRename() }
                        )
                    }
            )
            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        }
    }
}

@Composable
private fun RepeatLimitRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onValueChange(value - 1) }) { Text("-") }
            Text(value.toString(), modifier = Modifier.padding(12.dp))
            OutlinedButton(onClick = { onValueChange(value + 1) }) { Text("+") }
        }
    }
}

@Composable
private fun ResultCard(
    state: RandomDrawerUiState,
    onToggleResultExpanded: () -> Unit,
    onOpenFile: (String, String?) -> Unit
) {
    val result = state.lastResult
    Card(Modifier.fillMaxWidth()) {
        AnimatedContent(
            targetState = state.isDrawing,
            transitionSpec = {
                (fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.96f))
                    .togetherWith(fadeOut(tween(140)) + scaleOut(tween(140), targetScale = 0.98f))
            },
            label = "drawResultAnimation"
        ) { isDrawing ->
            when {
                isDrawing -> ProcessingResultContent()
                result.items.isEmpty() -> Text("No result yet", Modifier.padding(16.dp))
                else -> ConfirmedResultContent(result, onToggleResultExpanded, onOpenFile)
            }
        }
    }
}

@Composable
private fun ProcessingResultContent() {
    val transition = rememberInfiniteTransition(label = "drawingPulse")
    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drawingPulse"
    )

    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(88.dp)
                .graphicsLayer(scaleX = pulse, scaleY = pulse)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            StatusCircleIcon(processing = true)
        }
        Text("Drawing...", style = MaterialTheme.typography.titleMedium)
        Text("Picking a random result", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ConfirmedResultContent(
    result: DrawResult,
    onToggleResultExpanded: () -> Unit,
    onOpenFile: (String, String?) -> Unit
) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (result.items.size > 1) {
                TextButton(onClick = onToggleResultExpanded) {
                    Text(if (result.expanded) "v" else "<")
                }
            }
            StatusCircleIcon(processing = false)
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

@Composable
private fun StatusCircleIcon(processing: Boolean) {
    val fillColor = if (processing) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.primary
    }
    val markColor = if (processing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary

    Canvas(Modifier.size(44.dp)) {
        drawCircle(fillColor)
        if (processing) {
            drawCircle(markColor, radius = 4.dp.toPx())
        } else {
            val strokeWidth = 4.dp.toPx()
            drawLine(
                color = markColor,
                start = Offset(size.width * 0.28f, size.height * 0.52f),
                end = Offset(size.width * 0.44f, size.height * 0.68f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = markColor,
                start = Offset(size.width * 0.44f, size.height * 0.68f),
                end = Offset(size.width * 0.74f, size.height * 0.34f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
