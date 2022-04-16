package com.mmh.taxiappkotlin

import android.app.Application
import android.content.SharedPreferences
import com.mmh.taxiappkotlin.utils.APPLICATION_ID
import com.mmh.taxiappkotlin.utils.CLIENT_KEY
import com.mmh.taxiappkotlin.utils.SERVER_URL
import com.parse.Parse


class App : Application() {

    companion object {
        var pref: SharedPreferences? = null
    }

    override fun onCreate() {
        super.onCreate()
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId(APPLICATION_ID)
                .clientKey(CLIENT_KEY)
                .server(SERVER_URL)
                .build()
        )
    }
}