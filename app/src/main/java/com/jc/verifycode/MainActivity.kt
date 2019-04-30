package com.jc.verifycode

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verifyCode.onVerifyCodeChangedListener = object : VerifyCodeEditText.OnVerifyCodeChangedListener {
            override fun onVerCodeChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.d(TAG, "onVerCodeChanged -> s:$s; start:$start; before:$before; count:$count")
            }

            override fun onInputCompleted(s: CharSequence) {
                Log.d(TAG, "onInputCompleted -> s:$s")
            }

        }
    }

    companion object {
        val TAG = "MainActivity"
    }
}
