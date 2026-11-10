package com.example.resiliencetracker

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.resiliencetracker.data.DefaultDataRepository
import com.example.resiliencetracker.ui.detail.AreaDetailScreen
import com.example.resiliencetracker.ui.drills.DrillsScreen
import com.example.resiliencetracker.ui.inventory.InventoryScreen
import com.example.resiliencetracker.ui.library.LibraryScreen
import com.example.resiliencetracker.ui.main.MainScreen
import com.example.resiliencetracker.ui.main.MainScreenViewModel
import com.example.resiliencetracker.ui.roadmap.RoadmapScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(MainNavKey)
    val repository = remember { DefaultDataRepository() }
    val mainViewModel = remember { MainScreenViewModel(repository) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<MainNavKey> {
                MainScreen(
                    onNavigate = { key -> backStack.add(key) },
                    viewModel = mainViewModel,
                    modifier = Modifier.safeDrawingPadding()
                )
            }
            entry<AreaDetailNavKey> { key ->
                AreaDetailScreen(
                    areaId = key.areaId,
                    repository = repository,
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToGuide = { areaId -> backStack.add(LibraryNavKey) }
                )
            }
            entry<InventoryNavKey> {
                InventoryScreen(
                    repository = repository,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<LibraryNavKey> {
                LibraryScreen(
                    repository = repository,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<DrillsNavKey> {
                DrillsScreen(
                    repository = repository,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<RoadmapNavKey> {
                RoadmapScreen(
                    repository = repository,
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToArea = { areaId -> backStack.add(AreaDetailNavKey(areaId)) }
                )
            }
        }
    )
}
