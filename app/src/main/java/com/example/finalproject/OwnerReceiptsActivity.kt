package com.example.finalproject

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class OwnerReceiptsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var containerReceipts: LinearLayout
    private lateinit var btnClearOrders: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_receipts)

        dbHelper = DatabaseHelper(this)
        containerReceipts = findViewById(R.id.containerReceipts)
        btnClearOrders = findViewById<Button>(R.id.btnClearOrders)

        val btnBackReceipts = findViewById<Button>(R.id.btnBackReceipts)
        btnBackReceipts.setOnClickListener {
            finish()
        }

        btnClearOrders.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear All Orders")
                .setMessage("Are you sure you want to clear all order receipts? This will reset orders to 'No Orders For Now!'")
                .setPositiveButton("Clear") { _, _ ->
                    dbHelper.clearAllOrders()
                    Toast.makeText(this, "All orders cleared", Toast.LENGTH_SHORT).show()
                    loadReceipts()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        loadReceipts()
    }

    private fun loadReceipts() {
        containerReceipts.removeAllViews()
        val orders = dbHelper.getAllOrders()

        if (orders.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No Orders For Now!"
                textSize = 18f
                setPadding(16, 32, 16, 16)
                gravity = Gravity.CENTER
                setTextColor(resources.getColor(R.color.text_muted, theme))
            }
            containerReceipts.addView(emptyTv)
            btnClearOrders.isEnabled = false
            return
        }

        btnClearOrders.isEnabled = true
        for (order in orders) {
            val cardView = LayoutInflater.from(this).inflate(R.layout.item_receipt, containerReceipts, false) as LinearLayout

            val tvId = cardView.findViewById<TextView>(R.id.tvReceiptOrderId)
            val tvType = cardView.findViewById<TextView>(R.id.tvReceiptType)
            val tvTime = cardView.findViewById<TextView>(R.id.tvReceiptTimestamp)
            val containerDetails = cardView.findViewById<LinearLayout>(R.id.containerReceiptDetails)
            val tvTotal = cardView.findViewById<TextView>(R.id.tvReceiptTotal)
            val btnMarkPrepared = cardView.findViewById<Button>(R.id.btnMarkPrepared)

            tvId.text = "Order #${order.id}"
            tvType.text = order.orderType
            tvTime.text = order.timestamp
            tvTotal.text = "Total: $${String.format("%.2f", order.totalPrice)}"

            btnMarkPrepared.setOnClickListener {
                dbHelper.deleteOrder(order.id)
                Toast.makeText(this, "Order #${order.id} marked as prepared & delivered!", Toast.LENGTH_SHORT).show()
                loadReceipts()
            }

            containerDetails.removeAllViews()
            for (detail in order.details) {
                val detailTv = TextView(this).apply {
                    text = "• ${detail.foodName} x ${detail.quantity} ($${String.format("%.2f", detail.unitPrice * detail.quantity)})"
                    textSize = 14f
                    setTextColor(resources.getColor(R.color.text_dark, theme))
                    setPadding(0, 2, 0, 2)
                }
                containerDetails.addView(detailTv)
            }

            containerReceipts.addView(cardView)
        }
    }
}
