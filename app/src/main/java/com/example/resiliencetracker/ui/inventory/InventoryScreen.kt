package com.example.resiliencetracker.ui.inventory

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
import com.example.resiliencetracker.data.InventoryItem
import com.example.resiliencetracker.data.InventoryStatus
import com.example.resiliencetracker.data.SecurityCategory
import com.example.resiliencetracker.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    repository: DataRepository,
    onBack: () -> Unit
) {
    val items by repository.inventoryFlow.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📦 GESTOR DE INVENTARIO Y CADUCIDAD", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = CyberCyan,
                contentColor = DarkBackground
            ) {
                Text("➕", fontSize = 18.sp)
            }
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
                    text = "Rastrea artículos perecederos, filtros de agua, medicinas o baterías antes de que pierdan su efectividad.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            items(items) { item ->
                InventoryItemRow(
                    item = item,
                    onDelete = { repository.removeInventoryItem(item.id) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    if (showDialog) {
        AddItemDialog(
            onDismiss = { showDialog = false },
            onAdd = { name, areaId, qty, days ->
                repository.addInventoryItem(name, areaId, qty, days)
                showDialog = false
            }
        )
    }
}

@Composable
private fun InventoryItemRow(
    item: InventoryItem,
    onDelete: () -> Unit
) {
    val category = remember(item.areaId) { SecurityCategory.fromId(item.areaId) }

    val statusColor = when (item.status) {
        InventoryStatus.EXPIRED -> AlertRed
        InventoryStatus.EXPIRING_SOON -> WarningGold
        InventoryStatus.OK -> NeonLime
    }

    val statusText = when (item.status) {
        InventoryStatus.EXPIRED -> "⚠️ VENCIDO"
        InventoryStatus.EXPIRING_SOON -> "⏳ PRÓXIMO A VENCER"
        InventoryStatus.OK -> "✅ EN REGLA"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(statusColor.copy(alpha = 0.5f), DarkBorder))
        )
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(category.icon, fontSize = 22.sp, modifier = Modifier.padding(end = 12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Cantidad: ${item.quantity} | Área: ${category.title}", fontSize = 11.sp, color = TextMuted)
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$statusText (${item.expirationDateString})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Text("🗑️", fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, areaId: String, quantity: String, daysLeft: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var daysLeftText by remember { mutableStateOf("30") }
    val selectedCategory by remember { mutableStateOf(SecurityCategory.FOOD) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Artículo", color = CyberCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del artículo") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan)
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Cantidad (Ej: 10 latas, 2 botellas)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan)
                )
                OutlinedTextField(
                    value = daysLeftText,
                    onValueChange = { daysLeftText = it },
                    label = { Text("Días faltantes para vencer") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = daysLeftText.toIntOrNull() ?: 30
                    if (name.isNotBlank()) {
                        onAdd(name, selectedCategory.id, quantity.ifBlank { "1" }, days)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = DarkBackground)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextMuted) }
        },
        containerColor = DarkSurface
    )
}
