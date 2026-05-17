package com.example.randomdrawer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.FileProvider
import androidx.room.Room
import com.example.randomdrawer.data.FileCacheManager
import com.example.randomdrawer.data.RandomDrawerDatabase
import com.example.randomdrawer.data.RandomDrawerRepository
import com.example.randomdrawer.ui.RandomDrawerApp
import com.example.randomdrawer.ui.RandomDrawerViewModel
import com.example.randomdrawer.ui.theme.RandomDrawerTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(
            applicationContext,
            RandomDrawerDatabase::class.java,
            "random-drawer.db"
        )
            .addMigrations(RandomDrawerDatabase.Migration1To2)
            .build()
        val repository = RandomDrawerRepository(database.dao(), FileCacheManager(filesDir))
        val viewModel = RandomDrawerViewModel(repository)
        viewModel.initialize()

        var fileWithNameMode = false
        val addFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) {
                fileWithNameMode = false
                return@registerForActivityResult
            }

            val name = displayNameFor(uri)
            val mime = contentResolver.getType(uri)
            val spaceId = viewModel.state.value.selectedSpaceId
            if (spaceId == null) {
                fileWithNameMode = false
                return@registerForActivityResult
            }

            if (fileWithNameMode) {
                viewModel.setPendingPickedFile(uri.toString(), name, mime)
                fileWithNameMode = false
            } else {
                contentResolver.openInputStream(uri)?.let { input ->
                    viewModel.addCachedFile(spaceId, name, name, mime, input)
                }
            }
        }

        setContent {
            val state by viewModel.state.collectAsState()
            RandomDrawerTheme(themeMode = state.themeMode) {
                RandomDrawerApp(
                    state = state,
                    onToggleDrawer = viewModel::toggleDrawer,
                    onNewSpace = viewModel::createNewSpace,
                    onSelectSpace = viewModel::selectSpace,
                    onRenameSpace = viewModel::renameSpace,
                    onDeleteSpace = viewModel::deleteSpace,
                    onToggleTheme = viewModel::toggleTheme,
                    onDeleteAllCache = viewModel::deleteAllCache,
                    onSetDrawMode = viewModel::setDrawMode,
                    onSetDrawCount = viewModel::setDrawCount,
                    onSetSingleRepeatLimit = viewModel::setSingleRepeatLimit,
                    onSetMultiRepeatLimit = viewModel::setMultiRepeatLimit,
                    onSetAnimationsEnabled = viewModel::setAnimationsEnabled,
                    onSetAnimationDelayMillis = viewModel::setAnimationDelayMillis,
                    onDraw = viewModel::drawRandom,
                    onDismissDrawPopup = viewModel::dismissDrawPopup,
                    onToggleResultExpanded = viewModel::toggleResultExpanded,
                    onAddText = viewModel::addText,
                    onAddFile = {
                        fileWithNameMode = false
                        addFileLauncher.launch(arrayOf("*/*"))
                    },
                    onAddFileWithName = {
                        fileWithNameMode = true
                        addFileLauncher.launch(arrayOf("*/*"))
                    },
                    onDeleteItem = viewModel::deleteItem,
                    onConfirmFileWithName = { pendingFile, customName ->
                        val uri = Uri.parse(pendingFile.uriString)
                        val spaceId = viewModel.state.value.selectedSpaceId
                        if (spaceId != null) {
                            contentResolver.openInputStream(uri)?.let { input ->
                                viewModel.addCachedFile(
                                    spaceId = spaceId,
                                    displayName = customName,
                                    originalFileName = pendingFile.originalFileName,
                                    mimeType = pendingFile.mimeType,
                                    input = input
                                )
                            }
                        }
                        viewModel.clearPendingPickedFile()
                    },
                    onCancelFileWithName = viewModel::clearPendingPickedFile,
                    onOpenFile = ::openCachedFile
                )
            }
        }
    }

    private fun displayNameFor(uri: Uri): String {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getString(index) ?: "Picked file"
            }
        }
        return "Picked file"
    }

    private fun openCachedFile(path: String, mimeType: String?) {
        val uri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            File(path)
        )
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, mimeType ?: "*/*")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "Open file"))
    }
}
