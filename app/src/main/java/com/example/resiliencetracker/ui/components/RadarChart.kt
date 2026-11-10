package com.example.resiliencetracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.resiliencetracker.data.SecurityCategory
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FuturisticRadarChart(
    scores: Map<String, Int>,
    onAreaClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = remember { SecurityCategory.values() }
    val count = categories.size // 12 points

    val animatedScores = categories.map { category ->
        val target = (scores[category.id] ?: 0) / 100f
        animateFloatAsState(
            targetValue = target,
            animationSpec = tween(durationMillis = 1000),
            label = "score_${category.id}"
        ).value
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(scores) {
                    detectTapGestures { tapOffset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val radius = minOf(centerX, centerY) * 0.72f

                        // Determine closest vertex
                        var minDistance = Float.MAX_VALUE
                        var closestCategory: String? = null

                        categories.forEachIndexed { index, category ->
                            val angle = (2 * PI / count) * index - (PI / 2)
                            val vertexX = centerX + radius * cos(angle).toFloat()
                            val vertexY = centerY + radius * sin(angle).toFloat()

                            val dx = tapOffset.x - vertexX
                            val dy = tapOffset.y - vertexY
                            val dist = kotlin.math.sqrt(dx * dx + dy * dy)

                            if (dist < minDistance) {
                                minDistance = dist
                                closestCategory = category.id
                            }
                        }

                        // Also check click relative to center angle
                        val dx = tapOffset.x - centerX
                        val dy = tapOffset.y - centerY
                        var touchAngle = kotlin.math.atan2(dy, dx) + (PI / 2)
                        if (touchAngle < 0) touchAngle += 2 * PI

                        val anglePerSector = 2 * PI / count
                        val sectorIndex = ((touchAngle + anglePerSector / 2) / anglePerSector).toInt() % count
                        val sectorCategory = categories[sectorIndex].id

                        onAreaClick(closestCategory ?: sectorCategory)
                    }
                }
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = minOf(centerX, centerY) * 0.68f

            val gridColor = Color(0xFF1E293B)
            val gridGlowColor = Color(0xFF00F0FF).copy(alpha = 0.2f)
            val strokeCyan = Color(0xFF00F0FF)
            val neonLime = Color(0xFF39FF14)

            // 1. Draw Concentric 12-Polygon Webs (20%, 40%, 60%, 80%, 100%)
            val webLevels = listOf(0.2f, 0.4f, 0.6f, 0.8f, 1.0f)
            webLevels.forEach { level ->
                val webPath = Path()
                for (i in 0 until count) {
                    val angle = (2 * PI / count) * i - (PI / 2)
                    val r = radius * level
                    val x = centerX + r * cos(angle).toFloat()
                    val y = centerY + r * sin(angle).toFloat()
                    if (i == 0) webPath.moveTo(x, y) else webPath.lineTo(x, y)
                }
                webPath.close()
                drawPath(
                    path = webPath,
                    color = if (level == 1.0f) gridGlowColor else gridColor,
                    style = Stroke(width = if (level == 1.0f) 2.dp.toPx() else 1.dp.toPx())
                )
            }

            // 2. Draw Radial Axis Lines
            for (i in 0 until count) {
                val angle = (2 * PI / count) * i - (PI / 2)
                val x = centerX + radius * cos(angle).toFloat()
                val y = centerY + radius * sin(angle).toFloat()
                drawLine(
                    color = gridColor,
                    start = Offset(centerX, centerY),
                    end = Offset(x, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 3. Draw Actual Data Polygon (Filled + Glowing Contour)
            val dataPath = Path()
            val pointOffsets = mutableListOf<Offset>()

            categories.forEachIndexed { i, _ ->
                val scoreFactor = animatedScores[i].coerceAtLeast(0.05f) // min 5% for visual vertex
                val angle = (2 * PI / count) * i - (PI / 2)
                val r = radius * scoreFactor
                val x = centerX + r * cos(angle).toFloat()
                val y = centerY + r * sin(angle).toFloat()

                pointOffsets.add(Offset(x, y))
                if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
            }
            dataPath.close()

            // Fill Polygon with translucent neon gradient
            drawPath(
                path = dataPath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        strokeCyan.copy(alpha = 0.45f),
                        neonLime.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = radius
                )
            )

            // Draw Polygon Contour
            drawPath(
                path = dataPath,
                color = strokeCyan,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // 4. Draw Glowing Nodes at Data Points
            pointOffsets.forEach { point ->
                drawCircle(
                    color = strokeCyan.copy(alpha = 0.3f),
                    radius = 8.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = neonLime,
                    radius = 4.dp.toPx(),
                    center = point
                )
            }

            // 5. Draw Labels around the Perimeter
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#94A3B8")
                textSize = 24f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }

            val textPaintActive = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#00F0FF")
                textSize = 26f
                isFakeBoldText = true
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }

            categories.forEachIndexed { i, category ->
                val angle = (2 * PI / count) * i - (PI / 2)
                val labelRadius = radius * 1.22f
                val x = centerX + labelRadius * cos(angle).toFloat()
                val y = centerY + labelRadius * sin(angle).toFloat() + 8f

                val scorePct = scores[category.id] ?: 0
                val labelText = "${category.icon} $scorePct%"

                drawContext.canvas.nativeCanvas.drawText(
                    labelText,
                    x,
                    y,
                    if (scorePct > 50) textPaintActive else textPaint
                )
            }
        }
    }
}
