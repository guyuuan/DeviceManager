package com.iknowmuch.devicemanager.bean

import android.annotation.SuppressLint
import com.squareup.moshi.JsonClass
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.and

/**
 *@author: Chen
 *@createTime: 2021/10/12 17:17
 *@description:
 **/
@JsonClass(generateAdapter = true)
data class QRCodeJson(
    val scene:String,
    val page:String = "pages/smartCabinetDetails/smartCabinetDetails"
)