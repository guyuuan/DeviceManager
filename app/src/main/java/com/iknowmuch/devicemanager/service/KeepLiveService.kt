package com.iknowmuch.devicemanager.service

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.util.Log
import com.iknowmuch.devicemanager.MainActivity
import com.iknowmuch.devicemanager.preference.KeepLivePreference
import com.tencent.mmkv.MMKV

/**
 *@author: Chen
 *@createTime: 2021/10/21 9:09
 *@description:
 **/
private const val TAG = "KeepLiveService"

class KeepLiveService : JobService() {

    private val keepLiveFlag by KeepLivePreference(MMKV.defaultMMKV())

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: ")
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.e(TAG, "onStartJob")
        if (!keepLiveFlag) return true
        if (!isRunning()) {
            val intent2 = Intent(this, MainActivity::class.java)
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent2)
        }
        try {
            if (null != params) {
                jobFinished(params, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun isRunning(): Boolean {
        var isRunning = false
        val am = this.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val lists = am.runningAppProcesses
        // 获取运行服务再启动
        for (info in lists) {
            Log.d(
                TAG,
                "isRunning: " + info.processName + " process importance: " + info.importance
            )
            if (info.processName == null) continue
            if (info.processName.startsWith(this.packageName)) {
                isRunning = info.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                break
            }
        }
        return isRunning
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }
}