package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class CustomerMenuActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var containerMenu: LinearLayout
    private lateinit var btnViewCart: Button
    private lateinit var btnSelectDrinks: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_menu)

        dbHelper = DatabaseHelper(this)
        containerMenu = findViewById(R.id.containerMenu)
        btnViewCart = findViewById<Button>(R.id.btnViewCart)
        btnSelectDrinks = findViewById<Button>(R.id.btnSelectDrinks)

        val btnBackCustomer = findViewById<Button>(R.id.btnBackCustomer)
        btnBackCustomer.setOnClickListener {
            finish()
        }

        btnViewCart.setOnClickListener {
            showCurrentOrderDialog()
        }

        btnSelectDrinks.setOnClickListener {
            val intent = Intent(this, DrinksSelectionActivity::class.java)
            startActivity(intent)
        }

        loadMenu()
    }

    override fun onResume() {
        super.onResume()
        updateCartButtonText()
        loadMenu()
    }

    private fun updateCartButtonText() {
        val count = CartManager.getTotalItemCount()
        btnViewCart.text = "Cart ($count)"
    }

    private fun showCurrentOrderDialog() {
        val cartItems = CartManager.getCartItems()
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Your Current Order")

        if (cartItems.isEmpty()) {
            builder.setMessage("Your order is currently empty.")
        } else {
            val sb = StringBuilder()
            for (item in cartItems) {
                sb.append("• ${item.foodItem.name} x ${item.quantity}  ($${String.format("%.2f", item.foodItem.price * item.quantity)})\n")
            }
            sb.append("\nTotal: $${String.format("%.2f", CartManager.getTotalPrice())}")
            builder.setMessage(sb.toString())
        }

        builder.setPositiveButton("Close", null)
        builder.setNeutralButton("Go to Checkout") { _, _ ->
            val intent = Intent(this, CartCheckoutActivity::class.java)
            startActivity(intent)
        }
        builder.show()
    }

    private fun loadMenu() {
        containerMenu.removeAllViews()
        val inventoryList = dbHelper.getAllInventory()

        val availableItems = inventoryList.filter { it.availability == 1 && it.quantity > 0 }

        if (availableItems.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No food items currently available."
                textSize = 16f
                setPadding(16, 16, 16, 16)
            }
            containerMenu.addView(emptyTv)
            return
        }

        for (item in availableItems) {
            val cardView = LayoutInflater.from(this).inflate(R.layout.item_food_customer, containerMenu, false) as LinearLayout

            val tvName = cardView.findViewById<TextView>(R.id.tvCustFoodName)
            val tvPrice = cardView.findViewById<TextView>(R.id.tvCustFoodPrice)
            val tvStock = cardView.findViewById<TextView>(R.id.tvCustFoodStock)
            val btnAddToCart = cardView.findViewById<Button>(R.id.btnAddToCart)

            tvName.text = item.name
            tvPrice.text = "$${String.format("%.2f", item.price)}"
            tvStock.text = "Available Stock: ${item.quantity}"

            btnAddToCart.setOnClickListener {
                showAddToCartDialog(item)
            }

            containerMenu.addView(cardView)
        }
    }

    private fun showAddToCartDialog(item: FoodItem) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add to Cart: ${item.name}")

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_to_cart, null)
        val etQty = view.findViewById<EditText>(R.id.etCartQty)

        builder.setView(view)

        builder.setPositiveButton("Add") { _, _ ->
            val qtyStr = etQty.text.toString().trim()
            val qty = qtyStr.toIntOrNull() ?: 1

            if (qty <= 0) {
                Toast.makeText(this, "Quantity must be at least 1", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (qty > item.quantity) {
                Toast.makeText(this, "Cannot order more than available stock (${item.quantity})", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val cartItem = CartItem(item, qty, "Standard")
            CartManager.addToCart(cartItem)
            updateCartButtonText()
            Toast.makeText(this, "Added ${item.name} to cart", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}
