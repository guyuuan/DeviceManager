package com.iknowmuch.devicemanager.mqtt

import android.content.Intent
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.iknowmuch.devicemanager.Config
import com.iknowmuch.devicemanager.bean.CabinetDoor
import com.iknowmuch.devicemanager.bean.MQMessage
import com.iknowmuch.devicemanager.bean.Message
import com.iknowmuch.devicemanager.http.api.DoorApi
import com.iknowmuch.devicemanager.http.moshi.moshi
import com.iknowmuch.devicemanager.preference.PreferenceManager
import com.iknowmuch.devicemanager.repository.DoorApiRepository
import com.iknowmuch.devicemanager.repository.MainRepository
import com.iknowmuch.devicemanager.repository.SerialPortDataRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MqttService"

@ExperimentalUnsignedTypes
@AndroidEntryPoint
class MqttService : LifecycleService() {

    @Inject
    lateinit var mqttManager: MqttManager

    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var doorApiRepository: DoorApiRepository

    @Inject
    lateinit var mainRepository: MainRepository

    @ExperimentalUnsignedTypes
    @Inject
    lateinit var serialPortDataRepository: SerialPortDataRepository


    private val clientID by lazy {
        preferenceManager.deviceID
//        "124"
    }

    private val topic by lazy {
        Config.getTopic(preferenceManager.deviceID)
    }

    @ExperimentalCoroutinesApi
    override fun onCreate() {
        Log.d(TAG, "onCreate: ")
        super.onCreate()
        startMqtt()
        collectMqttStatus()
        collectMqttMessage()
        serialPortDataRepository.init()
//        startSendMessage()
//        lifecycleScope.launch(Dispatchers.IO) {
//            delay(1000)
//            serialPortDataRepository.controlDoor(1, onOpen = {
//                Log.d(TAG, "开门: $it")
//            }) {door,probe->
//                Log.d(TAG, "关门: $door 在线:$probe")
//            }
//        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ")
        return super.onStartCommand(intent, flags, startId)
    }

//    override fun onBind(intent: Intent): IBinder {
//        super.onBind(intent)
//        return Binder(this)
//    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: ")
        serialPortDataRepository.close()
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
                    preferenceManager.mqttServer
                )
                try {
                    if (!client.isConnected) {
                        mqttManager.connect(client)
                    } else {
                        mainRepository.heartBeat()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "startMqtt: ", e)
                }
                delay(Config.Minute)
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
                    preferenceManager.mqttServer
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

    @ExperimentalCoroutinesApi
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

    @ExperimentalCoroutinesApi
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun handlerMqttMessage(mqMessage: MQMessage) {
        val currentTime = System.currentTimeMillis() - 5 * 1000L
        if (mqMessage.topic != topic || mqMessage.timestamp < currentTime) {
            Log.d(TAG, "handlerMqttMessage: 消息时间${mqMessage.timestamp} 历史消息 now $currentTime")
            return
        }
        try {
            val json = jsonAdapter.fromJson(mqMessage.message) ?: return
            Log.d(TAG, "handlerMqttMessage: $json")
            preferenceManager.deptID = json.data.deptId.toString()
            preferenceManager.token = json.userToken ?: ""
            lifecycleScope.launch(Dispatchers.IO) {
                if (json.data.newAppVersion == null) {
                    //借还操作
                    when (json.data.state) {
                        //借
                        0 -> {
                            serialPortDataRepository.controlDoor(json.data.doorNo!!, 0,onOpen = {
                                Log.d(TAG, "handlerMqttMessage: 开门 $it")
                                doorApiRepository.openDoor(
                                    if (it) DoorApi.StateSuccess else DoorApi.StateFailed,
                                    json.data.deptId.toString(),
                                    json.data.doorNo
                                )
                            }) { door, probe ->
                                Log.d(TAG, "handlerMqttMessage: 关门 $door 在线$probe")
                                doorApiRepository.closeDoor(
                                    if (door) DoorApi.StateSuccess else DoorApi.StateFailed,
                                    json.data.deptId.toString(),
                                    json.data.doorNo,
                                    probeState = probe
                                )
                                if (door && !probe) {
                                    mainRepository.updateCabinetDoorById(json.data.doorNo) {
                                        it.copy(status = CabinetDoor.Status.Empty)
                                    }
                                }
                            }
                        }
                        //还
                        else -> {
                            serialPortDataRepository.controlDoor(json.data.doorNo!!,state = 1, onOpen = {
                                Log.d(TAG, "handlerMqttMessage: 开门 $it")
                                doorApiRepository.openDoor(
                                    if (it) DoorApi.StateSuccess else DoorApi.StateFailed,
                                    json.data.deptId.toString(),
                                    json.data.doorNo
                                )
                            }) { door, probe ->
                                Log.d(TAG, "handlerMqttMessage: 关门 $door 在线$probe")
                                doorApiRepository.closeDoor(
                                    if (door) DoorApi.StateSuccess else DoorApi.StateFailed,
                                    json.data.deptId.toString(),
                                    json.data.doorNo,
                                    probeState = probe
                                )
                                if (door && probe) {
                                    mainRepository.updateCabinetDoorById(json.data.doorNo) {
                                        it.copy(
                                            status = CabinetDoor.Status.Charging,
                                            devicePower = 0,
                                            availableTime = 0f
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    //版本更新
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "handlerMqttMessage: ", e)
        } finally {
            preferenceManager.lastMessageTime = mqMessage.timestamp
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

//    class Binder(mqttService: MqttService) : android.os.Binder()
}