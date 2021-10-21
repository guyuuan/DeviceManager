package com.iknowmuch.devicemanager.bean

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class ReturnProbeResult(
    @Json(name = "data")
    val `data`: Data?,
    @Json(name = "status")
    override val status: Int,
    @Json(name = "msg")
    override val msg: String,
) : BaseJson() {
    data class Data(
        @Json(name = "data")
        val `data`: Data?,
        @Json(name = "msg")
        val msg: String,
        @Json(name = "status")
        val status: Int
    ) {
        data class Data(
            @Json(name = "data")
            val `data`: Any?,
            @Json(name = "msg")
            val msg: String,
            @Json(name = "status")
            val status: Int
        )
    }

    val realStatus = data?.data?.status?:status
    val realMessage = data?.data?.msg?:msg
}