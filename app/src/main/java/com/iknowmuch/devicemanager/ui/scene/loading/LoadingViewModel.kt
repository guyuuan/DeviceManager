package com.iknowmuch.devicemanager.ui.scene.loading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.DeviceUtils
import com.iknowmuch.devicemanager.mqtt.MQTTStatus
import com.iknowmuch.devicemanager.mqtt.MqttManager
import com.iknowmuch.devicemanager.preference.AutoJumpTimePreference
import com.iknowmuch.devicemanager.preference.DeviceIDPreference
import com.iknowmuch.devicemanager.preference.HttpServerPreference
import com.iknowmuch.devicemanager.preference.KeepLivePreference
import com.iknowmuch.devicemanager.preference.MqttServerPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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
    deviceIDPreference: DeviceIDPreference,
    httpServerPreference: HttpServerPreference,
    mqttServerPreference: MqttServerPreference,
    keepLivePreference: KeepLivePreference,
    autoJumpTimePreference: AutoJumpTimePreference,
    mqttManager: MqttManager
) : ViewModel() {

    var autoJumpTime by autoJumpTimePreference
        private set
    var httpServer by httpServerPreference
        private set
    var mqttServer by mqttServerPreference
        private set
    var keepLive by keepLivePreference
        private set
    private var _deviceID by deviceIDPreference
    val deviceID = MutableStateFlow(_deviceID)
    val mqttState = mqttManager.getMqttStatus()
        .map { it["android.cloud.shelf.$_deviceID"] ?: MQTTStatus.CONNECTING }.stateIn(
            viewModelScope,
            SharingStarted.Lazily, MQTTStatus.CONNECTING
        )

    init {
        if (_deviceID.isEmpty()) {
            _deviceID = DeviceUtils.getAndroidID()
        }
        viewModelScope.launch(Dispatchers.Default) {
            deviceID.emit(_deviceID)
        }
    }

    fun saveAppConfig(httpServer: String, mqttServer: String, keepLive: Boolean) {
        this.httpServer = httpServer
        this.mqttServer = mqttServer
        this.keepLive = keepLive
    }

    fun saveAutoJumpTime(time: Int) {
        autoJumpTime = time
    }

}