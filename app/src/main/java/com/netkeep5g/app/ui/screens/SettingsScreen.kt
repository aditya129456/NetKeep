package com.netkeep5g.app.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netkeep5g.app.engine.TrafficEngine
import com.netkeep5g.app.model.ConsumptionEstimate
import com.netkeep5g.app.model.TrafficMode
import com.netkeep5g.app.ui.MainUiState
import com.netkeep5g.app.ui.MainViewModel
import com.netkeep5g.app.ui.theme.AmberWarning
import com.netkeep5g.app.ui.theme.CyanAccent
import com.netkeep5g.app.ui.theme.GreenActive
import com.netkeep5g.app.ui.theme.RedStop
import com.netkeep5g.app.ui.theme.SurfaceBorderDark
import com.netkeep5g.app.ui.theme.SurfaceDark
import com.netkeep5g.app.ui.theme.SurfaceVariantDark
import com.netkeep5g.app.ui.theme.TextPrimary
import com.netkeep5g.app.ui.theme.TextSecondary
import com.netkeep5g.app.ui.theme.TextTertiary

@SuppressLint("BatteryLife")
@Composable
fun SettingsScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }
    val isIgnoringBatteryOptimizations = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }

    var editContinuousUrl by remember(uiState.settings.customContinuousUrl) {
        mutableStateOf(uiState.settings.customContinuousUrl)
    }
    var editKeepAliveUrl by remember(uiState.settings.customKeepAliveUrl) {
        mutableStateOf(uiState.settings.customKeepAliveUrl)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "APPLICATION SETTINGS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Preferences, custom endpoints, and battery execution configuration.",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Battery Optimization Guidance
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, RoundedCornerShape(20.dp))
                .border(1.dp, SurfaceBorderDark, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isIgnoringBatteryOptimizations) GreenActive.copy(alpha = 0.15f) else AmberWarning.copy(alpha = 0.15f),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = null,
                        tint = if (isIgnoringBatteryOptimizations) GreenActive else AmberWarning,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Battery Optimization",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isIgnoringBatteryOptimizations) "Unrestricted (Optimized for Background)" else "Restricted by Android Doze",
                        fontSize = 12.sp,
                        color = if (isIgnoringBatteryOptimizations) GreenActive else AmberWarning
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Android aggressively throttles background network and kills services when the phone is locked or screen is off. To maintain continuous traffic, exempt NetKeep 5G from battery optimization.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(fallbackIntent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Open Android Settings > Battery > Optimize Battery to allow unrestricted execution", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = if (isIgnoringBatteryOptimizations) SurfaceVariantDark else CyanAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isIgnoringBatteryOptimizations) "Recheck Battery Settings" else "Request Battery Exemption",
                    color = if (isIgnoringBatteryOptimizations) TextPrimary else SurfaceDark,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Default Mode & Startup Speed
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, RoundedCornerShape(20.dp))
                .border(1.dp, SurfaceBorderDark, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Text(
                text = "DEFAULT STARTUP CONFIGURATION",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Default Startup Mode", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TrafficMode.entries.forEach { mode ->
                    val isSelected = uiState.settings.defaultMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isSelected) CyanAccent.copy(alpha = 0.15f) else SurfaceVariantDark, RoundedCornerShape(12.dp))
                            .border(1.dp, if (isSelected) CyanAccent else SurfaceBorderDark, RoundedCornerShape(12.dp))
                            .clickable { viewModel.updateDefaultMode(mode) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (mode == TrafficMode.KEEP_ALIVE) "Keep-Alive" else "Continuous",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) CyanAccent else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Default Continuous Speed: ${uiState.settings.defaultSpeedKBps} KB/s", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(1, 5, 10, 20, 50).forEach { spd ->
                    val isSelected = uiState.settings.defaultSpeedKBps == spd
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isSelected) CyanAccent.copy(alpha = 0.15f) else SurfaceVariantDark, RoundedCornerShape(10.dp))
                            .border(1.dp, if (isSelected) CyanAccent else SurfaceBorderDark, RoundedCornerShape(10.dp))
                            .clickable { viewModel.updateDefaultSpeed(spd) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${spd}K",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) CyanAccent else TextTertiary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Custom Endpoints
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
                    text = "NETWORK ENDPOINTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Reset Defaults",
                    fontSize = 11.sp,
                    color = CyanAccent,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        editContinuousUrl = TrafficEngine.DEFAULT_CONTINUOUS_URL
                        editKeepAliveUrl = TrafficEngine.DEFAULT_KEEP_ALIVE_URL
                        viewModel.updateContinuousUrl(TrafficEngine.DEFAULT_CONTINUOUS_URL)
                        viewModel.updateKeepAliveUrl(TrafficEngine.DEFAULT_KEEP_ALIVE_URL)
                        Toast.makeText(context, "Endpoints reset to defaults", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Continuous Download URL", fontSize = 12.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = editContinuousUrl,
                onValueChange = {
                    editContinuousUrl = it
                    viewModel.updateContinuousUrl(it)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = SurfaceBorderDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextSecondary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Keep-Alive Ping URL", fontSize = 12.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = editKeepAliveUrl,
                onValueChange = {
                    editKeepAliveUrl = it
                    viewModel.updateKeepAliveUrl(it)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = SurfaceBorderDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextSecondary
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Data Storage & Session Stats
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, RoundedCornerShape(20.dp))
                .border(1.dp, SurfaceBorderDark, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Text(
                text = "DATA USAGE & STATISTICS",
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
                Text("All-Time Cumulative Traffic", fontSize = 13.sp, color = TextSecondary)
                Text(
                    text = ConsumptionEstimate.formatBytes(uiState.settings.cumulativeBytesAllTime),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = {
                    viewModel.resetSessionStats()
                    viewModel.resetCumulativeUsage()
                    Toast.makeText(context, "All session and cumulative statistics cleared", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RedStop)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear All Data Statistics", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // About & Airtel 5G Context
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, RoundedCornerShape(20.dp))
                .border(1.dp, SurfaceBorderDark, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("About NetKeep 5G", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "NetKeep 5G is a native Android background network traffic generation tool designed to investigate whether continuous or periodic cellular activity on Airtel 5G mobile data helps sustain internet routing for connected hotspot tethered devices.\n\n" +
                        "Transparency Note: This tool generates real HTTPS traffic. It does not tamper with system routing tables, bypass carrier APN tethering restrictions, or claim to prevent carrier-enforced throttling.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Version 1.0.0 (Native Kotlin)", fontSize = 11.sp, color = TextTertiary)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
