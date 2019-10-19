package com.example.rathings

import android.content.Context
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import androidx.core.content.ContextCompat.getSystemService
import android.net.ConnectivityManager



/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.example.rathings", appContext.packageName)
    }

    @Test
    fun isNetworkAvailable() {
        val appContext = InstrumentationRegistry.getTargetContext()
        val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        assert(connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo.isConnected)
    }


    @Test // internet, access network state, write external storage, camera
    fun checkPermissions() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals(0, appContext.checkCallingOrSelfPermission("android.permission.CAMERA"))
        assertEquals(0, appContext.checkCallingOrSelfPermission("android.permission.INTERNET"))
        assertEquals(0, appContext.checkCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE"))
        assertEquals(0, appContext.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE"))
        assertEquals(0, appContext.checkCallingOrSelfPermission("android.permission.GET_ACCOUNTS"))
    }

}
