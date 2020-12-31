package com.soufianekre.floatingdraw.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import es.dmoral.toasty.Toasty

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun showInfo(msg : String){
        Toasty.error(this,msg,Toasty.LENGTH_SHORT).show()
    }

    fun showSuccess(msg : String){
        Toasty.error(this,msg,Toasty.LENGTH_SHORT).show()
    }

    fun showError(msg : String){
        Toasty.error(this,msg,Toasty.LENGTH_SHORT).show()
    }

}