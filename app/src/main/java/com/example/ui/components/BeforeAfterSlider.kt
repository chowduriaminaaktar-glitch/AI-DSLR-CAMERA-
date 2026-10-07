package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun BeforeAfterSlider(
    modifier: Modifier = Modifier,
    originalBitmap: Bitmap? = null,
    enhancedBitmap: Bitmap? = null,
    originalUri: Any? = null,
    enhancedUri: Any? = null,
    initialSplit: Float = 0.5f
) {
    var splitFraction by remember { mutableFloatStateOf(initialSplit) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .testTag("before_after_container")
    ) {
        val totalWidthPx = constraints.maxWidth.toFloat()
        val splitX = (totalWidthPx * splitFraction).coerceIn(0f, totalWidthPx)

        // Base Layer: Enhanced Image (shown on right)
        Box(modifier = Modifier.fillMaxSize()) {
            if (enhancedBitmap != null) {
                Image(
                    bitmap = enhancedBitmap.asImageBitmap(),
                    contentDescription = "Enhanced photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (enhancedUri != null) {
                AsyncImage(
                    model = enhancedUri,
                    contentDescription = "Enhanced photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Top Layer: Original Image clipped to width of splitX (shown on left)
        val density = androidx.compose.ui.platform.LocalDensity.current
        val splitWidthDp = with(density) { splitX.toDp() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .width(splitWidthDp)
                .clipToBounds()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (originalBitmap != null) {
                    Image(
                        bitmap = originalBitmap.asImageBitmap(),
                        contentDescription = "Original photo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (originalUri != null) {
                    AsyncImage(
                        model = originalUri,
                        contentDescription = "Original photo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Vertical divider line
        Box(
            modifier = Modifier
                .offset { IntOffset(splitX.toInt() - 2, 0) }
                .fillMaxSize()
                .width(3.dp)
                .background(Color.White)
        )

        // Draggable Handle Pill/Circle
        val handleSizeDp = 44.dp
        val handleHalfPx = with(density) { (handleSizeDp / 2).toPx() }

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (splitX - handleHalfPx).toInt(),
                        (constraints.maxHeight / 2 - handleHalfPx).toInt()
                    )
                }
                .size(handleSizeDp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.85f))
                .border(2.dp, Color(0xFFFFB300), CircleShape)
                .pointerInput(totalWidthPx) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newX = (splitX + dragAmount.x).coerceIn(20f, totalWidthPx - 20f)
                        splitFraction = newX / totalWidthPx
                    }
                }
                .testTag("before_after_slider_handle"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CompareArrows,
                contentDescription = "Drag to compare before and after",
                tint = Color(0xFFFFB300),
                modifier = Modifier.size(26.dp)
            )
        }

        // Badges: "ORIGINAL" on top left, "AI ENHANCED" on top right
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.65f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
        ) {
            Text(
                text = "ORIGINAL",
                color = Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.75f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x88FFB300))
        ) {
            Text(
                text = "AI DSLR ENHANCED",
                color = Color(0xFFFFB300),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}
