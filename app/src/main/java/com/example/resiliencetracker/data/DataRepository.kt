package com.example.resiliencetracker.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

interface DataRepository {
    val actionsFlow: Flow<List<SecurityAction>>
    val inventoryFlow: Flow<List<InventoryItem>>
    val guidesFlow: Flow<List<SurvivalGuide>>
    val drillsFlow: Flow<List<DrillItem>>
    val summaryFlow: Flow<ResilienceSummary>

    fun toggleAction(actionId: String)
    fun toggleSubtask(actionId: String, subtaskId: String)
    fun addInventoryItem(name: String, areaId: String, quantity: String, daysLeft: Int)
    fun removeInventoryItem(id: String)
    fun toggleDrill(drillId: String)
}

class DefaultDataRepository : DataRepository {

    private val actionsState = MutableStateFlow(createInitialActions())
    private val inventoryState = MutableStateFlow(createInitialInventory())
    private val guidesState = MutableStateFlow(createInitialGuides())
    private val drillsState = MutableStateFlow(createInitialDrills())

    override val actionsFlow: Flow<List<SecurityAction>> = actionsState
    override val inventoryFlow: Flow<List<InventoryItem>> = inventoryState
    override val guidesFlow: Flow<List<SurvivalGuide>> = guidesState
    override val drillsFlow: Flow<List<DrillItem>> = drillsState

    override val summaryFlow: Flow<ResilienceSummary> = actionsState.map { actions ->
        val areaScores = mutableMapOf<String, Int>()

        SecurityCategory.values().forEach { category ->
            val areaActions = actions.filter { it.areaId == category.id }
            if (areaActions.isEmpty()) {
                areaScores[category.id] = 0
            } else {
                var earnedPoints = 0
                var maxPoints = 0
                areaActions.forEach { action ->
                    maxPoints += action.points
                    if (action.isCompleted) {
                        earnedPoints += action.points
                    } else if (action.subtasks.isNotEmpty()) {
                        val completedSub = action.subtasks.count { it.isCompleted }
                        val subRatio = completedSub.toFloat() / action.subtasks.size
                        earnedPoints += (action.points * subRatio).toInt()
                    }
                }
                val percent = if (maxPoints > 0) ((earnedPoints.toFloat() / maxPoints) * 100).toInt() else 0
                areaScores[category.id] = percent.coerceIn(0, 100)
            }
        }

        val totalScore = if (areaScores.isNotEmpty()) areaScores.values.average().toInt() else 0

        val tierName = when {
            totalScore >= 85 -> "⚡ Sobreviviente Avanzado"
            totalScore >= 70 -> "🛡️ Nivel Resiliente"
            totalScore >= 50 -> "⚔️ Nivel Táctico"
            totalScore >= 30 -> "📦 Nivel Básico"
            else -> "⚠️ Vulnerable"
        }

        val completedCount = actions.count { it.isCompleted }
        val expiringCount = inventoryState.value.count { it.status != InventoryStatus.OK }
        val activeDrills = drillsState.value.count { !it.isCompleted }

        ResilienceSummary(
            areaScores = areaScores,
            totalScore = totalScore,
            tierName = tierName,
            completedActionsCount = completedCount,
            totalActionsCount = actions.size,
            expiringItemsCount = expiringCount,
            activeDrillsCount = activeDrills
        )
    }

    override fun toggleAction(actionId: String) {
        actionsState.update { current ->
            current.map { action ->
                if (action.id == actionId) {
                    val newCompleted = !action.isCompleted
                    action.copy(
                        isCompleted = newCompleted,
                        subtasks = action.subtasks.map { it.copy(isCompleted = newCompleted) }
                    )
                } else action
            }
        }
    }

    override fun toggleSubtask(actionId: String, subtaskId: String) {
        actionsState.update { current ->
            current.map { action ->
                if (action.id == actionId) {
                    val updatedSubtasks = action.subtasks.map { sub ->
                        if (sub.id == subtaskId) sub.copy(isCompleted = !sub.isCompleted) else sub
                    }
                    val allCompleted = updatedSubtasks.all { it.isCompleted }
                    action.copy(subtasks = updatedSubtasks, isCompleted = allCompleted)
                } else action
            }
        }
    }

    override fun addInventoryItem(name: String, areaId: String, quantity: String, daysLeft: Int) {
        val status = when {
            daysLeft <= 0 -> InventoryStatus.EXPIRED
            daysLeft <= 30 -> InventoryStatus.EXPIRING_SOON
            else -> InventoryStatus.OK
        }
        val newItem = InventoryItem(
            id = "inv_${System.currentTimeMillis()}",
            areaId = areaId,
            name = name,
            quantity = quantity,
            expirationDateString = "En $daysLeft días",
            daysLeft = daysLeft,
            status = status
        )
        inventoryState.update { it + newItem }
    }

    override fun removeInventoryItem(id: String) {
        inventoryState.update { current -> current.filter { it.id != id } }
    }

    override fun toggleDrill(drillId: String) {
        drillsState.update { current ->
            current.map { if (it.id == drillId) it.copy(isCompleted = !it.isCompleted) else it }
        }
    }

    private fun createInitialActions(): List<SecurityAction> {
        return listOf(
            // 1. ALIMENTARIA
            SecurityAction(
                id = "food_1",
                areaId = "alimentaria",
                title = "Despensa de Emergencia 72H",
                description = "Comida lista para consumir sin necesidad de cocinar para 3 días.",
                points = 1,
                isCompleted = true,
                subtasks = listOf(
                    SubTask("f1_1", "Atún y enlatados de alta proteína", 1, true),
                    SubTask("f1_2", "Barras energéticas y frutos secos", 1, true)
                )
            ),
            SecurityAction(
                id = "food_2",
                areaId = "alimentaria",
                title = "Despensa Rotativa 1 a 3 Meses",
                description = "Granos enteros, conservas y rotación contínua de fechas.",
                points = 3,
                isCompleted = false,
                subtasks = listOf(
                    SubTask("f2_1", "25kg de Arroz y Frijol en contenedores sellados", 1, true),
                    SubTask("f2_2", "Aceite vegetal, sal y especias básicas", 1, false)
                )
            ),
            SecurityAction(
                id = "food_3",
                areaId = "alimentaria",
                title = "Producción Propia Autónoma",
                description = "Huerto urbano/hidroponía o crianza de animales pequeños.",
                points = 5,
                isCompleted = false,
                subtasks = listOf(
                    SubTask("f3_1", "Sistema de cultivo en bancales o macetas", 2, false),
                    SubTask("f3_2", "Semillas germinables no híbridas (Heirloom)", 3, false)
                )
            ),

            // 2. AGUA
            SecurityAction(
                id = "water_1",
                areaId = "agua",
                title = "Reserva Doméstica de Agua (3 días)",
                description = "4 litros por persona/día almacenados en recipientes libres de BPA.",
                points = 1,
                isCompleted = true,
                subtasks = listOf(
                    SubTask("w1_1", "Garrafones de agua purificada en lugar oscuro", 1, true)
                )
            ),
            SecurityAction(
                id = "water_2",
                areaId = "agua",
                title = "Sistemas Portátiles de Purificación",
                description = "Filtros de micro-membrana (Sawyer/LifeStraw) y pastillas de cloro.",
                points = 2,
                isCompleted = true,
                subtasks = listOf(
                    SubTask("w2_1", "Filtro Sawyer Squeeze o LifeStraw", 1, true),
                    SubTask("w2_2", "Gotas o pastillas de purificación acuosa", 1, true)
                )
            ),
            SecurityAction(
                id = "water_3",
                areaId = "agua",
                title = "Captación de Lluvia y Filtrado Avanzado",
                description = "Sistema de recolección en techo y filtro de carbón/ósmosis.",
                points = 5,
                isCompleted = false,
                subtasks = listOf(
                    SubTask("w3_1", "Tanque de captación pluvial con desvío de primeras aguas", 2, false),
                    SubTask("w3_2", "Filtro cerámico gravitacional tipo Berkey", 3, false)
                )
            ),

            // 3. ENERGÍA
            SecurityAction(
                id = "energy_1",
                areaId = "energia",
                title = "Baterías Portátiles y Iluminación LED",
                description = "Powerbanks de 20k mAh y linternas recargables/pila.",
                points = 1,
                isCompleted = true,
                subtasks = listOf(
                    SubTask("e1_1", "Linternas frontales LED para cada miembro", 1, true),
                    SubTask("e1_2", "Powerbank USB-C reforzado", 1, true)
                )
            ),
            SecurityAction(
                id = "energy_2",
                areaId = "energia",
                title = "Generador Solar Portátil (Estación de Energía)",
                description = "Batería LiFePO4 de >500Wh con paneles solares plegables.",
                points = 3,
                isCompleted = false,
                subtasks = listOf(
                    SubTask("e2_1", "Estación tipo Jackery/Bluetti", 2, false),
                    SubTask("e2_2", "Panel solar de 100W a 200W", 1, false)
                )
            ),
            SecurityAction(
                id = "energy_3",
                areaId = "energia",
                title = "Sistema Fotovoltaico Residencial Aislado",
                description = "Inversor y baterías de litio independientes de la red pública.",
                points = 5,
                isCompleted = false,
                subtasks = listOf(
                    SubTask("e3_1", "Banco de baterías de 5kWh+", 3, false),
                    SubTask("e3_2", "Inversor híbrido fuera de red (Off-Grid)", 2, false)
                )
            ),

            // 4. CIBERSEGURIDAD
            SecurityAction(
                id = "cyber_1",
                areaId = "ciberseguridad",
                title = "Autenticación 2FA y Gestor de Claves",
                description = "Uso de Bitwarden/KeePass y llaves de seguridad 2FA tipo YubiKey.",
                points = 2,
                isCompleted = true,
                subtasks = listOf(
                    SubTask("c1_1", "Gestor de contraseñas con contraseñas únicas", 1, true),
                    SubTask("c1_2", "Aplicación de 2FA (Aegis/Yubico) activa", 1, true)
                )
            ),
            SecurityAction(
                id = "cyber_2",
                areaId = "ciberseguridad",
                title = "Segmentación de Red Wi-Fi Doméstica",
                description = "Separar dispositivos IoT de la red personal y equipos de trabajo.",
                points = 3,
                isCompleted = false,
                subtasks = listOf(
                    SubTask("c2_1", "VLANs o Red de Invitados aislada para electrodomésticos", 2, false),
                    SubTask("c2_2", "WPA3 Enterprise activado en el Router", 1, false)
                )
            ),
            SecurityAction(
                id = "cyber_3",
                areaId = "ciberseguridad",
                title = "Hardware Firewall Doméstico",
                description = "pfSense / OPNsense bloqueando conexiones sospechosas entrantes.",
                points = 5,
                isCompleted = false,
                subtasks = listOf(
                    SubTask("c3_1", "Mini PC con pfSense/OPNsense configurado", 3, false),
                    SubTask("c3_2", "VPN WireGuard autohospedada para acceso remoto seguro", 2, false)
                )
            ),

            // 5. DATOS
            SecurityAction(
                id = "data_1",
                areaId = "datos",
                title = "Bóveda de Documentos Críticos (Físico/Digital)",
                description = "Actas, escrituras y pasaportes en caja ignífuga y USB cifrado.",
                points = 2,
                isCompleted = true,
                subtasks = listOf(
                    SubTask("d1_1", "Bolsa/Caja ignífuga e impermeable para papeles", 1, true),
                    SubTask("d1_2", "Memoria USB cifrada con VeraCrypt conteniendo scannings", 1, true)
                )
            ),
            SecurityAction(
                id = "data_2",
                areaId = "datos",
                title = "Estrategia de Backup 3-2-1",
                description = "3 copias de datos, 2 medios distintos, 1 fuera de sitio.",
                points = 4,
                isCompleted = false,
                subtasks = listOf(
                    SubTask("d2_1", "Disco duro externo con cifrado LUKS/BitLocker", 2, false),
                    SubTask("d2_2", "Copia remota cifrada de extremo a extremo", 2, false)
                )
            ),

            // 6. ECONÓMICA
            SecurityAction(
                id = "econ_1",
                areaId = "economica",
                title = "Reserva de Efectivo en Casa",
                description = "Billetes de baja denominación guardados en lugar seguro.",
                points = 2,
                isCompleted = true,
                subtasks = listOf(
                    SubTask("ec1_1", "Efectivo para 2 semanas de compras esenciales", 2, true)
                )
            ),
            SecurityAction(
                id = "econ_2",
                areaId = "economica",
                title = "Fondo de Emergencia Líquido (3-6 Meses)",
                description = "Capital accesible rápidamente en cuenta sin riesgo de mercado.",
                points = 3,
                isCompleted = false,
                subtasks = listOf(
                    SubTask("ec2_1", "Fondo equivalente a 3 meses de costo operativo", 3, false)
                )
            ),
            SecurityAction(
                id = "econ_3",
                areaId = "economica",
                title = "Cobertura Anti-Inflación y Metales Preciosos",
                description = "Monedas de plata/oro físico y activos no correlacionados.",
                points = 5,
                isCompleted = false,
                subtasks = listOf(
                    SubTask("ec3_1", "Onzas de plata pura fraccionadas para trueque", 3, false),
                    SubTask("ec3_2", "Resguardo de valor diversificado fuera del sistema bancario", 2, false)
                )
            ),

            // 7. FAMILIAR
            SecurityAction(
                id = "fam_1",
                areaId = "familiar",
                title = "Reforzamiento de Puntos de Acceso",
                description = "Cerraduras de seguridad, escudos y sensores de apertura.",
                points = 3,
                isCompleted = true,
                subtasks = listOf(
                    SubTask("fm1_1", "Pernos de 3 pulgadas en marcos de puertas", 1, true),
                    SubTask("fm1_2", "Película de seguridad anti-impacto en ventanas", 2, true)
                )
            ),
            SecurityAction(
                id = "fam_2",
                areaId = "familiar",
                title = "Sistema de Cámaras CCTV Local",
                description = "Grabación en NVR local sin depender de servidores en la nube.",
                points = 4,
                isCompleted = false,
                subtasks = listOf(
                    SubTask("fm2_1", "Cámaras IP PoE en perímetro", 2, false),
                    SubTask("fm2_2", "NVR con respaldo de batería UPS", 2, false)
                )
            ),

            // 8. SALUD
            SecurityAction(
                id = "health_1",
                areaId = "salud",
                title = "Botiquín IFAK (Individual First Aid Kit)",
                description = "Torniquete CAT Gen7, gasa hemostática y vendaje israelí.",
                points = 3,
                isCompleted = true,
                subtasks = listOf(
                    SubTask("h1_1", "Torniquete homologado CAT o SOFTT-W", 2, true),
                    SubTask("h1_2", "Tijeras de trauma y guantes de nitrilo", 1, true)
                )
            ),
            SecurityAction(
                id = "health_2",
                areaId = "salud",
                title = "Capacitación en Soporte Vital y Hemorragias",
                description = "Curso presencial Stop The Bleed y RCP actualizado.",
                points = 5,
                isCompleted = false,
                subtasks = listOf(
                    SubTask("h2_1", "Certificado Stop The Bleed / Primeros Auxilios", 3, false),
                    SubTask("h2_2", "Práctica regular de aplicación de torniquete", 2, false)
                )
            ),

            // 9. COMUNICACIONES
            SecurityAction(
                id = "comms_1",
                areaId = "comunicaciones",
                title = "Radios de Onda Corta PMR446 / Baofeng",
                description = "Radios bidireccionales cargadas con frecuencias locales guardadas.",
                points = 2,
                isCompleted = true,
                subtasks = listOf(
                    SubTask("cm1_1", "Par de radios reprogramados en canal familiar", 2, true)
                )
            ),
            SecurityAction(
                id = "comms_2",
                areaId = "comunicaciones",
                title = "Estación de Radioaficionado (HAM Radio)",
                description = "Radio HF/VHF/UHF para recibir boletines en situaciones de desastre.",
                points = 5,
                isCompleted = false,
                subtasks = listOf(
                    SubTask("cm2_1", "Antena de alto rendimiento instalada", 2, false),
                    SubTask("cm2_2", "Licencia HAM o estación receptora SDR lista", 3, false)
                )
            ),

            // 10. REFUGIO Y EVACUACIÓN
            SecurityAction(
                id = "shelter_1",
                areaId = "refugio",
                title = "Mochilas de Evacuación Rápida (Bug-Out Bag)",
                description = "Equipamiento para sobrevivir 72 horas en tránsito.",
                points = 3,
                isCompleted = true,
                subtasks = listOf(
                    SubTask("s1_1", "Muro de herramientas, muda de ropa y mapa físico", 1, true),
                    SubTask("s1_2", "Manta térmica, lona y cuerdas de paracord", 2, true)
                )
            ),
            SecurityAction(
                id = "shelter_2",
                areaId = "refugio",
                title = "Plan de Evacuación Familiar y Rutas A/B",
                description = "Puntos de encuentro acordados previamente fuera del área urbana.",
                points = 4,
                isCompleted = false,
                subtasks = listOf(
                    SubTask("s2_1", "Rutas alternativas sin autopistas marcadas en mapa impreso", 2, false),
                    SubTask("s2_2", "Ensayo del plan de evacuación en tiempo real", 2, false)
                )
            ),

            // 11. RED COMUNITARIA
            SecurityAction(
                id = "comm_1",
                areaId = "comunidad",
                title = "Red de Contactos de Confianza y Vecindario",
                description = "Plan de apoyo mutuo con al menos 3 familias cercanas.",
                points = 3,
                isCompleted = true,
                subtasks = listOf(
                    SubTask("co1_1", "Intercambio de frecuencias de radio y claves de aviso", 3, true)
                )
            ),

            // 12. NRBQ
            SecurityAction(
                id = "cbrn_1",
                areaId = "nrbq",
                title = "Protección Respiratoria Avanzada (N95 / CBRN)",
                description = "Máscaras panorámicas y filtros para partículas y gases tóxicos.",
                points = 3,
                isCompleted = false,
                subtasks = listOf(
                    SubTask("cb1_1", "Máscara panorámica con filtro NATO 40mm", 2, false),
                    SubTask("cb1_2", "Pastillas de Yoduro de Potasio (KI) para eventos nucleares", 1, false)
                )
            )
        )
    }

    private fun createInitialInventory(): List<InventoryItem> {
        return listOf(
            InventoryItem("inv_1", "alimentaria", "Atún en lata (Pack 12)", "12 latas", "En 15 días", 15, InventoryStatus.EXPIRING_SOON),
            InventoryItem("inv_2", "salud", "Pastillas de Cloro Purificadoras", "100 tabletas", "En 120 días", 120, InventoryStatus.OK),
            InventoryItem("inv_3", "salud", "Gasa hemostática QuikClot", "2 sobres", "Vencido hace 3 días", -3, InventoryStatus.EXPIRED),
            InventoryItem("inv_4", "energia", "Pilas CR123A para Linterna Táctica", "8 unidades", "En 25 días", 25, InventoryStatus.EXPIRING_SOON),
            InventoryItem("inv_5", "alimentaria", "Arroz blanco en cuenco sellado Mylar", "20 kg", "En 730 días", 730, InventoryStatus.OK)
        )
    }

    private fun createInitialGuides(): List<SurvivalGuide> {
        return listOf(
            SurvivalGuide(
                id = "guide_water",
                areaId = "agua",
                title = "Regla de los 3 Minutos / 3 Días",
                subtitle = "Métodos de Purificación en Terreno",
                content = """
                    Sin agua potable, el cuerpo humano sufre deshidratación severa en menos de 72 horas.
                    
                    MÉTODOS DE PURIFICACIÓN:
                    1. Hervido: Hervir durante al menos 1 minuto a borbollones (3 min en gran altitud). Elimina bacterias, virus y parásitos.
                    2. Filtros de Micro-membrana: Usar filtros de 0.1 micras (Sawyer). Retienen bacterias y protozoos, pero NO virus ni químicos.
                    3. Cloración: 2 gotas de cloro doméstico sin aroma por litro de agua clara. Esperar 30 minutos antes de consumir.
                    4. Destilación solar: Ideal para agua contaminada o salada.
                """.trimIndent(),
                keyPoints = listOf("Hervir 1 minuto", "2 gotas de cloro por litro", "Filtro 0.1 micras para bacterias")
            ),
            SurvivalGuide(
                id = "guide_health",
                areaId = "salud",
                title = "Control de Hemorragias Masivas (MARCH)",
                subtitle = "Protocolo de Emergencia Trauma",
                content = """
                    La pérdida masiva de sangre puede ser fatal en menos de 3 minutos.
                    
                    PROTOCOLO DE ACTUACIÓN:
                    1. M (Massive Bleeding): Colocar torniquete CAT 5 a 7 cm por encima de la herida en extremidades. Ajustar hasta cortar el pulso distal.
                    2. A (Airway): Verificar que las vías aéreas estén libres.
                    3. R (Respiration): Sellar heridas de tórax con parche oclusivo (Chest Seal).
                    4. C (Circulation): Aplicar gasa hemostática y empaquetar la herida con presión firme de 3 minutos.
                    5. H (Hypothermia): Cubrir a la víctima con manta térmica reflejante.
                """.trimIndent(),
                keyPoints = listOf("Torniquete a 5-7cm de la herida", "Empaquetar herida con gasa", "Manta térmica contra hipotermia")
            ),
            SurvivalGuide(
                id = "guide_cyber",
                areaId = "ciberseguridad",
                title = "Higiene Digital en Tiempos de Crisis",
                subtitle = "Seguridad de Comunicaciones y Privacidad",
                content = """
                    En situaciones de conflicto o censura de red:
                    
                    1. Desactivar biometría (huella/rostro) en el teléfono; usar solo clave alfanumérica de 6+ dígitos.
                    2. Cifrar mensajes usando aplicaciones de código abierto con cifrado E2E como Signal o Briar (mesh offline).
                    3. Mantener mapas offline descargados en OsmAnd o Maps.me.
                    4. Tener copias de seguridad de claves cifradas en USB de aluminio resistente a agua/golpes.
                """.trimIndent(),
                keyPoints = listOf("Clave alfanumérica en vez de huella", "Mapas descargados en OsmAnd", "App Briar para Mesh sin Internet")
            ),
            SurvivalGuide(
                id = "guide_cbrn",
                areaId = "nrbq",
                title = "Resguardo Ante Evento Radiológico / Nuclear",
                subtitle = "Protocolo de Refugio y Descontaminación",
                content = """
                    En caso de detonación o accidente nuclear cercano:
                    
                    1. ENTRAR: Refugiarse en el centro de un edificio de concreto o sótano inmediatamente.
                    2. PERMANECER: Permanecer adentro durante al menos 24 a 48 horas (la radiación decae 90% en las primeras 24h).
                    3. SELLAR: Apagar aire acondicionado y sellar rendijas con plástico y cinta adhesiva.
                    4. DESCONTAMINAR: Quitarse la ropa exterior (elimina 90% de partículas radiadas) y lavarse suavemente con agua y jabón.
                """.trimIndent(),
                keyPoints = listOf("Refugio en sótano o concreto", "Permanecer adentro 24-48 horas", "Quitarse ropa exterior para descontaminar")
            )
        )
    }

    private fun createInitialDrills(): List<DrillItem> {
        return listOf(
            DrillItem(
                id = "drill_1",
                title = "⚡ Simulacro de Apagón General (Blackout 12h)",
                areaId = "energia",
                description = "Desconecta el interruptor principal de energía de tu casa por 4 horas. Evalúa linternas, cocción sin luz y temperatura.",
                difficulty = "Media",
                isCompleted = false,
                scorePoints = 15
            ),
            DrillItem(
                id = "drill_2",
                title = "🎒 Simulacro de Evacuación de Emergencia 5 min",
                areaId = "refugio",
                description = "Haz sonar una alarma aleatoria. Todos los miembros deben tomar sus mochilas Bug-Out y estar en el vehículo en < 5 minutos.",
                difficulty = "Alta",
                isCompleted = true,
                scorePoints = 25
            ),
            DrillItem(
                id = "drill_3",
                title = "📻 Prueba de Red MESH de Radio",
                areaId = "comunicaciones",
                description = "Envía un mensaje de prueba a un contacto de confianza a 2km de distancia usando radios PMR/Baofeng.",
                difficulty = "Fácil",
                isCompleted = false,
                scorePoints = 10
            )
        )
    }
}
