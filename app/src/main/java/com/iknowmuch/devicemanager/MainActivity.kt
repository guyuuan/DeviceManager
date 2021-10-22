package com.iknowmuch.devicemanager

import android.Manifest
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.pager.ExperimentalPagerApi
import com.iknowmuch.devicemanager.mqtt.MqttService
import com.iknowmuch.devicemanager.preference.KeepLivePreference
import com.iknowmuch.devicemanager.service.KeepLiveService
import com.iknowmuch.devicemanager.ui.LocalInsetsController
import com.iknowmuch.devicemanager.ui.Router
import com.iknowmuch.devicemanager.ui.dialog.AppGlobalInfoDialog
import com.iknowmuch.devicemanager.ui.theme.AppTheme
import com.permissionx.guolindev.PermissionX
import com.tencent.mmkv.MMKV
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val windowInsetsController by lazy {
        WindowInsetsControllerCompat(window, window.decorView)
    }

    private var touchCount by mutableStateOf(0)


    @ExperimentalCoilApi
    @ExperimentalUnsignedTypes
    @ExperimentalComposeUiApi
    @ExperimentalAnimationApi
    @ExperimentalPagerApi
    @ExperimentalMaterialApi
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        PermissionX.init(this).permissions(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA
        ).request { allGranted, _, _ ->
            if (allGranted) {
                setContent {
                    AppTheme {
                        CompositionLocalProvider(LocalInsetsController provides windowInsetsController) {
                            Surface(color = MaterialTheme.colors.background) {
                                Router()
                            }

                            if (touchCount > 7) {
                                AppGlobalInfoDialog(onDismissRequest = {
                                    touchCount = 0
                                }) {
                                    //点击关闭App按钮的回调
                                    touchCount = 0
                                    finish()
                                }
                            }
                        }
                        // A surface container using the 'background' color from the theme

                    }
                }
            } else {
                finish()
            }

        }
        val intent = Intent(this, MqttService::class.java)
        startService(intent)
        initKeepLive()
    }

    private val keepLiveFlag by KeepLivePreference(MMKV.defaultMMKV())
    private fun initKeepLive() {
        val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        if (!keepLiveFlag) {
            jobScheduler.cancel(3)
            return
        }
        val builder = JobInfo.Builder(
            3, ComponentName(
                packageName,
                KeepLiveService::class.java.name
            )
        )
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
        builder.setPersisted(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setMinimumLatency((10 * 1000).toLong())
        } else {
            builder.setPeriodic((10 * 1000).toLong())
        }
        jobScheduler.schedule(builder.build())
    }

    private val myHandler = Handler(Looper.getMainLooper())
    private val clearCountRunnable: Runnable by lazy {
        Runnable { touchCount = 0 }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val size =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350f, resources.displayMetrics)
        val screenWidth = resources.displayMetrics.widthPixels
        if (event?.action == MotionEvent.ACTION_UP) {
            myHandler.removeCallbacks(clearCountRunnable)
            val x: Float = event.x
            val y: Float = event.y
            if (x > screenWidth - size && x < screenWidth && y > 0 && y < size) {
                Log.d(TAG, "onTouchEvent: count = $touchCount")
                if (++touchCount < 7) {
                    myHandler.postDelayed(clearCountRunnable, 1500)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onResume() {
        super.onResume()
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    @ExperimentalUnsignedTypes
    override fun onDestroy() {
        stopService(Intent(this, MqttService::class.java))
        super.onDestroy()
    }
}