package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OwnerDashboardActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var containerInventory: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_dashboard)

        dbHelper = DatabaseHelper(this)
        containerInventory = findViewById(R.id.containerInventory)

        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        val fabAddFood = findViewById<FloatingActionButton>(R.id.fabAddFood)
        fabAddFood.setOnClickListener {
            showAddOrEditDialog(null)
        }

        loadInventoryList()
    }

    private fun loadInventoryList() {
        containerInventory.removeAllViews()
        val inventoryList = dbHelper.getAllInventory()

        if (inventoryList.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No food items in inventory."
                textSize = 16f
                setPadding(16, 16, 16, 16)
            }
            containerInventory.addView(emptyTv)
            return
        }

        for (item in inventoryList) {
            val cardView = LayoutInflater.from(this).inflate(R.layout.item_inventory_owner, containerInventory, false) as LinearLayout

            val tvName = cardView.findViewById<TextView>(R.id.tvItemName)
            val tvPrice = cardView.findViewById<TextView>(R.id.tvItemPrice)
            val tvQuantity = cardView.findViewById<TextView>(R.id.tvItemQuantity)
            val tvStatus = cardView.findViewById<TextView>(R.id.tvItemStatus)
            val btnAddStock = cardView.findViewById<Button>(R.id.btnAddStock)
            val btnEdit = cardView.findViewById<Button>(R.id.btnEditItem)
            val btnDelete = cardView.findViewById<Button>(R.id.btnDeleteItem)

            tvName.text = item.name
            tvPrice.text = "$${String.format("%.2f", item.price)}"
            tvQuantity.text = "Stock: ${item.quantity}"
            if (item.availability == 1) {
                tvStatus.text = "Status: Available"
                tvStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark, theme))
            } else {
                tvStatus.text = "Status: Unavailable (Out of Stock)"
                tvStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark, theme))
            }

            btnAddStock.setOnClickListener {
                showAddStockDialog(item)
            }

            btnEdit.setOnClickListener {
                showAddOrEditDialog(item)
            }

            btnDelete.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete ${item.name}?")
                    .setPositiveButton("Delete") { _, _ ->
                        dbHelper.deleteFoodItem(item.id)
                        Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show()
                        loadInventoryList()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            containerInventory.addView(cardView)
        }
    }

    private fun showAddStockDialog(item: FoodItem) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Stock: ${item.name}")

        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "Enter quantity to add"
        }
        builder.setView(input)

        builder.setPositiveButton("Add") { _, _ ->
            val addStr = input.text.toString().trim()
            if (addStr.isNotEmpty()) {
                val addQty = addStr.toIntOrNull() ?: 0
                val newTotal = item.quantity + addQty
                dbHelper.updateStockQuantity(item.id, newTotal)
                Toast.makeText(this, "Stock updated to $newTotal", Toast.LENGTH_SHORT).show()
                loadInventoryList()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showAddOrEditDialog(item: FoodItem?) {
        val isEditing = item != null
        val builder = AlertDialog.Builder(this)
        builder.setTitle(if (isEditing) "Edit Food Item" else "Add New Food Item")

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_food, null)
        val etName = view.findViewById<EditText>(R.id.etDialogName)
        val etPrice = view.findViewById<EditText>(R.id.etDialogPrice)
        val etQuantity = view.findViewById<EditText>(R.id.etDialogQuantity)

        if (isEditing) {
            etName.setText(item!!.name)
            etPrice.setText(item.price.toString())
            etQuantity.setText(item.quantity.toString())
        }

        builder.setView(view)

        builder.setPositiveButton(if (isEditing) "Update" else "Add") { _, _ ->
            val name = etName.text.toString().trim()
            val priceStr = etPrice.text.toString().trim()
            val qtyStr = etQuantity.text.toString().trim()

            if (name.isEmpty() || priceStr.isEmpty() || qtyStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val price = priceStr.toDoubleOrNull() ?: 0.0
            val qty = qtyStr.toIntOrNull() ?: 0

            if (isEditing) {
                dbHelper.updateFoodItem(item!!.id, name, price, qty)
                Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                dbHelper.addFoodItem(name, price, qty)
                Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
            }
            loadInventoryList()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}
