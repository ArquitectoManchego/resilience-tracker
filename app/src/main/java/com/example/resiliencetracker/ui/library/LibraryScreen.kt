package com.example.resiliencetracker.ui.library

import androidx.compose.animation.AnimatedVisibility
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
import com.example.resiliencetracker.data.SecurityCategory
import com.example.resiliencetracker.data.SurvivalGuide
import com.example.resiliencetracker.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    initialAreaId: String? = null,
    repository: DataRepository,
    onBack: () -> Unit
) {
    val guides by repository.guidesFlow.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📚 BIBLIOTECA DE SUPERVIVENCIA OFFLINE", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
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
                Text(
                    text = "Manuales y protocolos descargados directamente en tu dispositivo para consulta rápida sin conexión a Internet.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            items(guides) { guide ->
                GuideCard(guide = guide, defaultExpanded = guide.areaId == initialAreaId)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun GuideCard(guide: SurvivalGuide, defaultExpanded: Boolean) {
    var expanded by remember { mutableStateOf(defaultExpanded) }
    val category = remember(guide.areaId) { SecurityCategory.fromId(guide.areaId) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(CyberCyan.copy(alpha = 0.4f), DarkBorder))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(category.icon, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(guide.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                    Text(guide.subtitle, fontSize = 12.sp, color = TextSecondary)
                }

                Text(if (expanded) "▲" else "▼", fontSize = 14.sp, color = CyberCyan)
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    HorizontalDivider(color = DarkBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("PUNTOS CLAVE:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonLime)
                    guide.keyPoints.forEach { point ->
                        Text("• $point", fontSize = 12.sp, color = TextPrimary, modifier = Modifier.padding(vertical = 2.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkCard)
                            .padding(12.dp)
                    ) {
                        Text(guide.content, fontSize = 12.sp, color = TextSecondary, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}
