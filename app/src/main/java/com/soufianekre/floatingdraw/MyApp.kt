package com.soufianekre.floatingdraw

import android.app.Application
import android.content.Context
import timber.log.Timber

class MyApp : Application(){


    override fun onCreate(){
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

    companion object {

        private var instance : MyApp? = null

        fun getContext(): Context {
            return instance!!.applicationContext
        }
    }


}