package com.iknowmuch.devicemanager.repository

import android.content.Context
import android.util.Log
import com.iknowmuch.devicemanager.bean.QRCodeJson
import com.iknowmuch.devicemanager.http.api.WeiXinApi
import com.iknowmuch.devicemanager.preference.PreferenceManager
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

/**
 *@author: Chen
 *@createTime: 2021/10/13 11:23
 *@description:
 **/
private const val TAG = "WeiXinRepository"

class WeiXinRepository(
    private val api: WeiXinApi,
    context: Context,
    private val preferenceManager: PreferenceManager
) {

    private val qrCodeSavePath by lazy {
        context.getExternalFilesDir("qrcode")
    }

    private suspend fun getAccessToken() = api.getWXAccessToken()

    fun getQRCode() = flow {
        val scene = "${preferenceManager.deptID},${preferenceManager.deviceID}"
        val file = File(qrCodeSavePath, scene)
        if (file.exists()) {
            emit(DownloadResult.Success(file))
            return@flow
        }
        val token = try {
            getAccessToken().accessToken
        } catch (e: Exception) {
            Log.e(TAG, "getQRCode: ", e)
            emit(DownloadResult.Failed(e))
            return@flow
        }
        try {
            val response = api.getWXACode(
                token = token,
                data = QRCodeJson(scene = scene)
            )
            val body = response.body()
            if (response.isSuccessful && body != null) {
                if (body.contentType().toString() != "image/jpeg") {
                    throw RuntimeException("获取微信小程序码失败:${response.body()}")
                }
                val buffer = ByteArray(1024)
                val inputStream = body.byteStream()
                val total = body.contentLength()
                val outputStream = FileOutputStream(file)
                var len: Int
                var sum = 0
                inputStream.use { fis ->
                    outputStream.use { fos ->
                        while (fis.read(buffer).also { len = it } != -1) {
                            fos.write(buffer, 0, len)
                            sum += len
                            emit(DownloadResult.Progress((sum * 100f / total).roundToInt()))
                        }
                        fos.flush()
                    }
                }
                emit(DownloadResult.Success(file))
            } else {
                throw RuntimeException(response.message())
            }
        } catch (e: Exception) {
            Log.e(TAG, "getQRCode: ", e)
            emit(DownloadResult.Failed(e))
        }
    }

    sealed class DownloadResult {
        class Progress(val process: Int) : DownloadResult()
        class Success(val file: File) : DownloadResult()
        class Failed(val error: Exception) : DownloadResult()
    }
}
