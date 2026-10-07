package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DslrManualPanel(
    modifier: Modifier = Modifier,
    exposureIndex: Int,
    exposureRange: IntRange,
    simulatedAperture: String,
    onExposureChange: (Int) -> Unit,
    onApertureChange: (String) -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xCC12161E),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFB300))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRO DSLR MANUAL CONTROLS",
                    color = Color(0xFFFFB300),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "EXP: ${if (exposureIndex >= 0) "+$exposureIndex" else "$exposureIndex"} EV",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Exposure Compensation Slider
            val minExp = exposureRange.first.toFloat()
            val maxExp = exposureRange.last.toFloat()
            if (minExp < maxExp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "-EV", color = Color.Gray, fontSize = 11.sp)
                    Slider(
                        value = exposureIndex.toFloat(),
                        onValueChange = { onExposureChange(it.toInt()) },
                        valueRange = minExp..maxExp,
                        steps = (maxExp - minExp).toInt() - 1,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("exposure_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFB300),
                            activeTrackColor = Color(0xFFFFB300),
                            inactiveTrackColor = Color.DarkGray
                        )
                    )
                    Text(text = "+EV", color = Color.Gray, fontSize = 11.sp)
                }
            }

            // Aperture / Bokeh Simulation Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Aperture (DoF):", color = Color.LightGray, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("f/1.4", "f/2.8", "f/5.6", "f/11").forEach { fStop ->
                        val isSelected = simulatedAperture == fStop
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFFFB300) else Color(0x33FFFFFF))
                                .clickable { onApertureChange(fStop) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = fStop,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
