package com.iknowmuch.devicemanager.preference

/**
 *@author: Chen
 *@createTime: 2021/9/24 15:36
 *@description:
 **/
class PreferenceManager(
    deviceIDPreference: DeviceIDPreference,
    deptIDPreference: DeptIDPreference,
    httpServerPreference: HttpServerPreference,
    mqttServerPreference: MqttServerPreference,
    keepLivePreference: KeepLivePreference,
    autoJumpTimePreference: AutoJumpTimePreference,
    chargingTimePreference: ChargingTimePreference,
    lastMessageTimePreference: LastMessageTimePreference
) {
//    var deviceID by deviceIDPreference
    var deviceID = "124"
    var deptID by deptIDPreference
    var httpServer by httpServerPreference
    var mqttServer by mqttServerPreference
    var keepLive by keepLivePreference
    var autoJumpTime by autoJumpTimePreference
    var chargingTime by chargingTimePreference
    var lastMessageTime by lastMessageTimePreference
}