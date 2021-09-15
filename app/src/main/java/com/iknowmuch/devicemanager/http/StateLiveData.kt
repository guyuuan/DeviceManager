package com.iknowmuch.devicemanager.http

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class StateLiveData<T> : MutableLiveData<RequestStatus<T>>(RequestStatus())

abstract class IStateObserver<T> : Observer<RequestStatus<T>> {
    override fun onChanged(t: RequestStatus<T>) {
        when (t.status) {
            RequestStatus.State.STATE_SUCCESS -> {
                //请求成功，数据不为null
                t.json?.let {
                    onDataChange(it)
                }
            }

            RequestStatus.State.STATE_EMPTY -> {
                //数据为空
                onDataEmpty()
            }

            RequestStatus.State.STATE_FAILED -> {
                t.msg?.let {
                    onFailed(it)
                }
            }
            RequestStatus.State.STATE_ERROR -> {
                //请求错误
                t.error?.let { onError(it) }
            }
            else -> {
                onFailed("unknown error")
            }
        }

    }

    abstract fun onDataChange(data: T)

    abstract fun onDataEmpty()

    abstract fun onFailed(msg: String)

    abstract fun onError(error: Throwable)
}