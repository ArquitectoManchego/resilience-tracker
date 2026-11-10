package com.example.resiliencetracker.data

enum class SecurityCategory(
    val id: String,
    val title: String,
    val icon: String,
    val description: String
) {
    FOOD("alimentaria", "Seguridad Alimentaria", "🌾", "Alimentos, despensa y producción propia"),
    WATER("agua", "Seguridad Hídrica", "💧", "Almacenamiento, potabilización y fuentes"),
    ENERGY("energia", "Seguridad Energética", "⚡", "Respaldo eléctrico, paneles y generadores"),
    CYBER("ciberseguridad", "Ciberseguridad", "🛡️", "2FA, contraseñas, firewalls e higiene digital"),
    DATA("datos", "Protección de Datos", "💾", "Respaldos 3-2-1, NAS y documentos físicos"),
    ECONOMIC("economica", "Seguridad Económica", "💰", "Efectivo, metales, fondo de emergencia"),
    FAMILY("familiar", "Seguridad Familiar", "🏠", "Reforzamiento de hogar, alarmas y defensa"),
    HEALTH("salud", "Salud y Auxilios", "🩺", "Botiquines IFAK, medicinas y formación"),
    COMMS("comunicaciones", "Comunicaciones", "📻", "Radios VHF/UHF, Ham Radio y satelital"),
    SHELTER("refugio", "Refugio y Evacuación", "🎒", "Mochilas Bug-Out, vehículos y rutas"),
    COMMUNITY("comunidad", "Red de Apoyo", "👥", "Vecinos de confianza, grupos MAG y trueque"),
    CBRN("nrbq", "Defensa NRBQ", "☣️", "Máscaras CBRN, trajes y detectores Geiger");

    companion object {
        fun fromId(id: String): SecurityCategory = values().find { it.id == id } ?: FOOD
    }
}

data class SubTask(
    val id: String,
    val title: String,
    val points: Int,
    val isCompleted: Boolean
)

data class SecurityAction(
    val id: String,
    val areaId: String,
    val title: String,
    val description: String,
    val points: Int,
    val isCompleted: Boolean,
    val subtasks: List<SubTask> = emptyList(),
    val isPerishable: Boolean = false,
    val expirationDaysLeft: Int? = null
)

enum class InventoryStatus {
    OK,
    EXPIRING_SOON,
    EXPIRED
}

data class InventoryItem(
    val id: String,
    val areaId: String,
    val name: String,
    val quantity: String,
    val expirationDateString: String,
    val daysLeft: Int,
    val status: InventoryStatus
)

data class SurvivalGuide(
    val id: String,
    val areaId: String,
    val title: String,
    val subtitle: String,
    val content: String,
    val keyPoints: List<String>
)

data class DrillItem(
    val id: String,
    val title: String,
    val areaId: String,
    val description: String,
    val difficulty: String,
    val isCompleted: Boolean,
    val scorePoints: Int
)

data class ResilienceSummary(
    val areaScores: Map<String, Int>, // percentage 0..100 per areaId
    val totalScore: Int, // percentage 0..100
    val tierName: String,
    val completedActionsCount: Int,
    val totalActionsCount: Int,
    val expiringItemsCount: Int,
    val activeDrillsCount: Int
)
