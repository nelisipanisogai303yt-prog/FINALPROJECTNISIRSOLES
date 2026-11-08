package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class CartCheckoutActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var containerCartItems: LinearLayout
    private lateinit var tvTotalPrice: TextView
    private lateinit var rgOrderPreferences: RadioGroup
    private lateinit var cbUtensils: CheckBox
    private lateinit var cbNapkins: CheckBox
    private lateinit var cbKetchup: CheckBox
    private lateinit var cbExtraBag: CheckBox
    private lateinit var btnPlaceOrder: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart_checkout)

        dbHelper = DatabaseHelper(this)
        containerCartItems = findViewById(R.id.containerCartItems)

        val btnBackCart = findViewById<Button>(R.id.btnBackCart)
        btnBackCart.setOnClickListener {
            finish()
        }

        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        rgOrderPreferences = findViewById(R.id.rgOrderPreferences)
        cbUtensils = findViewById(R.id.cbUtensils)
        cbNapkins = findViewById(R.id.cbNapkins)
        cbKetchup = findViewById(R.id.cbKetchup)
        cbExtraBag = findViewById(R.id.cbExtraBag)
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder)

        btnPlaceOrder.setOnClickListener {
            if (CartManager.getCartItems().isEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("Cannot Place Order")
                    .setMessage("Your order cannot be placed because your cart is empty. Please select at least one meal from the menu before placing your order.")
                    .setPositiveButton("Go to Menu") { _, _ ->
                        finish()
                    }
                    .show()
                return@setOnClickListener
            }

            val baseOrderType = when (rgOrderPreferences.checkedRadioButtonId) {
                R.id.rbTakeout -> "Takeout"
                R.id.rbDriveThru -> "Drive-Thru"
                R.id.rbCurbside -> "Curbside Pickup"
                else -> "Dine-In"
            }

            val requestsList = mutableListOf<String>()
            if (cbUtensils.isChecked) requestsList.add("Utensils")
            if (cbNapkins.isChecked) requestsList.add("Napkins")
            if (cbKetchup.isChecked) requestsList.add("Ketchup")
            if (cbExtraBag.isChecked) requestsList.add("Extra plastic bag")

            val orderType = if (requestsList.isNotEmpty()) {
                "$baseOrderType [Requests: ${requestsList.joinToString(", ")}]"
            } else {
                baseOrderType
            }

            val finalTotal = CartManager.getTotalPrice()

            val success = dbHelper.placeOrder(orderType, finalTotal, CartManager.getCartItems())
            if (success) {
                CartManager.clearCart()
                val intent = Intent(this, OrderSuccessActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Order failed! Insufficient stock for one or more items.", Toast.LENGTH_LONG).show()
                loadCartView()
            }
        }

        loadCartView()
    }

    private fun loadCartView() {
        containerCartItems.removeAllViews()
        val items = CartManager.getCartItems()

        if (items.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "Your cart is empty. Please return to the menu and select at least one meal before placing an order."
                textSize = 15f
                setPadding(16, 16, 16, 16)
            }
            containerCartItems.addView(emptyTv)
            tvTotalPrice.text = "Total: $0.00"
            btnPlaceOrder.isEnabled = false
            return
        }

        btnPlaceOrder.isEnabled = true
        for (cartItem in items) {
            val cardView = LayoutInflater.from(this).inflate(R.layout.item_cart, containerCartItems, false) as LinearLayout

            val tvName = cardView.findViewById<TextView>(R.id.tvCartItemName)
            val tvPrice = cardView.findViewById<TextView>(R.id.tvCartItemPrice)
            val tvCust = cardView.findViewById<TextView>(R.id.tvCartItemCustomizations)
            val tvQty = cardView.findViewById<TextView>(R.id.tvCartItemQty)
            val btnMinus = cardView.findViewById<Button>(R.id.btnCartMinus)
            val btnPlus = cardView.findViewById<Button>(R.id.btnCartPlus)
            val btnRemove = cardView.findViewById<Button>(R.id.btnCartRemove)

            tvName.text = cartItem.foodItem.name
            tvPrice.text = "$${String.format("%.2f", cartItem.foodItem.price * cartItem.quantity)}"
            tvCust.visibility = View.GONE
            tvQty.text = "Qty: ${cartItem.quantity}"

            btnMinus.setOnClickListener {
                CartManager.updateQuantity(cartItem, -1)
                loadCartView()
            }

            btnPlus.setOnClickListener {
                if (cartItem.quantity < cartItem.foodItem.quantity) {
                    CartManager.updateQuantity(cartItem, 1)
                    loadCartView()
                } else {
                    Toast.makeText(this, "Cannot add more. Max stock reached.", Toast.LENGTH_SHORT).show()
                }
            }

            btnRemove.setOnClickListener {
                CartManager.removeItem(cartItem)
                loadCartView()
            }

            containerCartItems.addView(cardView)
        }

        val total = CartManager.getTotalPrice()
        tvTotalPrice.text = "Total: $${String.format("%.2f", total)}"
    }
}
