package com.soufianekre.floatingdraw

import android.app.Application
import android.content.Context
import com.airbnb.lottie.Lottie
import com.airbnb.lottie.LottieConfig
import timber.log.Timber

class FloatingDrawApp : Application(){


    override fun onCreate(){
        super.onCreate()
        Timber.plant(Timber.DebugTree())

    }

    companion object {

        private var instance : FloatingDrawApp? = null

        fun getContext(): Context {
            return instance!!.applicationContext
        }
    }


}