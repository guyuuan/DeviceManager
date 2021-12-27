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
import com.iknowmuch.devicemanager.service.VersionUpdateService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.pqpo.librarylog4a.Log4a
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
    }

    private val topic by lazy {
        Config.getTopic(preferenceManager.deviceID)
    }

    override fun onCreate() {
        super.onCreate()
        startMqtt()
        collectMqttStatus()
        collectMqttMessage()
        serialPortDataRepository.init()

    }

//    override fun onBind(intent: Intent): IBinder {
//        super.onBind(intent)
//        return Binder(this)
//    }

    override fun onDestroy() {
        mqttManager.release()
        serialPortDataRepository.close()
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
                    Log4a.e(TAG, "startMqtt: ", e)
                }
                delay(Config.Minute)
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

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun handlerMqttMessage(mqMessage: MQMessage) {
        val currentTime = System.currentTimeMillis() - 5 * 1000L
        if (mqMessage.topic != topic || mqMessage.timestamp < currentTime) {
            Log4a.d(TAG, "handlerMqttMessage: 消息时间${mqMessage.timestamp} 历史消息 now $currentTime")
            return
        }
        try {
            val json = jsonAdapter.fromJson(mqMessage.message) ?: return
            Log4a.d(TAG, "handlerMqttMessage: $json")
            preferenceManager.deptID = json.data.deptId.toString()
            json.userToken?.let {
                preferenceManager.token = it
            }
            lifecycleScope.launch(Dispatchers.IO) {
                if (json.data.newAppVersion == null) {
                    //借还操作
//                    when (json.data.state) {
//                        //借
//                        0 -> {
                    Log4a.d(
                        TAG,
                        "handlerMqttMessage: ${if (json.data.state == 0) "借" else "还"} ${json.data.doorNo}门"
                    )
                    serialPortDataRepository.controlDoor(
                        json.data.doorNo!!,
                        json.data.state!!,
                        onOpen = { door, probe ->
                            Log4a.d(TAG, "handlerMqttMessage: 开门 $door 在线 $probe")
                            doorApiRepository.reportDoorState(
                                state = if (door) DoorApi.StateOpen else DoorApi.StateClose,
                                deptId = json.data.deptId.toString(),
                                doorNo = json.data.doorNo,
                                probeState = probe
                            )
                        }) { door, probe ->
                        Log4a.d(TAG, "handlerMqttMessage: 关门 $door 在线$probe")
                        doorApiRepository.reportDoorState(
                            if (door) DoorApi.StateClose else DoorApi.StateOpen,
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
                } else {
                    //版本更新
                    Log4a.d(TAG, "版本更新")
                    startService(Intent(this@MqttService, VersionUpdateService::class.java).apply {
                        putExtra(Config.NewVersionURL, json.data.url)
                        putExtra(Config.NewVersion, json.data.newAppVersion)
                    })
                }
            }
        } catch (e: Exception) {
            Log4a.e(TAG, "handlerMqttMessage: ", e)
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