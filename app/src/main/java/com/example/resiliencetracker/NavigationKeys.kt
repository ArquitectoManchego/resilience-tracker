package com.example.resiliencetracker

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object MainNavKey : NavKey

@Serializable
data class AreaDetailNavKey(val areaId: String) : NavKey

@Serializable
data object InventoryNavKey : NavKey

@Serializable
data object LibraryNavKey : NavKey

@Serializable
data object DrillsNavKey : NavKey

@Serializable
data object RoadmapNavKey : NavKey
