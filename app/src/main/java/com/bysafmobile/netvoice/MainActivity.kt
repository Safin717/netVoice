package com.bysafmobile.netvoice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    val TAG: String = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.w(TAG, "start of onCreate function")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val firstName: String = "Ivan "
        val lastName: String = "Ivanov"
        var age: Int = 37
        var height: Double = 172.2

        val person: String = "Name: $firstName Surname: $lastName Age: $age Height: $height "
        val result: TextView = findViewById(R.id.result)

        result.text = person
        Log.w(TAG, "end of onCreate function")
    }
}