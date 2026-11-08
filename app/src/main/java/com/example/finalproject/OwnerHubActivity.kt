package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class OwnerHubActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_hub)

        val btnBackHub = findViewById<Button>(R.id.btnBackHub)
        val btnManageInventory = findViewById<Button>(R.id.btnManageInventory)
        val btnViewAccounts = findViewById<Button>(R.id.btnViewAccounts)
        val btnViewReceipts = findViewById<Button>(R.id.btnViewReceipts)

        btnBackHub.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        btnManageInventory.setOnClickListener {
            val intent = Intent(this, OwnerDashboardActivity::class.java)
            startActivity(intent)
        }

        btnViewAccounts.setOnClickListener {
            val intent = Intent(this, OwnerAccountsListActivity::class.java)
            startActivity(intent)
        }

        btnViewReceipts.setOnClickListener {
            val intent = Intent(this, OwnerReceiptsActivity::class.java)
            startActivity(intent)
        }
    }
}
