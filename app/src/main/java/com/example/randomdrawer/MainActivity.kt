package com.example.randomdrawer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.room.Room
import com.example.randomdrawer.data.FileCacheManager
import com.example.randomdrawer.data.RandomDrawerDatabase
import com.example.randomdrawer.data.RandomDrawerRepository
import com.example.randomdrawer.ui.RandomDrawerApp
import com.example.randomdrawer.ui.RandomDrawerViewModel
import com.example.randomdrawer.ui.theme.RandomDrawerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(
            applicationContext,
            RandomDrawerDatabase::class.java,
            "random-drawer.db"
        ).build()
        val repository = RandomDrawerRepository(database.dao(), FileCacheManager(filesDir))
        val viewModel = RandomDrawerViewModel(repository)
        viewModel.initialize()

        setContent {
            val state by viewModel.state.collectAsState()
            RandomDrawerTheme(themeMode = state.themeMode) {
                RandomDrawerApp(
                    state = state,
                    onToggleDrawer = viewModel::toggleDrawer,
                    onNewSpace = {},
                    onSelectSpace = viewModel::selectSpace,
                    onToggleTheme = viewModel::toggleTheme,
                    onDeleteAllCache = {},
                    onSetDrawMode = viewModel::setDrawMode,
                    onSetDrawCount = viewModel::setDrawCount,
                    onDraw = viewModel::drawRandom,
                    onToggleResultExpanded = viewModel::toggleResultExpanded,
                    onAddText = {},
                    onAddFile = {},
                    onAddFileWithName = {}
                )
            }
        }
    }
}
