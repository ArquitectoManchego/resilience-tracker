package com.example.resiliencetracker.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.resiliencetracker.data.DataRepository
import com.example.resiliencetracker.data.ResilienceSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainScreenViewModel(
    private val repository: DataRepository
) : ViewModel() {

    val summaryState: StateFlow<ResilienceSummary> = repository.summaryFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ResilienceSummary(
            areaScores = emptyMap(),
            totalScore = 0,
            tierName = "Cargando...",
            completedActionsCount = 0,
            totalActionsCount = 0,
            expiringItemsCount = 0,
            activeDrillsCount = 0
        )
    )
}
