package com.iknowmuch.devicemanager

/**
 *@author: Chen
 *@createTime: 2021/9/13 10:36
 *@description:
 **/
object Config {
    const val HTTP = "http://"
    const val HTTPS = "https://"
    const val TCP = "tcp://"
    const val SSL = "ssl://"
    const val Hour = 1000L * 60 * 60
    const val Minute = 1000L * 60

    //    const val DEFAULT_HTTP_SERVER = "http://10.6.3.96:8081"
    const val DEFAULT_HTTP_SERVER = "http://asset.iknowmuch.com"
    const val DEFAULT_AUTO_JUMP_TIME = 30
    const val DEFAULT_CHARGING_TIME = 2f

    //    val DEFAULT_MQTT_SERVER = if (BuildConfig.DEBUG) "10.6.3.126:1883" else "symq.iknowmuch.com"
//    const val DEFAULT_MQTT_SERVER = "39.100.118.46:1883"
    const val DEFAULT_MQTT_SERVER = "skechersmq.iknowmuch.com"

    private const val MQTT_TOPIC = "android.asset.topic."
    fun getTopic(id: String) = "$MQTT_TOPIC$id"

    const val ErrorReportDelay = 60* 10 * 1000L
    const val NewVersionURL = "new_version_url"
    const val NewVersion = "new_version"
}