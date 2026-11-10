package com.netkeep5g.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.netkeep5g.app.model.ConsumptionEstimate
import com.netkeep5g.app.ui.theme.AmberWarning
import com.netkeep5g.app.ui.theme.CyanAccent
import com.netkeep5g.app.ui.theme.SurfaceBorderDark
import com.netkeep5g.app.ui.theme.SurfaceDark
import com.netkeep5g.app.ui.theme.SurfaceVariantDark
import com.netkeep5g.app.ui.theme.TextPrimary
import com.netkeep5g.app.ui.theme.TextSecondary

@Composable
fun HighBandwidthWarningDialog(
    speedKBps: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val estimate = ConsumptionEstimate.calculate(speedKBps)
    val dailyFormatted = ConsumptionEstimate.formatBytes(estimate.dailyBytes)

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(SurfaceDark, RoundedCornerShape(24.dp))
                .border(1.dp, SurfaceBorderDark, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(AmberWarning.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "High Bandwidth Notice",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "You have selected a high continuous download speed of $speedKBps KB/s (${speedKBps * 8} kbps).",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariantDark, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "ESTIMATED 24-HOUR DATA CONSUMPTION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AmberWarning,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "≈ $dailyFormatted / day",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Ensure you have an unlimited or high-allowance mobile plan (e.g. Airtel 5G Unlimited) to avoid unexpected carrier overage.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Proceed", color = SurfaceDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
