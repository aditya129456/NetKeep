package com.netkeep5g.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netkeep5g.app.model.ConsumptionEstimate
import com.netkeep5g.app.ui.MainUiState
import com.netkeep5g.app.ui.theme.BlueAccent
import com.netkeep5g.app.ui.theme.CyanAccent
import com.netkeep5g.app.ui.theme.GreenActive
import com.netkeep5g.app.ui.theme.SurfaceBorderDark
import com.netkeep5g.app.ui.theme.SurfaceDark
import com.netkeep5g.app.ui.theme.SurfaceVariantDark
import com.netkeep5g.app.ui.theme.TextPrimary
import com.netkeep5g.app.ui.theme.TextSecondary
import com.netkeep5g.app.ui.theme.TextTertiary

@Composable
fun CalculatorScreen(
    uiState: MainUiState,
    modifier: Modifier = Modifier
) {
    var calculatorSpeedKBps by remember { mutableIntStateOf(uiState.selectedSpeedKBps) }
    val estimate = ConsumptionEstimate.calculate(calculatorSpeedKBps)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "DATA CONSUMPTION CALCULATOR",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Accurate theoretical projections vs actual measured session data.",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Interactive Speed Slider for Calculation
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
                    text = "CALCULATOR SPEED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$calculatorSpeedKBps KB/s (${calculatorSpeedKBps * 8} kbps)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Slider(
                value = calculatorSpeedKBps.toFloat(),
                onValueChange = { calculatorSpeedKBps = it.toInt().coerceIn(1, 50) },
                valueRange = 1f..50f,
                steps = 48,
                colors = SliderDefaults.colors(
                    thumbColor = CyanAccent,
                    activeTrackColor = CyanAccent,
                    inactiveTrackColor = SurfaceBorderDark
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Projection summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProjectionCard(
                    title = "Per Hour",
                    value = ConsumptionEstimate.formatBytes(estimate.hourlyBytes),
                    modifier = Modifier.weight(1f)
                )
                ProjectionCard(
                    title = "Per 24h",
                    value = ConsumptionEstimate.formatBytes(estimate.dailyBytes),
                    highlightColor = CyanAccent,
                    modifier = Modifier.weight(1f)
                )
                ProjectionCard(
                    title = "Per 30 Days",
                    value = ConsumptionEstimate.formatBytes(estimate.monthlyBytes),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Actual vs Theoretical Comparison Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, RoundedCornerShape(20.dp))
                .border(1.dp, SurfaceBorderDark, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Text(
                text = "SESSION COMPARISON",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Actual Measured Data", fontSize = 12.sp, color = TextTertiary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.trafficState.formattedTotalData,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenActive
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Session Duration", fontSize = 12.sp, color = TextTertiary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.trafficState.formattedDuration,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SurfaceBorderDark, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            val elapsedSec = uiState.trafficState.elapsedDurationSeconds
            val theoreticalSessionBytes = (uiState.selectedSpeedKBps * 1000L) * elapsedSec

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Theoretical Expected Data:", fontSize = 12.sp, color = TextSecondary)
                Text(
                    text = ConsumptionEstimate.formatBytes(theoreticalSessionBytes),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reference Presets Table
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, RoundedCornerShape(20.dp))
                .border(1.dp, SurfaceBorderDark, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Text(
                text = "REFERENCE BANDWIDTH CONSUMPTION TABLE",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            val referenceRows = listOf(
                Triple("1 KB/s", "8 kbps", "86.4 MB"),
                Triple("5 KB/s", "40 kbps", "432 MB"),
                Triple("10 KB/s", "80 kbps", "864 MB"),
                Triple("20 KB/s", "160 kbps", "1.728 GB"),
                Triple("50 KB/s", "400 kbps", "4.32 GB")
            )

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceVariantDark, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text("Speed", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.weight(1f))
                Text("Bitrate", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.weight(1f))
                Text("Daily (24h)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.weight(1f))
            }

            referenceRows.forEach { (speed, bitrate, daily) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(speed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CyanAccent, modifier = Modifier.weight(1f))
                    Text(bitrate, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                    Text(daily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                }
                HorizontalDivider(color = SurfaceBorderDark.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ProjectionCard(
    title: String,
    value: String,
    highlightColor: androidx.compose.ui.graphics.Color = TextPrimary,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(SurfaceVariantDark, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(title.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = TextTertiary, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = highlightColor)
        }
    }
}
