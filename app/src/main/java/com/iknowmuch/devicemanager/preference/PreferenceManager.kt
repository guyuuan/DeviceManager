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
    lastMessageTimePreference: LastMessageTimePreference,
    serialPortPathPreference: SerialPortPathPreference,
    tokenPreference: TokenPreference,
    updateRecordPreference: UpdateRecordPreference
) {
    var deviceID by deviceIDPreference
//    var deviceID = "8944c483b3567702"
    var deptID by deptIDPreference

    //    var deptID = "14"
    var httpServer by httpServerPreference
    var mqttServer by mqttServerPreference
    var keepLive by keepLivePreference
    var autoJumpTime by autoJumpTimePreference
    var chargingTime by chargingTimePreference
    var lastMessageTime by lastMessageTimePreference
    var serialPortPath by serialPortPathPreference
    var token by tokenPreference
    var updateRecord by updateRecordPreference
}