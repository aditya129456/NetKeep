package com.netkeep5g.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.netkeep5g.app.ui.components.HeroMetricCard
import com.netkeep5g.app.ui.components.SecondaryMetricCard
import com.netkeep5g.app.ui.components.SpeedSelector
import com.netkeep5g.app.ui.components.StatusIndicator
import com.netkeep5g.app.ui.theme.BlueAccent
import com.netkeep5g.app.ui.theme.CyanAccent
import com.netkeep5g.app.ui.theme.GreenActive
import com.netkeep5g.app.ui.theme.RedStop
import com.netkeep5g.app.ui.theme.SurfaceBorderDark
import com.netkeep5g.app.ui.theme.SurfaceDark
import com.netkeep5g.app.ui.theme.SurfaceVariantDark
import com.netkeep5g.app.ui.theme.TextPrimary
import com.netkeep5g.app.ui.theme.TextSecondary
import com.netkeep5g.app.ui.theme.TextTertiary

@Composable
fun DashboardScreen(
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
        // Status Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusIndicator(
                isActive = isRunning,
                statusText = uiState.trafficState.statusMessage,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { viewModel.resetSessionStats() },
                modifier = Modifier
                    .background(SurfaceVariantDark, RoundedCornerShape(12.dp))
                    .border(1.dp, SurfaceBorderDark, RoundedCornerShape(12.dp))
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Stats",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large START / STOP Button
        val buttonGradient = if (isRunning) {
            Brush.horizontalGradient(listOf(RedStop, Color(0xFFD32F2F)))
        } else {
            Brush.horizontalGradient(listOf(CyanAccent, BlueAccent))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(buttonGradient, RoundedCornerShape(20.dp))
                .clickable { viewModel.onStartStopClicked(context) },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (isRunning) TextPrimary else SurfaceDark,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isRunning) "STOP TRAFFIC" else "START TRAFFIC",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (isRunning) TextPrimary else SurfaceDark
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mode Switcher Tabs
        Text(
            text = "TRAFFIC MODE",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, RoundedCornerShape(16.dp))
                .border(1.dp, SurfaceBorderDark, RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            TrafficMode.entries.forEach { mode ->
                val isSelected = uiState.selectedMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) SurfaceVariantDark else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = !isRunning) { viewModel.setMode(mode) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (mode == TrafficMode.KEEP_ALIVE) "Keep-Alive" else "Continuous",
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) CyanAccent else TextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hero Metric Cards (Speed & Data)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HeroMetricCard(
                title = "Actual Speed",
                primaryValue = uiState.trafficState.formattedActualSpeed,
                secondaryValue = "${uiState.trafficState.actualSpeedKbps.toInt()} kbps",
                icon = Icons.Default.Speed,
                accentColor = CyanAccent,
                modifier = Modifier.weight(1f)
            )

            HeroMetricCard(
                title = "Total Data",
                primaryValue = uiState.trafficState.formattedTotalData,
                secondaryValue = "Target: ${uiState.selectedSpeedKBps} KB/s",
                icon = Icons.Default.DataUsage,
                accentColor = BlueAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Secondary Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SecondaryMetricCard(
                label = "Duration",
                value = uiState.trafficState.formattedDuration,
                icon = Icons.Default.Timer,
                modifier = Modifier.weight(1f)
            )

            SecondaryMetricCard(
                label = "Successful",
                value = "${uiState.trafficState.successfulRequests}",
                icon = Icons.Default.CheckCircle,
                valueColor = GreenActive,
                modifier = Modifier.weight(1f)
            )

            SecondaryMetricCard(
                label = "Failed",
                value = "${uiState.trafficState.failedRequests}",
                icon = Icons.Default.Error,
                valueColor = if (uiState.trafficState.failedRequests > 0) RedStop else TextTertiary,
                modifier = Modifier.weight(1f)
            )
        }

        // Quick Speed Controls (if continuous mode)
        AnimatedVisibility(
            visible = uiState.selectedMode == TrafficMode.CONTINUOUS,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(20.dp))
                SpeedSelector(
                    selectedSpeedKBps = uiState.selectedSpeedKBps,
                    onSpeedSelected = { viewModel.setSpeed(it, context) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
