package com.iknowmuch.devicemanager.ui.scene.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.AppUtils
import com.iknowmuch.devicemanager.Config
import com.iknowmuch.devicemanager.bean.Device
import com.iknowmuch.devicemanager.preference.PreferenceManager
import com.iknowmuch.devicemanager.repository.DeviceRepository
import com.iknowmuch.devicemanager.repository.MainRepository
import com.iknowmuch.devicemanager.repository.SerialPortDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.pqpo.librarylog4a.Log4a
import javax.inject.Inject
import kotlin.math.roundToLong

/**
 *@author: Chen
 *@createTime: 2021/9/15 10:47
 *@description:
 **/
private const val TAG = "HomeViewModel"

@ExperimentalUnsignedTypes
@HiltViewModel
class HomeViewModel @Inject constructor(
    preferenceManager: PreferenceManager,
    private val deviceRepository: DeviceRepository,
    private val serialPortDataRepository: SerialPortDataRepository,
    private val repository: MainRepository
) : ViewModel() {

    val controlResult = serialPortDataRepository.controlResult
    private var _returnResult = mutableStateOf(-1 to "")

    val returnResult: State<Pair<Int, String>> get() = _returnResult
    val deviceID = preferenceManager.deviceID
    val cabinetDoorList = repository.getCabinetDoorFlow().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), emptyList()
    )

    val device = deviceRepository.getDeviceInfo().stateIn(
        viewModelScope, started = SharingStarted.Lazily,
        Device()
    )

    init {
        //不为空说明是更新过后重新启动的
        if (preferenceManager.updateRecord.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                val record = preferenceManager.updateRecord.toList()
                val currentVersion = AppUtils.getAppVersionName()
                val recordVersion = record[1]
                val updateTime = record[0]
                val state = if (currentVersion == recordVersion) 1 else 0
                repository.updateRecordReport(
                    state = state, version = currentVersion, updateTime = updateTime
                )
            }
        }

        //0可借用，1使用中，2被预定，3充电中，4有故障，5遗失，6异常，7归还异常
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    repository.updateLocaleData(deviceRepository)
                } catch (e: Exception) {
                    Log4a.e(TAG, "updateHomeData: ", e)
                }
                delay(10 * 1000L)
            }
        }

        repository.startDataAutoUpdate(
            preferenceManager,
            viewModelScope,
            (preferenceManager.chargingTime * Config.Hour).roundToLong()
        )
/*        val cabinetDoorList = listOf(
            CabinetDoor(
                id = 1, status = CabinetDoor.Status.Empty,
            ),
            CabinetDoor(
                id = 2, status = CabinetDoor.Status.Empty,
            ),
            CabinetDoor(
                id = 3, status = CabinetDoor.Status.Empty,
                devicePower = 0
            ),
            CabinetDoor(
                id = 4, status = CabinetDoor.Status.Empty,
            ),
            CabinetDoor(
                id = 5, status = CabinetDoor.Status.Empty,
            ),
            CabinetDoor(
                id = 6, status = CabinetDoor.Status.Empty,
            )
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCabinetDoor(cabinetDoorList)
            deviceRepository.insertDeviceInfo()
        }*/
    }

    fun returnProbe(probeCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.returnProbe(probeCode)
                if (response.status2 == 200 && response.status == 200 && response.status1 == 200) {
                    //归还成功

                } else {
                    //归还失败
                    Log4a.d(TAG, "returnProbe: ${response.realMessage}")
                }
                _returnResult.value =
                    (response.status2 ?: response.status1
                    ?: response.status) to response.realMessage
            } catch (e: Exception) {
                Log4a.e(TAG, "returnProbe: ", e)
            }
        }
    }

    fun clearReturnDiaLog4a() {
        _returnResult.value = -1 to ""
    }

    fun clearControlDoorResult() {
        viewModelScope.launch {
            serialPortDataRepository.clearControlDoorResult()
        }
    }

    fun updateLocalData(){
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    repository.updateLocaleData(deviceRepository)
                } catch (e: Exception) {
                    Log4a.e(TAG, "updateHomeData: ", e)
                }
                delay(10 * 1000L)
            }
        }
    }
}