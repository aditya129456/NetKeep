package com.netkeep5g.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netkeep5g.app.model.TrafficMode
import com.netkeep5g.app.ui.MainUiState
import com.netkeep5g.app.ui.MainViewModel
import com.netkeep5g.app.ui.components.SpeedSelector
import com.netkeep5g.app.ui.theme.BlueAccent
import com.netkeep5g.app.ui.theme.CyanAccent
import com.netkeep5g.app.ui.theme.RedStop
import com.netkeep5g.app.ui.theme.SurfaceBorderDark
import com.netkeep5g.app.ui.theme.SurfaceDark
import com.netkeep5g.app.ui.theme.SurfaceVariantDark
import com.netkeep5g.app.ui.theme.TextPrimary
import com.netkeep5g.app.ui.theme.TextSecondary
import com.netkeep5g.app.ui.theme.TextTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrafficControlScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isRunning = uiState.trafficState.isRunning

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "TRAFFIC CONTROLS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Configure traffic mode, bandwidth rate, and request intervals.",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Mode Selection Cards
        TrafficMode.entries.forEach { mode ->
            val isSelected = uiState.selectedMode == mode
            val isModeRunning = isRunning && uiState.trafficState.mode == mode

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(
                        if (isSelected) SurfaceVariantDark else SurfaceDark,
                        RoundedCornerShape(18.dp)
                    )
                    .border(
                        1.dp,
                        if (isSelected) CyanAccent else SurfaceBorderDark,
                        RoundedCornerShape(18.dp)
                    )
                    .clickable(enabled = !isRunning) { viewModel.setMode(mode) }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isSelected) CyanAccent.copy(alpha = 0.15f) else SurfaceBorderDark,
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (mode == TrafficMode.CONTINUOUS) Icons.Default.Speed else Icons.Default.NetworkPing,
                            contentDescription = null,
                            tint = if (isSelected) CyanAccent else TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = mode.displayName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) CyanAccent else TextPrimary
                            )
                            if (isModeRunning) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mode.description,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Parameters for Selected Mode
        if (uiState.selectedMode == TrafficMode.CONTINUOUS) {
            SpeedSelector(
                selectedSpeedKBps = uiState.selectedSpeedKBps,
                onSpeedSelected = { viewModel.setSpeed(it, context) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Speed distinction notice card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(16.dp))
                    .border(1.dp, SurfaceBorderDark, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "KB/s vs. kbps Distinction",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1 Byte = 8 bits. A speed of 50 KB/s (KiloBytes per second) equals 400 kbps (kilobits per second). NetKeep 5G throttles at the exact byte level.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            // Keep-Alive Mode controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(20.dp))
                    .border(1.dp, SurfaceBorderDark, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REQUEST INTERVAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${uiState.selectedKeepAliveIntervalSec} seconds",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interval Preset Chips
                val intervalPresets = listOf(10, 30, 60, 120, 300)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    intervalPresets.forEach { sec ->
                        val isSelected = uiState.selectedKeepAliveIntervalSec == sec
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) CyanAccent.copy(alpha = 0.15f) else SurfaceVariantDark,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) CyanAccent else SurfaceBorderDark,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.setKeepAliveInterval(sec) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (sec < 60) "${sec}s" else "${sec / 60}m",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) CyanAccent else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Slider(
                    value = uiState.selectedKeepAliveIntervalSec.toFloat(),
                    onValueChange = { viewModel.setKeepAliveInterval(it.toInt()) },
                    valueRange = 5f..300f,
                    steps = 58,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanAccent,
                        activeTrackColor = CyanAccent,
                        inactiveTrackColor = SurfaceBorderDark
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("5s (frequent)", fontSize = 10.sp, color = TextTertiary)
                    Text("30s (recommended)", fontSize = 10.sp, color = TextTertiary)
                    Text("300s (5 min)", fontSize = 10.sp, color = TextTertiary)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Start/Stop action button
        val buttonGradient = if (isRunning) {
            Brush.horizontalGradient(listOf(RedStop, Color(0xFFD32F2F)))
        } else {
            Brush.horizontalGradient(listOf(CyanAccent, BlueAccent))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(buttonGradient, RoundedCornerShape(16.dp))
                .clickable { viewModel.onStartStopClicked(context) },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (isRunning) TextPrimary else SurfaceDark,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "STOP SERVICE" else "START TRAFFIC GENERATOR",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isRunning) TextPrimary else SurfaceDark
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
