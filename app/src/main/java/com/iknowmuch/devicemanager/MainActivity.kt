package com.iknowmuch.devicemanager

import android.Manifest
import android.content.Intent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.accompanist.pager.ExperimentalPagerApi
import com.iknowmuch.devicemanager.mqtt.MqttService
import com.iknowmuch.devicemanager.ui.Router
import com.iknowmuch.devicemanager.ui.dialog.AppGlobalInfoDialog
import com.iknowmuch.devicemanager.ui.theme.AppTheme
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val windowInsetsController by lazy {
        WindowInsetsControllerCompat(window, window.decorView)
    }

    private var touchCount by mutableStateOf(0)

    @ExperimentalFoundationApi
    @ExperimentalAnimationApi
    @ExperimentalPagerApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        PermissionX.init(this).permissions(
            Manifest.permission.READ_EXTERNAL_STORAGE
        ).request { allGranted, _, _ ->
            if (allGranted) {
                setContent {
                    AppTheme {
                        // A surface container using the 'background' color from the theme
                        Surface(color = MaterialTheme.colors.background) {
                            Router()
                        }
                        if (touchCount > 7) {
                            AppGlobalInfoDialog(onDismissRequest = {
                                touchCount = 0
                            }) {
                                touchCount = 0
                                finish()
                            }
                        }
                    }
                }
                startService(Intent(this, MqttService::class.java))
            } else {
                finish()
            }
        }
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
                if (++touchCount < 8) {
                    myHandler.postDelayed(clearCountRunnable, 1500)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }


}