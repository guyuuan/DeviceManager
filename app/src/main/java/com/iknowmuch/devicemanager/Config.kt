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

    const val DEFAULT_HTTP_SERVER = "10.6.3.96:8081"
    const val DEFAULT_AUTO_JUMP_TIME = 30
    val DEFAULT_MQTT_SERVER = if (BuildConfig.DEBUG) "10.6.3.126:1883" else "symq.iknowmuch.com"

    const val MQTT_TOPIC = "android.cloud.shelf."
    fun getTopic(id: String) = "$MQTT_TOPIC$id"
}