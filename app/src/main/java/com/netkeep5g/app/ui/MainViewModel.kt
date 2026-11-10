package com.netkeep5g.app.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.netkeep5g.app.data.SettingsRepository
import com.netkeep5g.app.data.TrafficStateRepository
import com.netkeep5g.app.data.UserSettings
import com.netkeep5g.app.model.TrafficMode
import com.netkeep5g.app.model.TrafficState
import com.netkeep5g.app.service.TrafficForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val trafficState: TrafficState = TrafficState(),
    val settings: UserSettings = UserSettings(),
    val selectedMode: TrafficMode = TrafficMode.KEEP_ALIVE,
    val selectedSpeedKBps: Int = 5,
    val selectedKeepAliveIntervalSec: Int = 30,
    val showWarningDialog: Boolean = false
)

private data class UserSelections(
    val modeOverride: TrafficMode? = null,
    val speedOverride: Int? = null,
    val intervalOverride: Int? = null,
    val showWarning: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val _selections = MutableStateFlow(UserSelections())

    val uiState: StateFlow<MainUiState> = combine(
        TrafficStateRepository.trafficState,
        settingsRepository.userSettingsFlow,
        _selections
    ) { trafficState, settings, selections ->
        val activeMode = if (trafficState.isRunning) {
            trafficState.mode
        } else {
            selections.modeOverride ?: settings.defaultMode
        }

        val activeSpeed = if (trafficState.isRunning) {
            trafficState.targetSpeedKBps
        } else {
            selections.speedOverride ?: settings.defaultSpeedKBps
        }

        val activeInterval = if (trafficState.isRunning) {
            trafficState.keepAliveIntervalSeconds
        } else {
            selections.intervalOverride ?: settings.keepAliveIntervalSec
        }

        MainUiState(
            trafficState = trafficState,
            settings = settings,
            selectedMode = activeMode,
            selectedSpeedKBps = activeSpeed,
            selectedKeepAliveIntervalSec = activeInterval,
            showWarningDialog = selections.showWarning
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun setMode(mode: TrafficMode) {
        _selections.update { it.copy(modeOverride = mode) }
    }

    fun setSpeed(speedKBps: Int, context: Context) {
        val clamped = speedKBps.coerceIn(1, 50)
        _selections.update { it.copy(speedOverride = clamped) }
        if (uiState.value.trafficState.isRunning && uiState.value.trafficState.mode == TrafficMode.CONTINUOUS) {
            TrafficForegroundService.updateSpeed(context, clamped)
        }
    }

    fun setKeepAliveInterval(intervalSec: Int) {
        _selections.update { it.copy(intervalOverride = intervalSec.coerceIn(5, 600)) }
    }

    fun onStartStopClicked(context: Context) {
        val current = uiState.value
        if (current.trafficState.isRunning) {
            stopTraffic(context)
        } else {
            // Warn if continuous mode with speed >= 20 KB/s
            if (current.selectedMode == TrafficMode.CONTINUOUS && current.selectedSpeedKBps >= 20) {
                _selections.update { it.copy(showWarning = true) }
            } else {
                startTraffic(context)
            }
        }
    }

    fun confirmWarningAndStart(context: Context) {
        _selections.update { it.copy(showWarning = false) }
        startTraffic(context)
    }

    fun dismissWarning() {
        _selections.update { it.copy(showWarning = false) }
    }

    private fun startTraffic(context: Context) {
        val state = uiState.value
        val endpoint = if (state.selectedMode == TrafficMode.CONTINUOUS) {
            state.settings.customContinuousUrl
        } else {
            state.settings.customKeepAliveUrl
        }

        TrafficForegroundService.startService(
            context = context,
            mode = state.selectedMode,
            speedKBps = state.selectedSpeedKBps,
            intervalSec = state.selectedKeepAliveIntervalSec,
            endpointUrl = endpoint
        )
    }

    fun stopTraffic(context: Context) {
        TrafficForegroundService.stopService(context)
    }

    fun resetSessionStats() {
        TrafficStateRepository.resetSession()
    }

    fun updateDefaultMode(mode: TrafficMode) {
        viewModelScope.launch {
            settingsRepository.updateDefaultMode(mode)
        }
    }

    fun updateDefaultSpeed(speedKBps: Int) {
        viewModelScope.launch {
            settingsRepository.updateDefaultSpeed(speedKBps)
        }
    }

    fun updateKeepAliveIntervalSetting(intervalSec: Int) {
        viewModelScope.launch {
            settingsRepository.updateKeepAliveInterval(intervalSec)
        }
    }

    fun updateDataUsageWarningMB(warningMB: Int) {
        viewModelScope.launch {
            settingsRepository.updateDataUsageWarningMB(warningMB)
        }
    }

    fun updateContinuousUrl(url: String) {
        viewModelScope.launch {
            settingsRepository.updateContinuousUrl(url)
        }
    }

    fun updateKeepAliveUrl(url: String) {
        viewModelScope.launch {
            settingsRepository.updateKeepAliveUrl(url)
        }
    }

    fun resetCumulativeUsage() {
        viewModelScope.launch {
            settingsRepository.resetCumulativeBytes()
        }
    }
}
