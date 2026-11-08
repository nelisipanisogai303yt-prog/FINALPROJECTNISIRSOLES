package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

data class DrinkOption(val id: Int, val name: String, val price: Double)

class DrinksSelectionActivity : AppCompatActivity() {

    private lateinit var containerDrinks: LinearLayout
    private lateinit var containerChosenMeal: LinearLayout

    private val drinkOptions = listOf(
        DrinkOption(-1, "Water (Free)", 0.00),
        DrinkOption(-2, "Coke", 1.00),
        DrinkOption(-3, "Sprite", 1.00),
        DrinkOption(-4, "Royal", 1.00),
        DrinkOption(-5, "Juice", 0.50)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drinks_selection)

        val btnBackDrinks = findViewById<Button>(R.id.btnBackDrinks)
        containerDrinks = findViewById(R.id.containerDrinks)
        containerChosenMeal = findViewById(R.id.containerChosenMeal)
        val btnContinueCheckout = findViewById<Button>(R.id.btnContinueCheckout)

        btnBackDrinks.setOnClickListener {
            finish()
        }

        loadDrinksList()
        loadChosenSummary()

        btnContinueCheckout.setOnClickListener {
            val intent = Intent(this, CartCheckoutActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadChosenSummary()
    }

    private fun loadDrinksList() {
        containerDrinks.removeAllViews()
        for (drink in drinkOptions) {
            val cardView = LayoutInflater.from(this).inflate(R.layout.item_drink, containerDrinks, false) as LinearLayout

            val tvName = cardView.findViewById<TextView>(R.id.tvDrinkName)
            val tvPrice = cardView.findViewById<TextView>(R.id.tvDrinkPrice)
            val btnAddDrink = cardView.findViewById<Button>(R.id.btnAddDrink)

            tvName.text = drink.name
            tvPrice.text = "$${String.format("%.2f", drink.price)}"

            btnAddDrink.setOnClickListener {
                val foodItem = FoodItem(drink.id, drink.name, drink.price, 999, 1)
                val cartItem = CartItem(foodItem, 1, "Beverage")
                CartManager.addToCart(cartItem)
                Toast.makeText(this, "Added ${drink.name} to cart", Toast.LENGTH_SHORT).show()
                loadChosenSummary()
            }

            containerDrinks.addView(cardView)
        }
    }

    private fun loadChosenSummary() {
        containerChosenMeal.removeAllViews()
        val cartItems = CartManager.getCartItems()
        if (cartItems.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No items selected yet."
                textSize = 14f
            }
            containerChosenMeal.addView(emptyTv)
        } else {
            for (cartItem in cartItems) {
                val itemTv = TextView(this).apply {
                    text = "• ${cartItem.foodItem.name} x ${cartItem.quantity} ($${String.format("%.2f", cartItem.foodItem.price * cartItem.quantity)})"
                    textSize = 14f
                    setTextColor(resources.getColor(R.color.text_dark, theme))
                    setPadding(0, 4, 0, 4)
                }
                containerChosenMeal.addView(itemTv)
            }
        }
    }
}
