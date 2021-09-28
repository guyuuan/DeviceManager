package com.iknowmuch.devicemanager.mqtt

import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.blankj.utilcode.util.DeviceUtils
import com.iknowmuch.devicemanager.Config
import com.iknowmuch.devicemanager.bean.MQMessage
import com.iknowmuch.devicemanager.bean.Message
import com.iknowmuch.devicemanager.http.moshi.moshi
import com.iknowmuch.devicemanager.preference.MqttServerPreference
import com.iknowmuch.devicemanager.preference.PreferenceManager
import com.tencent.mmkv.MMKV
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MqttService"

@AndroidEntryPoint
class MqttService : LifecycleService() {
    @Inject
    lateinit var mqttManager: MqttManager

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private val mqttServer by MqttServerPreference(MMKV.defaultMMKV())

    private val clientID by lazy {
        preferenceManager.deviceID
    }

    private val topic by lazy {
        Config.getTopic(DeviceUtils.getAndroidID())
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate: ")
        super.onCreate()
        startMqtt()
        collectMqttStatus()
        collectMqttMessage()
//        startSendMessage()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return Binder(this)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: ")
        mqttManager.release()
        super.onDestroy()
    }

    private fun startMqtt() {
        lifecycleScope.launch(Dispatchers.IO) {
            mqttManager.release()
            while (true) {
                val client = mqttManager.getClient(clientID) ?: mqttManager.createClient(
                    this@MqttService,
                    clientID,
                    mqttServer
                )
                try {
                    if (!client.isConnected) {
                        mqttManager.connect(client)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "startMqtt: ", e)
                }
                delay(60 * 1000L)
            }
        }
    }

    private fun startSendMessage() {
        lifecycleScope.launch(Dispatchers.IO) {
            delay(1000L)
            while (true) {
                val client = mqttManager.getClient(clientID) ?: mqttManager.createClient(
                    this@MqttService,
                    clientID,
                    mqttServer
                )
                if (!client.isConnected) {
                    mqttManager.connect(client)
                }
//                mqttManager.publish(client, topic, "Message : Hello MQTT")
                delay(10 * 60 * 1000L)
            }
        }
    }

    private fun collectMqttStatus() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val status = mqttManager.getMqttStatus()
                status.collectLatest {
                    handlerMqttStatus(it[clientID] ?: MQTTStatus.CONNECTING)
                }
            }
        }
    }

    private fun collectMqttMessage() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mqttManager.mqttMessageFlow.collectLatest {
                    handlerMqttMessage(it)
                }
            }
        }
    }

    private val jsonAdapter by lazy { moshi.adapter(Message::class.java) }
    private fun handlerMqttMessage(mqMessage: MQMessage) {
        if (mqMessage.topic != topic || mqMessage.timestamp < preferenceManager.lastMessageTime) return
        mqMessage.message
        val json = jsonAdapter.fromJson(mqMessage.message) ?: return
        when (json.code) {
            1 -> {
            }
            else -> {
            }
        }
    }

    private fun handlerMqttStatus(status: MQTTStatus) {
        when (status) {
            MQTTStatus.CONNECT_SUCCESS -> {
                mqttManager.getClient(clientID)?.let { client ->
                    mqttManager.subscribe(client, topic)
                }
            }
            else -> {

            }
        }
    }

    class Binder(mqttService: MqttService) : android.os.Binder()
}