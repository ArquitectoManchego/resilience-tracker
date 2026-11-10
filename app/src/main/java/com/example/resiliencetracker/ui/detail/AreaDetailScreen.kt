package com.example.resiliencetracker.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun AreaDetailScreen(
    areaId: String,
    repository: DataRepository,
    onBack: () -> Unit,
    onNavigateToGuide: (String) -> Unit
) {
    val category = remember(areaId) { SecurityCategory.fromId(areaId) }
    val actions by repository.actionsFlow.collectAsState(initial = emptyList())
    val areaActions = remember(actions, areaId) { actions.filter { it.areaId == areaId } }

    val totalPoints = remember(areaActions) { areaActions.sumOf { it.points } }
    val earnedPoints = remember(areaActions) {
        areaActions.sumOf { action ->
            if (action.isCompleted) action.points
            else if (action.subtasks.isNotEmpty()) {
                val done = action.subtasks.count { it.isCompleted }
                (action.points * (done.toFloat() / action.subtasks.size)).toInt()
            } else 0
        }
    }

    val scorePercent = if (totalPoints > 0) ((earnedPoints.toFloat() / totalPoints) * 100).toInt() else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(category.icon, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            category.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                },
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
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(CyberCyan.copy(alpha = 0.3f), DarkBorder)
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = category.description,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NIVEL CONSOLIDADO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan
                            )
                            Text(
                                text = "$scorePercent% ($earnedPoints / $totalPoints pts)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (scorePercent >= 70) NeonLime else if (scorePercent >= 40) CyberCyan else AlertRed
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { scorePercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (scorePercent >= 70) NeonLime else if (scorePercent >= 40) CyberCyan else AlertRed,
                            trackColor = DarkCard
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = { onNavigateToGuide(areaId) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("📚 Ver Manual de Supervivencia", color = CyberCyan, fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "ACCIONES E IMPLEMENTACIONES ($earnedPoints pts ganados)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(areaActions) { action ->
                ActionCard(
                    action = action,
                    onToggleAction = { repository.toggleAction(action.id) },
                    onToggleSubtask = { subId -> repository.toggleSubtask(action.id, subId) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ActionCard(
    action: SecurityAction,
    onToggleAction: () -> Unit,
    onToggleSubtask: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (action.isCompleted) DarkSurface.copy(alpha = 0.9f) else DarkCard
        ),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                listOf(
                    if (action.isCompleted) NeonLime.copy(alpha = 0.5f) else DarkBorder,
                    DarkBorder
                )
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = action.isCompleted,
                    onCheckedChange = { onToggleAction() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = NeonLime,
                        uncheckedColor = TextMuted,
                        checkmarkColor = DarkBackground
                    )
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = action.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (action.isCompleted) NeonLime else TextPrimary
                    )
                    Text(
                        text = action.description,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+${action.points} PTS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                }
            }

            if (action.subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = DarkBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sub-tareas consolidadas:",
                    fontSize = 11.sp,
                    color = TextMuted
                )

                action.subtasks.forEach { sub ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleSubtask(sub.id) }
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (sub.isCompleted) "✔" else "○",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (sub.isCompleted) NeonLime else TextMuted.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = sub.title,
                            fontSize = 12.sp,
                            color = if (sub.isCompleted) TextPrimary else TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "+${sub.points} pt",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}
