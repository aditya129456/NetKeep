package com.netkeep5g.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netkeep5g.app.ui.theme.GreenActive
import com.netkeep5g.app.ui.theme.GreenActiveGlow
import com.netkeep5g.app.ui.theme.SurfaceBorderDark
import com.netkeep5g.app.ui.theme.SurfaceVariantDark
import com.netkeep5g.app.ui.theme.TextPrimary
import com.netkeep5g.app.ui.theme.TextTertiary

@Composable
fun StatusIndicator(
    isActive: Boolean,
    statusText: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = if (isActive) 0.1f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Row(
        modifier = modifier
            .background(SurfaceVariantDark, RoundedCornerShape(20.dp))
            .border(1.dp, SurfaceBorderDark, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(16.dp)
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .scale(pulseScale)
                        .background(GreenActiveGlow.copy(alpha = pulseAlpha), CircleShape)
                )
            }
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(if (isActive) GreenActive else TextTertiary, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = if (isActive) "ACTIVE" else "IDLE",
            color = if (isActive) GreenActive else TextTertiary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "• $statusText",
            color = TextPrimary,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}
