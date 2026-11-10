package com.example.resiliencetracker.ui.drills

import androidx.compose.foundation.background
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
import com.example.resiliencetracker.data.DrillItem
import com.example.resiliencetracker.data.SecurityCategory
import com.example.resiliencetracker.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrillsScreen(
    repository: DataRepository,
    onBack: () -> Unit
) {
    val drills by repository.drillsFlow.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🚨 GAMIFICACIÓN Y SIMULACROS", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
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
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(WarningGold.copy(alpha = 0.4f), DarkBorder))
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🎯 GAMIFICACIÓN DE PREPARACIÓN", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarningGold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Pon a prueba tus equipos y tiempo de reacción con simulacros periódicos. Al completarlos ganas puntos extra y desbloqueas insignias tácticas.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            item {
                Text(
                    text = "SIMULACROS ACTIVOS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(drills) { drill ->
                DrillCard(
                    drill = drill,
                    onToggle = { repository.toggleDrill(drill.id) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun DrillCard(
    drill: DrillItem,
    onToggle: () -> Unit
) {
    val category = remember(drill.areaId) { SecurityCategory.fromId(drill.areaId) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (drill.isCompleted) DarkSurface.copy(alpha = 0.8f) else DarkCard),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                listOf(if (drill.isCompleted) NeonLime.copy(alpha = 0.5f) else WarningGold.copy(alpha = 0.3f), DarkBorder)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = drill.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = NeonLime, uncheckedColor = TextMuted)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(category.icon, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(drill.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (drill.isCompleted) NeonLime else TextPrimary)
                }

                Text(drill.description, fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(WarningGold.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Dificultad: ${drill.difficulty}", fontSize = 10.sp, color = WarningGold, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("+${drill.scorePoints} PTS EXTRA", fontSize = 10.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
