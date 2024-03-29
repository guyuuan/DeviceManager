package com.iknowmuch.devicemanager.mqtt

import android.content.Context
import android.util.Log
import com.iknowmuch.devicemanager.Config
import com.iknowmuch.devicemanager.bean.MQMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.pqpo.librarylog4a.Log4a
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

private const val TAG = "MqttManager"

class MqttManager {

    init {
        Log.d(TAG, "init: ")
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val clientsMap = HashMap<String, MqttAndroidClient>()

    private val mqttStatusMap = MutableStateFlow<Map<String, MQTTStatus>>(emptyMap())

    val mqttMessageFlow by lazy { MutableSharedFlow<MQMessage>(replay = 3) }

    fun getClient(id: String) = clientsMap[id]

    fun createClient(cxt: Context, clientId: String, url: String): MqttAndroidClient {
        return clientsMap.getOrPut(clientId) {
            MqttAndroidClient(cxt, checkUrl(url), clientId, MemoryPersistence()).apply {
                setCallback(object : MqttCallbackExtended {
                    override fun connectionLost(cause: Throwable?) {
                        Log4a.e(TAG, "connectionLost: ", cause)
                        updateMqttStatus(MQTTStatus.CONNECT_LOST, clientId)
                    }

                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        message?.let {
                            coroutineScope.launch {
                                Log4a.d(TAG, "messageArrived: $it")
                                mqttMessageFlow.emit(MQMessage(topic ?: "", it.toString()))
                            }
                        }
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                        Log.d(TAG, "deliveryComplete: ")
                    }

                    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                        if (reconnect) {
                            Log4a.d(TAG, "connectComplete: 重连成功")
                        } else {
                            Log4a.d(TAG, "connectComplete: 连接成功")
                        }
                        updateMqttStatus(MQTTStatus.CONNECT_SUCCESS, clientId)
                    }

                })
            }
        }
    }

    private fun checkUrl(url: String): String {
        return when {
            url.startsWith(Config.HTTP) -> {
                url.replace(Config.HTTP, Config.TCP)
            }
            url.startsWith(Config.HTTPS) -> {
                url.replace(Config.HTTPS, Config.SSL)
            }
            else -> Config.TCP + url
        }
    }

    fun subscribe(client: MqttAndroidClient, topic: String, qos: Int = 1) {
        if (client.isConnected) {
            try {
                client.subscribe(topic, qos, "Subscribe", object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log4a.d(TAG, "Subscribe onSuccess: $topic")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log4a.e(TAG, "Subscribe onFailure: ", exception)
                    }

                })
            } catch (e: Exception) {
                Log4a.e(TAG, "subscribe: ", e)
            }
        }
    }

/*    fun publish(client: MqttAndroidClient, topic: String, message: String) {
        if (client.isConnected) {
            try {
                client.publish(
                    topic,
                    ("$message ${sdf.format(System.currentTimeMillis())}").toByteArray(Charsets.UTF_8),
                    1,
                    true,
                    null,
                    object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            Log4a.d(TAG, "$topic 发送消息成功: ")
                        }

                        override fun onFailure(
                            asyncActionToken: IMqttToken?,
                            exception: Throwable?
                        ) {
                            Log4a.e(TAG, "$topic 发送消息失败: ", exception)
                        }
                    })
            } catch (e: Exception) {
                Log4a.e(TAG, "publish failed: ", e)
            }
        }
    }*/

    private fun updateMqttStatus(mqttStatus: MQTTStatus, id: String) {
        coroutineScope.launch {
            val map = mqttStatusMap.value.toMutableMap()
            map[id] = mqttStatus
            mqttStatusMap.emit(map)
        }
    }

    fun release() {
        coroutineScope.launch {
            mqttStatusMap.emit(emptyMap())

            for (entry in clientsMap) {
                if (entry.value.isConnected) {
                    try {
                        entry.value.unregisterResources()
                        entry.value.close()
                        entry.value.disconnect()
                    } catch (e: Exception) {
                        Log4a.e(TAG, "release: ", e)
                    } finally {
                        clientsMap.remove(entry.key)
                    }
                }
            }
            clientsMap.clear()
        }
    }

    fun connect(client: MqttAndroidClient) {
        if (client.isConnected) return
        val options = MqttConnectOptions().apply {
            isAutomaticReconnect = true
            isCleanSession = true
        }
        try {
            client.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log4a.d(TAG, "connect onSuccess: ")
                    coroutineScope.launch {
                        mqttStatusMap
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log4a.e(TAG, "connect onFailure: ", exception)
                }
            })
        } catch (e: Exception) {
            Log4a.e(TAG, "connect: ", e)
        }
    }

    fun getMqttStatus(): StateFlow<Map<String, MQTTStatus>> = mqttStatusMap

}