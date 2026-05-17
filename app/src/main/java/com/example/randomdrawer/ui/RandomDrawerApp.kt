package com.example.randomdrawer.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.randomdrawer.domain.DRAW_ANIMATION_DELAY_STEP_MILLIS
import com.example.randomdrawer.domain.DrawMode
import com.example.randomdrawer.domain.DrawResult
import com.example.randomdrawer.domain.ItemKind
import com.example.randomdrawer.domain.MAX_DRAW_ANIMATION_DELAY_MILLIS
import com.example.randomdrawer.domain.MIN_DRAW_ANIMATION_DELAY_MILLIS
import com.example.randomdrawer.domain.ThemeMode
import kotlin.math.PI
import kotlin.math.sin

const val RESULT_RING_PULSE_PERIOD_MILLIS = 1200L

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
    onSetAnimationsEnabled: (Boolean) -> Unit,
    onSetAnimationDelayMillis: (Long) -> Unit,
    onDraw: () -> Unit,
    onDismissDrawPopup: () -> Unit,
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .testTag("drawerSettingsScroll")
                ) {
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
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Animations")
                        Switch(checked = state.animationsEnabled, onCheckedChange = onSetAnimationsEnabled)
                    }
                    if (state.animationsEnabled) {
                        AnimationDelayRow(
                            valueMillis = state.animationDelayMillis,
                            label = state.animationDelayLabel,
                            onValueChange = onSetAnimationDelayMillis
                        )
                    }
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

                Button(onClick = onDraw, enabled = !state.isDrawing, modifier = Modifier.fillMaxWidth()) {
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

    if (state.drawPopupVisible) {
        DrawResultDialog(
            state = state,
            onDismiss = onDismissDrawPopup,
            onToggleResultExpanded = onToggleResultExpanded,
            onOpenFile = onOpenFile
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
private fun AnimationDelayRow(
    valueMillis: Long,
    label: String,
    onValueChange: (Long) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Animation delay")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                enabled = valueMillis > MIN_DRAW_ANIMATION_DELAY_MILLIS,
                onClick = { onValueChange(valueMillis - DRAW_ANIMATION_DELAY_STEP_MILLIS) }
            ) { Text("-") }
            Text(label, modifier = Modifier.padding(12.dp))
            OutlinedButton(
                enabled = valueMillis < MAX_DRAW_ANIMATION_DELAY_MILLIS,
                onClick = { onValueChange(valueMillis + DRAW_ANIMATION_DELAY_STEP_MILLIS) }
            ) { Text("+") }
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
        if (result.items.isEmpty()) {
            Text("No result yet", Modifier.padding(16.dp))
        } else {
            ResultContent(
                result = result,
                showStatusIcon = false,
                onToggleResultExpanded = onToggleResultExpanded,
                onOpenFile = onOpenFile
            )
        }
    }
}

@Composable
private fun DrawResultDialog(
    state: RandomDrawerUiState,
    onDismiss: () -> Unit,
    onToggleResultExpanded: () -> Unit,
    onOpenFile: (String, String?) -> Unit
) {
    val entryProgress = rememberFrameEntryProgress(durationMillis = 360L)
    val popupScale = 0.52f + (0.48f * entryProgress)
    val popupAlpha = entryProgress

    Dialog(
        onDismissRequest = {
            if (!state.isDrawing) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = !state.isDrawing,
            dismissOnClickOutside = !state.isDrawing
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .graphicsLayer(
                    scaleX = popupScale,
                    scaleY = popupScale,
                    alpha = popupAlpha
                ),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shadowElevation = 12.dp,
            tonalElevation = 0.dp
        ) {
            AnimatedContent(
                targetState = state.isDrawing,
                transitionSpec = {
                    (fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.94f))
                        .togetherWith(fadeOut(tween(140)) + scaleOut(tween(140), targetScale = 0.98f))
                },
                label = "drawPopupAnimation"
            ) { isDrawing ->
                if (isDrawing) {
                    ProcessingResultContent()
                } else {
                    DrawPopupResultContent(
                        result = state.lastResult,
                        onDismiss = onDismiss,
                        onToggleResultExpanded = onToggleResultExpanded,
                        onOpenFile = onOpenFile
                    )
                }
            }
        }
    }
}

@Composable
private fun ProcessingResultContent() {
    val spinProgress = rememberFrameLoopProgress(periodMillis = 720L)
    val pulseProgress = rememberFrameLoopProgress(periodMillis = RESULT_RING_PULSE_PERIOD_MILLIS)
    val pulse = 0.82f + (0.24f * ((sin(pulseProgress * PI * 2.0) + 1.0) / 2.0)).toFloat()
    val spin = spinProgress * 360f

    Column(
        modifier = Modifier.fillMaxWidth().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(88.dp)
                .graphicsLayer(scaleX = pulse, scaleY = pulse)
        ) {
            val ringColor = MaterialTheme.colorScheme.primary
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(rotationZ = spin)
            ) {
                val strokeWidth = 4.dp.toPx()
                drawArc(
                    color = ringColor.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 250f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            StatusCircleIcon(processing = true)
        }
        Text("Drawing...", style = MaterialTheme.typography.titleMedium)
        Text("Picking a random result", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun rememberFrameEntryProgress(durationMillis: Long): Float {
    var progress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(durationMillis) {
        val startNanos = withFrameNanos { it }
        while (progress < 1f) {
            withFrameNanos { nowNanos ->
                val elapsedMillis = (nowNanos - startNanos) / 1_000_000f
                val linearProgress = (elapsedMillis / durationMillis).coerceIn(0f, 1f)
                progress = FastOutSlowInEasing.transform(linearProgress)
            }
        }
    }
    return progress
}

@Composable
private fun rememberFrameLoopProgress(periodMillis: Long): Float {
    var progress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(periodMillis) {
        while (true) {
            withFrameNanos { nowNanos ->
                val elapsedMillis = nowNanos / 1_000_000L
                progress = (elapsedMillis % periodMillis).toFloat() / periodMillis
            }
        }
    }
    return progress
}

@Composable
private fun DrawPopupResultContent(
    result: DrawResult,
    onDismiss: () -> Unit,
    onToggleResultExpanded: () -> Unit,
    onOpenFile: (String, String?) -> Unit
) {
    Column(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (result.items.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusCircleIcon(processing = false)
                Text("No entries to draw", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            ResultRevealContent(
                result = result,
                onToggleResultExpanded = onToggleResultExpanded,
                onOpenFile = onOpenFile
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    }
}

@Composable
private fun ResultRevealContent(
    result: DrawResult,
    onToggleResultExpanded: () -> Unit,
    onOpenFile: (String, String?) -> Unit
) {
    val firstItem = result.items.first()
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                resultRevealHeader(result),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                firstItem.displayName,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            ResultChip(resultItemChipLabel(firstItem))
            val cachedPath = firstItem.cachedFilePath
            if (firstItem.kind == ItemKind.FILE && cachedPath != null) {
                TextButton(onClick = { onOpenFile(cachedPath, firstItem.mimeType) }) {
                    Text("Open")
                }
            }
        }

        if (result.items.size > 1) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Drawn items", color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onToggleResultExpanded) {
                    Text(if (result.expanded) "Hide" else "Show all")
                }
            }
            if (result.expanded) {
                result.items.forEachIndexed { index, item ->
                    ResultListRow(index = index, item = item, onOpenFile = onOpenFile)
                }
            }
        }
    }
}

@Composable
private fun ResultChip(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun ResultListRow(
    index: Int,
    item: com.example.randomdrawer.domain.DrawerItem,
    onOpenFile: (String, String?) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text("${index + 1}. ${item.displayName}", style = MaterialTheme.typography.bodyMedium)
                Text(resultItemChipLabel(item), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val cachedPath = item.cachedFilePath
            if (item.kind == ItemKind.FILE && cachedPath != null) {
                TextButton(onClick = { onOpenFile(cachedPath, item.mimeType) }) {
                    Text("Open")
                }
            }
        }
    }
}

fun resultRevealHeader(result: DrawResult): String {
    return if (result.items.size == 1) "Selected" else "${result.items.size} results selected"
}

private fun resultItemChipLabel(item: com.example.randomdrawer.domain.DrawerItem): String {
    return when {
        item.kind == ItemKind.TEXT -> "text"
        item.cachedFilePath == null -> "cache deleted"
        else -> "file"
    }
}

@Composable
private fun ResultContent(
    result: DrawResult,
    showStatusIcon: Boolean,
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
            if (showStatusIcon) {
                StatusCircleIcon(processing = false)
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
