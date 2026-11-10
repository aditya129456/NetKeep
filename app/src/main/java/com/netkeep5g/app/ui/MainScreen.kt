package com.netkeep5g.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.netkeep5g.app.R
import com.netkeep5g.app.ui.components.HighBandwidthWarningDialog
import com.netkeep5g.app.ui.screens.CalculatorScreen
import com.netkeep5g.app.ui.screens.DashboardScreen
import com.netkeep5g.app.ui.screens.SettingsScreen
import com.netkeep5g.app.ui.screens.TrafficControlScreen
import com.netkeep5g.app.ui.theme.BackgroundDark
import com.netkeep5g.app.ui.theme.CyanAccent
import com.netkeep5g.app.ui.theme.SurfaceBorderDark
import com.netkeep5g.app.ui.theme.SurfaceDark
import com.netkeep5g.app.ui.theme.SurfaceVariantDark
import com.netkeep5g.app.ui.theme.TextPrimary
import com.netkeep5g.app.ui.theme.TextTertiary

sealed class NavItem(val title: String, val icon: ImageVector) {
    object Dashboard : NavItem("Dashboard", Icons.Default.Dashboard)
    object Controls : NavItem("Controls", Icons.Default.Tune)
    object Calculator : NavItem("Calculator", Icons.Default.Calculate)
    object Settings : NavItem("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val navItems = listOf(
        NavItem.Dashboard,
        NavItem.Controls,
        NavItem.Calculator,
        NavItem.Settings
    )

    if (uiState.showWarningDialog) {
        HighBandwidthWarningDialog(
            speedKBps = uiState.selectedSpeedKBps,
            onConfirm = { viewModel.confirmWarningAndStart(context) },
            onDismiss = { viewModel.dismissWarning() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "NetKeep Logo",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "NetKeep 5G",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            letterSpacing = 0.5.sp,
                            color = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                tonalElevation = 8.dp,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = SurfaceBorderDark,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanAccent,
                            selectedTextColor = CyanAccent,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary,
                            indicatorColor = SurfaceVariantDark
                        )
                    )
                }
            }
        },
        containerColor = BackgroundDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(uiState = uiState, viewModel = viewModel)
                1 -> TrafficControlScreen(uiState = uiState, viewModel = viewModel)
                2 -> CalculatorScreen(uiState = uiState)
                3 -> SettingsScreen(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}
