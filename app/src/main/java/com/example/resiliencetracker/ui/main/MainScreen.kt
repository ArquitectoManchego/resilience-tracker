package com.example.resiliencetracker.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.example.resiliencetracker.*
import com.example.resiliencetracker.data.ResilienceSummary
import com.example.resiliencetracker.data.SecurityCategory
import com.example.resiliencetracker.theme.*
import com.example.resiliencetracker.ui.components.FuturisticRadarChart

@Composable
fun MainScreen(
    onNavigate: (NavKey) -> Unit,
    viewModel: MainScreenViewModel,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.summaryState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Header Banner
        item {
            HeaderBanner(summary = summary)
        }

        // 2. Futuristic Radar Chart Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🌐 MATRIZ DE SEGURIDAD POLIGONAL",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Toca un vértice para ver brechas",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FuturisticRadarChart(
                        scores = summary.areaScores,
                        onAreaClick = { areaId ->
                            onNavigate(AreaDetailNavKey(areaId))
                        }
                    )
                }
            }
        }

        // 3. Quick Action Hub (Roadmap, Inventario, Simulacros, Biblioteca)
        item {
            QuickHubSection(
                summary = summary,
                onNavigate = onNavigate
            )
        }

        // 4. Security Areas Grid / List
        item {
            Text(
                text = "⚡ ÁREAS DE SEGURIDAD (12 MATRICES)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
            )
        }

        items(SecurityCategory.values()) { category ->
            val score = summary.areaScores[category.id] ?: 0
            SecurityAreaRow(
                category = category,
                scorePercent = score,
                onClick = { onNavigate(AreaDetailNavKey(category.id)) }
            )
        }
    }
}

@Composable
private fun HeaderBanner(summary: ResilienceSummary) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        DarkCard,
                        Color(0xFF0F172A),
                        DarkSurface
                    )
                )
            )
            .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RESILIENCE TRACKER",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = CyberCyan,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = summary.tierName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonLime
                    )
                }

                // Global Score Circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(CyberCyan.copy(alpha = 0.15f))
                        .border(2.dp, CyberCyan, RoundedCornerShape(32.dp))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${summary.totalScore}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberCyan
                        )
                        Text(
                            text = "SCORE",
                            fontSize = 8.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                QuickStatItem(
                    label = "Acciones",
                    value = "${summary.completedActionsCount}/${summary.totalActionsCount}",
                    color = CyberCyan
                )
                QuickStatItem(
                    label = "Inventario Alerta",
                    value = "${summary.expiringItemsCount}",
                    color = if (summary.expiringItemsCount > 0) AlertRed else NeonLime
                )
                QuickStatItem(
                    label = "Simulacros Pend.",
                    value = "${summary.activeDrillsCount}",
                    color = WarningGold
                )
            }
        }
    }
}

@Composable
private fun QuickStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun QuickHubSection(
    summary: ResilienceSummary,
    onNavigate: (NavKey) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        item {
            HubChip(
                title = "🚀 Roadmap",
                subtitle = "Ver tareas prioritarias",
                badgeColor = CyberCyan,
                onClick = { onNavigate(RoadmapNavKey) }
            )
        }
        item {
            HubChip(
                title = "📦 Inventario",
                subtitle = "${summary.expiringItemsCount} requieren atención",
                badgeColor = if (summary.expiringItemsCount > 0) AlertRed else NeonLime,
                onClick = { onNavigate(InventoryNavKey) }
            )
        }
        item {
            HubChip(
                title = "🚨 Simulacros",
                subtitle = "${summary.activeDrillsCount} por realizar",
                badgeColor = WarningGold,
                onClick = { onNavigate(DrillsNavKey) }
            )
        }
        item {
            HubChip(
                title = "📚 Biblioteca",
                subtitle = "Manuales offline",
                badgeColor = ElectricPurple,
                onClick = { onNavigate(LibraryNavKey) }
            )
        }
    }
}

@Composable
private fun HubChip(
    title: String,
    subtitle: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextMuted,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SecurityAreaRow(
    category: SecurityCategory,
    scorePercent: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, DarkBorder)))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.icon,
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = category.description,
                    fontSize = 11.sp,
                    color = TextMuted,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Mini Progress Bar
                LinearProgressIndicator(
                    progress = { scorePercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (scorePercent >= 70) NeonLime else if (scorePercent >= 40) CyberCyan else AlertRed,
                    trackColor = DarkCard
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "$scorePercent%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (scorePercent >= 70) NeonLime else if (scorePercent >= 40) CyberCyan else AlertRed,
                textAlign = TextAlign.End
            )
        }
    }
}
