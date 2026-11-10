package com.example.resiliencetracker.ui.roadmap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.resiliencetracker.data.DataRepository
import com.example.resiliencetracker.data.SecurityAction
import com.example.resiliencetracker.data.SecurityCategory
import com.example.resiliencetracker.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadmapScreen(
    repository: DataRepository,
    onBack: () -> Unit,
    onNavigateToArea: (String) -> Unit
) {
    val actions by repository.actionsFlow.collectAsState(initial = emptyList())
    val summary by repository.summaryFlow.collectAsState(initial = null)

    val prioritizedActions = remember(actions, summary) {
        if (summary == null) emptyList()
        else {
            actions.filter { !it.isCompleted }
                .sortedByDescending { action ->
                    val areaScore = summary?.areaScores?.get(action.areaId) ?: 0
                    (100 - areaScore) * 10 + action.points
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🚀 ROADMAP DE SEGURIDAD RECOMENDADO", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(CyberCyan.copy(alpha = 0.4f), DarkBorder))
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("💡 ALGORITMO DE IMPACTO MÁXIMO", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Este Roadmap analiza tus mayores brechas de seguridad en el polígono y te sugiere el orden óptimo de acciones para maximizar tu nivel de resiliencia rápidamente.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            item {
                Text(
                    text = "PASOS RECOMENDADOS EN ORDEN DE PRIORIDAD",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            itemsIndexed(prioritizedActions) { index, action ->
                RoadmapStepRow(
                    stepNumber = index + 1,
                    action = action,
                    onClick = { onNavigateToArea(action.areaId) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun RoadmapStepRow(
    stepNumber: Int,
    action: SecurityAction,
    onClick: () -> Unit
) {
    val category = remember(action.areaId) { SecurityCategory.fromId(action.areaId) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(CyberCyan.copy(alpha = 0.2f), DarkBorder))
        )
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CyberCyan.copy(alpha = 0.2f))
                    .border(1.dp, CyberCyan, CircleShape)
            ) {
                Text("#$stepNumber", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(category.icon, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(category.title, fontSize = 11.sp, color = TextMuted)
                }

                Text(action.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(action.description, fontSize = 11.sp, color = TextSecondary, maxLines = 2)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(NeonLime.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("+${action.points} PTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonLime)
            }
        }
    }
}
