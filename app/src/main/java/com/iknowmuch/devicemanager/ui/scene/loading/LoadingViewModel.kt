package com.iknowmuch.devicemanager.ui.scene.loading

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.DeviceUtils
import com.iknowmuch.devicemanager.mqtt.MQTTStatus
import com.iknowmuch.devicemanager.mqtt.MqttManager
import com.iknowmuch.devicemanager.preference.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 *@author: Chen
 *@createTime: 2021/9/13 16:16
 *@description:
 **/
private const val TAG = "LoadingViewModel"

@HiltViewModel
class LoadingViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    mqttManager: MqttManager
) : ViewModel() {

    val autoJumpTime = preferenceManager.autoJumpTime
    val httpServer = preferenceManager.httpServer

    val mqttServer = preferenceManager.mqttServer
    val keepLive = preferenceManager.keepLive
    val chargingTime = preferenceManager.chargingTime
    private var _deviceID = MutableStateFlow(preferenceManager.deviceID)
    val deviceID: StateFlow<String>
        get() = _deviceID
    val mqttState = mqttManager.getMqttStatus()
        .map { it[preferenceManager.deviceID] ?: MQTTStatus.CONNECTING }.stateIn(
            viewModelScope,
            SharingStarted.Lazily, MQTTStatus.CONNECTING
        )

    init {
        if (preferenceManager.deviceID.isEmpty()) {
            preferenceManager.deviceID = DeviceUtils.getAndroidID()
        }
        viewModelScope.launch(Dispatchers.Default) {
            _deviceID.emit(preferenceManager.deviceID)
        }
    }

    fun saveAppConfig(httpServer: String, mqttServer: String, keepLive: Boolean) {
        preferenceManager.httpServer = httpServer
        preferenceManager.mqttServer = mqttServer
        preferenceManager.keepLive = keepLive
    }

    fun saveAutoJumpTime(time: Int) {
        preferenceManager.autoJumpTime = time
    }

    fun saveChargingTime(time: Float) {
        preferenceManager.chargingTime = time
    }
}