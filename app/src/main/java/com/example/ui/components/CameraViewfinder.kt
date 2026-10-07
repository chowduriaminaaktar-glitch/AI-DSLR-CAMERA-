package com.example.ui.components

import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.GridType
import kotlinx.coroutines.delay

@Composable
fun CameraViewfinder(
    modifier: Modifier = Modifier,
    previewView: PreviewView,
    gridType: GridType,
    showLevelIndicator: Boolean,
    onTapToFocus: (Float, Float) -> Unit,
    onPinchZoom: (Float) -> Unit
) {
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    var showFocusRing by remember { mutableStateOf(false) }

    LaunchedEffect(focusPoint) {
        if (focusPoint != null) {
            showFocusRing = true
            delay(1500)
            showFocusRing = false
            focusPoint = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    if (zoom != 1f) {
                        onPinchZoom(zoom)
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    focusPoint = offset
                    onTapToFocus(offset.x, offset.y)
                }
            }
    ) {
        // Camera PreviewView
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Grid lines overlay
        if (gridType != GridType.NONE) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 1.dp.toPx()
                val gridColor = Color(0x66FFFFFF)

                if (gridType == GridType.RULE_OF_THIRDS) {
                    val oneThirdX = size.width / 3f
                    val twoThirdX = size.width * 2f / 3f
                    val oneThirdY = size.height / 3f
                    val twoThirdY = size.height * 2f / 3f

                    // Vertical lines
                    drawLine(gridColor, Offset(oneThirdX, 0f), Offset(oneThirdX, size.height), strokeWidth)
                    drawLine(gridColor, Offset(twoThirdX, 0f), Offset(twoThirdX, size.height), strokeWidth)

                    // Horizontal lines
                    drawLine(gridColor, Offset(0f, oneThirdY), Offset(size.width, oneThirdY), strokeWidth)
                    drawLine(gridColor, Offset(0f, twoThirdY), Offset(size.width, twoThirdY), strokeWidth)
                } else if (gridType == GridType.GOLDEN_RATIO) {
                    val phi1 = size.width * 0.382f
                    val phi2 = size.width * 0.618f
                    val phiY1 = size.height * 0.382f
                    val phiY2 = size.height * 0.618f

                    drawLine(gridColor, Offset(phi1, 0f), Offset(phi1, size.height), strokeWidth)
                    drawLine(gridColor, Offset(phi2, 0f), Offset(phi2, size.height), strokeWidth)
                    drawLine(gridColor, Offset(0f, phiY1), Offset(size.width, phiY1), strokeWidth)
                    drawLine(gridColor, Offset(0f, phiY2), Offset(size.width, phiY2), strokeWidth)
                }
            }
        }

        // Artificial Level Horizon line in viewfinder
        if (showLevelIndicator) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerY = size.height / 2f
                val centerX = size.width / 2f
                val lineLength = 48.dp.toPx()
                val gap = 16.dp.toPx()
                val levelColor = Color(0x99FFB300)

                // Left line
                drawLine(
                    levelColor,
                    Offset(centerX - gap - lineLength, centerY),
                    Offset(centerX - gap, centerY),
                    2.dp.toPx()
                )
                // Right line
                drawLine(
                    levelColor,
                    Offset(centerX + gap, centerY),
                    Offset(centerX + gap + lineLength, centerY),
                    2.dp.toPx()
                )
                // Center point
                drawCircle(levelColor, 2.5.dp.toPx(), Offset(centerX, centerY))
            }
        }

        // Animated Tap-to-Focus Ring
        focusPoint?.let { pt ->
            AnimatedVisibility(
                visible = showFocusRing,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(300))
            ) {
                val ringSizeDp = 64.dp
                val density = LocalDensity.current
                val ringHalfPx = with(density) { (ringSizeDp / 2).toPx() }

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (pt.x - ringHalfPx).toInt(),
                                (pt.y - ringHalfPx).toInt()
                            )
                        }
                        .size(ringSizeDp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val goldColor = Color(0xFFFFB300)
                        val stroke = Stroke(width = 2.dp.toPx())
                        drawCircle(goldColor, size.minDimension / 2.2f, style = stroke)

                        // 4 viewfinder tick marks
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.minDimension / 2.2f
                        val tickLen = 6.dp.toPx()

                        // Top tick
                        drawLine(goldColor, Offset(center.x, center.y - radius - tickLen), Offset(center.x, center.y - radius), 2.dp.toPx())
                        // Bottom tick
                        drawLine(goldColor, Offset(center.x, center.y + radius), Offset(center.x, center.y + radius + tickLen), 2.dp.toPx())
                        // Left tick
                        drawLine(goldColor, Offset(center.x - radius - tickLen, center.y), Offset(center.x - radius, center.y), 2.dp.toPx())
                        // Right tick
                        drawLine(goldColor, Offset(center.x + radius, center.y), Offset(center.x + radius + tickLen, center.y), 2.dp.toPx())
                    }
                }
            }
        }
    }
}
