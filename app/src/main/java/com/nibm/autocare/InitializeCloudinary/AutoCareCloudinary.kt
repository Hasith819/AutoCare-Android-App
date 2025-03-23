package com.nibm.autocare

import android.app.Application
import com.cloudinary.android.MediaManager
import java.util.HashMap

class AutoCareCloudinary : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeCloudinary()
    }

    private fun initializeCloudinary() {
        val config = HashMap<String, String>()
        config["cloud_name"] = "dt2vnetaw"
        config["api_key"] = "819723664299813"
        config["api_secret"] = "wR2kaZn98ektecTnPLt0c9bBpwo"
        MediaManager.init(this, config)
    }
}