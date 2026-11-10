package com.netkeep5g.app.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netkeep5g.app.model.SpeedPreset
import com.netkeep5g.app.ui.theme.CyanAccent
import com.netkeep5g.app.ui.theme.SurfaceBorderDark
import com.netkeep5g.app.ui.theme.SurfaceDark
import com.netkeep5g.app.ui.theme.SurfaceVariantDark
import com.netkeep5g.app.ui.theme.TextPrimary
import com.netkeep5g.app.ui.theme.TextSecondary
import com.netkeep5g.app.ui.theme.TextTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeedSelector(
    selectedSpeedKBps: Int,
    onSpeedSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentPreset = SpeedPreset.fromSpeed(selectedSpeedKBps)

    Column(
        modifier = modifier
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
                text = "TARGET SPEED",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Text(
                text = "$selectedSpeedKBps KB/s (${selectedSpeedKBps * 8} kbps)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preset Chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpeedPreset.entries.filter { it != SpeedPreset.CUSTOM }.forEach { preset ->
                val isSelected = currentPreset == preset && selectedSpeedKBps == preset.speedKBps
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
                        .clickable { onSpeedSelected(preset.speedKBps) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = preset.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) CyanAccent else TextPrimary
                        )
                        Text(
                            text = "${preset.speedKBps} KB/s",
                            fontSize = 10.sp,
                            color = if (isSelected) CyanAccent else TextTertiary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Custom Slider
        Text(
            text = "Fine-tune (1 to 50 KB/s)",
            fontSize = 12.sp,
            color = TextTertiary
        )

        Slider(
            value = selectedSpeedKBps.toFloat(),
            onValueChange = { onSpeedSelected(it.toInt().coerceIn(1, 50)) },
            valueRange = 1f..50f,
            steps = 48,
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
            Text("1 KB/s (8 kbps)", fontSize = 10.sp, color = TextTertiary)
            Text("25 KB/s (200 kbps)", fontSize = 10.sp, color = TextTertiary)
            Text("50 KB/s (400 kbps)", fontSize = 10.sp, color = TextTertiary)
        }
    }
}
