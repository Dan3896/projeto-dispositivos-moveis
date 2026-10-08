package com.example.upcampusplus

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn = findViewById<Button>(R.id.btnComecar)
        btn.setOnClickListener {
            val intent = Intent(
                this,
                AgendaActivity::class.java
            )
            startActivity(intent)
        }
    }
}