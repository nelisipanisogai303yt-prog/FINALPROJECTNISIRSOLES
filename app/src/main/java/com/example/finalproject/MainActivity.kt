package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper(this)

        val btnCustomer = findViewById<Button>(R.id.btnCustomer)
        val tvOwnerPortal = findViewById<TextView>(R.id.tvOwnerPortal)

        btnCustomer.setOnClickListener {
            val intent = Intent(this, CustomerMenuActivity::class.java)
            startActivity(intent)
        }

        tvOwnerPortal.setOnClickListener {
            val intent = Intent(this, OwnerLoginActivity::class.java)
            startActivity(intent)
        }
    }
}
