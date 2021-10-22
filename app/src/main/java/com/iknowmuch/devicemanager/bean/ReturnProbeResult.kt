package com.iknowmuch.devicemanager.bean

import com.squareup.moshi.Json

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

    val status2 = data?.data?.status
    val realMessage = data?.data?.msg ?: data?.msg ?: msg
    val status1 = data?.status
}