package com.iknowmuch.devicemanager.base

import com.iknowmuch.devicemanager.bean.BaseJson
import com.iknowmuch.devicemanager.http.RequestStatus
import com.iknowmuch.devicemanager.http.StateLiveData
import kotlinx.coroutines.flow.MutableStateFlow

/**
 *@author: Chen
 *@createTime: 2021/8/31 15:05
 *@description:
 **/
open class BaseRemoteRepository {
    protected suspend fun <T : BaseJson> httpRequest(
        stateLiveData: StateLiveData<T>,
        block: suspend () -> T?
    ) {
        try {
            stateLiveData.postValue(RequestStatus(status = RequestStatus.State.STATE_LOADING))
            val data = block()
            val response = if (data != null) {
                RequestStatus(
                    code = data.status,
                    status = when (data.status) {
                        in 200..299 -> RequestStatus.State.STATE_SUCCESS
                        in 300..599 -> RequestStatus.State.STATE_FAILED
                        else -> RequestStatus.State.STATE_UNKNOWN
                    },
                    msg = data.msg,
                    json = data
                )
            } else {
                RequestStatus(status = RequestStatus.State.STATE_EMPTY)
            }
            stateLiveData.postValue(response)
        } catch (e: Exception) {
            e.printStackTrace()
            stateLiveData.postValue(
                RequestStatus(
                    status = RequestStatus.State.STATE_ERROR,
                    error = e
                )
            )
        }
    }

    protected suspend fun <T : BaseJson> httpRequest(
        stateLiveData: MutableStateFlow<RequestStatus<T>>,
        block: suspend () -> T?
    ) {
        try {
            stateLiveData.emit(RequestStatus(status = RequestStatus.State.STATE_LOADING))
            val data = block()
            val response = if (data != null) {
                RequestStatus(
                    code = data.status,
                    status = when (data.status) {
                        in 200..299 -> RequestStatus.State.STATE_SUCCESS
                        in 300..599 -> RequestStatus.State.STATE_FAILED
                        else -> RequestStatus.State.STATE_UNKNOWN
                    },
                    msg = data.msg,
                    json = data
                )
            } else {
                RequestStatus(status = RequestStatus.State.STATE_EMPTY)
            }
            stateLiveData.emit(response)
        } catch (e: Exception) {
            e.printStackTrace()
            stateLiveData.emit(RequestStatus(status = RequestStatus.State.STATE_ERROR, error = e))
        }
    }
}