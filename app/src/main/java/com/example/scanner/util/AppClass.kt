package com.example.scanner.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleObserver
import org.json.JSONArray
import org.json.JSONException
import kotlin.ranges.until
import kotlin.text.isNotEmpty

open class AppClass : Application(), Application.ActivityLifecycleCallbacks, LifecycleObserver {

//    private lateinit var mFirebaseRemoteConfig: FirebaseRemoteConfig

//    companion object {
//
//        var mActivity: Context? = null
//        var mInstance: AppClass? = null
//
//        var result: Int = 0
//        var h_bottomnavigation: Int = 0
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

//        mInstance = this
//        mActivity = this

        val lang = LocaleManager.getPersistedLanguage(this)
        if (lang.isNotEmpty()) LocaleManager.applyLocale(this, lang)
//        val identifier = resources.getIdentifier("status_bar_height", "dimen", "android")
//        val identifier2 = resources.getIdentifier("navigation_bar_height", "dimen", "android")
//        if (identifier > 0) {
//            result = resources.getDimensionPixelSize(identifier)
//            h_bottomnavigation = resources.getDimensionPixelSize(identifier2)
//        }


//        registerActivityLifecycleCallbacks(this)
//        FirebaseApp.initializeApp(this)
//        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
//        PrefUtil.getInstance(this)
//        getConfig()
    }

//    override fun attachBaseContext(base: Context) {
//        val lang = LocaleManager.getPersistedLanguage(base)
//        val ctx = if (lang.isNotEmpty()) LocaleManager.applyLocale(base, lang) else base
//        super.attachBaseContext(ctx)
//    }
//
//    private fun getConfig() {
//        FirebaseApp.initializeApp(this)
//        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
//        val configSettings = FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(1).build()
//        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
//        remoteMethod()
//    }
//
//    private fun remoteMethod() {
//        mFirebaseRemoteConfig.fetchAndActivate()
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val fullData = mFirebaseRemoteConfig.getString("AddData")
//                    totalData(fullData)
//
//
//                }
//            }
//    }
//
//
//
//    private fun totalData(allData: String?) {
//        try {
//            val jArray = JSONArray(allData)
//            for (i in 0 until jArray.length()) {
//                val jObject = jArray.getJSONObject(i)
//
//                Utill.TERMSCONDITION = jObject.getString("term_condition")
//                Utill.PRIVACYPOLICY = jObject.getString("Privacy_Policy_link")
//                PrefUtil.putString(Utill.SOCIALDOWNLOADERMAIN, jObject.getString("SocialDownloaderMainActivityOnOff"))
//                PrefUtil.putString(Utill.MEDIAICON, jObject.getString("SecureBrowserMediaIconOnOff"))
//
//                PrefUtil.putString(Utill.CALLSERVICESONOFF, jObject.getString("CallEndServicesOnoff"))
//                PrefUtil.putString(Utill.ENDSCREENCOUNTRY, jObject.getString("EndscreenNotShowCountry"))
//
//                PrefUtil.putString(Utill.INTROONE, jObject.getString("introOne"))
//            }
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
//    }
//
//
//
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        try {
//            PrefUtil.getString(Utill.LANKEY, "en")
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

}
