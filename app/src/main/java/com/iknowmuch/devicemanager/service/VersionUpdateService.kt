package com.iknowmuch.devicemanager.service

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.iknowmuch.devicemanager.Config
import com.iknowmuch.devicemanager.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.pqpo.librarylog4a.Log4a
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import javax.inject.Inject

private const val TAG = "VersionUpdateService"

@AndroidEntryPoint
class VersionUpdateService : LifecycleService() {
    @Inject
    lateinit var okHttpClient: OkHttpClient

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private val downloadPath by lazy {
        File(externalCacheDir, "apk").also {
            if (!it.exists()) {
                it.mkdir()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            with(intent.extras ?: return super.onStartCommand(intent, flags, startId)) {
                lifecycleScope.launch {
                    try {
                        downloadApk(
                            this@with.getString(Config.NewVersionURL, ""),
                            this@with.getString(Config.NewVersionURL, "")
                        )
                    } catch (e: Exception) {
                        Log4a.e(TAG, "downloadApk: ", e)
                        stopSelf()
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    private suspend fun downloadApk(url: String, newVersion: String) = withContext(Dispatchers.IO) {
        if (url.isEmpty() || newVersion.isEmpty()) {
            stopSelf()
            return@withContext
        }
        Log.d(TAG, "downloadApk: url:$url")
        val request = Request.Builder().url(url).build()
        val response = okHttpClient.newCall(request).execute()
        var inputStream: InputStream? = null
        var fos: FileOutputStream? = null
        try {
            val buffer = ByteArray(1024)
            val file = File(downloadPath, getNameFromUrl(url))
            if (!file.exists()) {
                val temp = File(downloadPath, getNameFromUrl(url) + ".temp")
                inputStream = response.body?.byteStream()
                fos = FileOutputStream(temp)
                var len: Int
                var sum = 0
                while (inputStream?.read(buffer).also { len = it ?: -1 } != -1) {
                    fos.write(buffer, 0, len)
                    sum += len
                }
                fos.flush()
                temp.renameTo(file)
                temp.delete()
                saveConfig(newVersion)
                install(file)
            }
        } catch (e: Exception) {
            Log4a.e(TAG, "download apk: ", e)
        } finally {
            try {
                inputStream?.close()
                fos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //    private fun install(file: File) {
//        val uri = FileProvider.getUriForFile(
//            this,
//            this.packageName + ".fileProvider",
//            file
//        )
//        applicationContext.grantUriPermission(
//            applicationContext.packageName, uri,
//            Intent.FLAG_GRANT_READ_URI_PERMISSION
//        )
//        val intent = Intent(Intent.ACTION_VIEW).apply {
//            flags =
//                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
//            setDataAndType(
//                uri,
//                "application/vnd.android.package-archive"
//            )
//        }
//        startActivity(intent)
//    }

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private fun saveConfig(version: String) {
        preferenceManager.updateRecord = setOf(sdf.format(System.currentTimeMillis()), version)
    }

    private fun install(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            this.packageName + ".fileProvider",
            file
        )
        applicationContext.grantUriPermission(
            applicationContext.packageName, uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent(Intent.ACTION_VIEW).apply {
                flags =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                setDataAndType(
                    uri,
                    "application/vnd.android.package-archive"
                )
            }
        } else {
            Intent(Intent.ACTION_VIEW).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK
                setDataAndType(
                    Uri.fromFile(file),
                    "application/vnd.android.package-archive"
                )
            }
        }
        Log.d(TAG, "install: ")
        startActivity(intent)
    }
//    private fun install(file: File) {
//
//        val apkPath = file.path
//        val printWriter: PrintWriter
//        var process: Process? = null
//        Log.d(TAG, "install: ")
//        try {
//            process = Runtime.getRuntime().exec("su")
//            printWriter = PrintWriter(process.outputStream)
//            printWriter.println("chmod 777 $apkPath")
//            printWriter
//                .println("export LD_LIBRARY_PATH=/vendor/lib:/system/lib")
//            printWriter.println("pm install -r $apkPath")
//            // PrintWriter.println("exit");
//            printWriter.flush()
//            printWriter.close()
//            val value = process.waitFor()
//            Log4a.i(TAG, "静默安装返回值：$value")
//        } catch (e: java.lang.Exception) {
//            e.printStackTrace()
//            Log4a.i(TAG, "安装apk出现异常")
//        } finally {
//            process?.destroy()
//        }
//    }

    private fun getNameFromUrl(url: String): String {
        return url.substring(url.lastIndexOf("/") + 1)
    }
}