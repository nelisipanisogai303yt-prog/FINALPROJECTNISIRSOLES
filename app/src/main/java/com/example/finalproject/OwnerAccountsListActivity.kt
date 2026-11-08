package com.example.finalproject

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OwnerAccountsListActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var containerAccounts: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_accounts_list)

        dbHelper = DatabaseHelper(this)
        containerAccounts = findViewById(R.id.containerAccounts)

        val btnBackAccounts = findViewById<Button>(R.id.btnBackAccounts)
        btnBackAccounts.setOnClickListener {
            finish()
        }

        loadAccountsList()
    }

    private fun loadAccountsList() {
        containerAccounts.removeAllViews()
        val accounts = dbHelper.getAllOwnerAccounts()

        if (accounts.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No registered admin accounts found."
                textSize = 16f
                setPadding(16, 16, 16, 16)
            }
            containerAccounts.addView(emptyTv)
            return
        }

        for (account in accounts) {
            val cardView = LayoutInflater.from(this).inflate(R.layout.item_owner_account, containerAccounts, false) as LinearLayout

            val tvUsername = cardView.findViewById<TextView>(R.id.tvAdminUsername)
            val tvLastLogin = cardView.findViewById<TextView>(R.id.tvAdminLastLogin)

            tvUsername.text = "Username: ${account.username}"
            tvLastLogin.text = "Last Login: ${account.lastLogin}"

            containerAccounts.addView(cardView)
        }
    }
}
