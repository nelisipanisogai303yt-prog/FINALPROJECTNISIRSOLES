package com.example.finalproject

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FoodItem(
    val id: Int,
    val name: String,
    val price: Double,
    val quantity: Int,
    val availability: Int
)

data class CartItem(
    val foodItem: FoodItem,
    var quantity: Int,
    val customizations: String
)

data class OwnerAccount(
    val id: Int,
    val username: String,
    val lastLogin: String
)

data class OrderDetailItem(
    val foodName: String,
    val quantity: Int,
    val unitPrice: Double
)

data class OrderTransaction(
    val id: Int,
    val orderType: String,
    val totalPrice: Double,
    val timestamp: String,
    val details: List<OrderDetailItem>
)

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "fast_food_db.db"
        private const val DATABASE_VERSION = 2

        // Table Names
        const val TABLE_ACCOUNTS = "accounts"
        const val TABLE_INVENTORY = "inventory"
        const val TABLE_ORDERS = "orders"
        const val TABLE_ORDER_DETAILS = "order_details"

        // Common column names
        const val COLUMN_ID = "id"

        // Accounts columns
        const val COLUMN_USERNAME = "username"
        const val COLUMN_PASSWORD_HASH = "password_hash"
        const val COLUMN_LAST_LOGIN = "last_login"

        // Inventory columns
        const val COLUMN_NAME = "name"
        const val COLUMN_PRICE = "price"
        const val COLUMN_QUANTITY = "quantity"
        const val COLUMN_AVAILABILITY = "availability"

        // Orders columns
        const val COLUMN_ORDER_TYPE = "order_type"
        const val COLUMN_TOTAL_PRICE = "total_price"
        const val COLUMN_TIMESTAMP = "timestamp"

        // Order Details columns
        const val COLUMN_ORDER_ID = "order_id"
        const val COLUMN_ITEM_ID = "item_id"
        const val COLUMN_UNIT_PRICE = "unit_price"
        const val COLUMN_CUSTOMIZATIONS = "customizations"

        // Hash utility
        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createAccountsTable = ("CREATE TABLE $TABLE_ACCOUNTS ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_USERNAME TEXT UNIQUE, "
                + "$COLUMN_PASSWORD_HASH TEXT, "
                + "$COLUMN_LAST_LOGIN TEXT)")

        val createInventoryTable = ("CREATE TABLE $TABLE_INVENTORY ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_NAME TEXT, "
                + "$COLUMN_PRICE REAL, "
                + "$COLUMN_QUANTITY INTEGER, "
                + "$COLUMN_AVAILABILITY INTEGER)")

        val createOrdersTable = ("CREATE TABLE $TABLE_ORDERS ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_ORDER_TYPE TEXT, "
                + "$COLUMN_TOTAL_PRICE REAL, "
                + "$COLUMN_TIMESTAMP TEXT)")

        val createOrderDetailsTable = ("CREATE TABLE $TABLE_ORDER_DETAILS ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_ORDER_ID INTEGER, "
                + "$COLUMN_ITEM_ID INTEGER, "
                + "$COLUMN_QUANTITY INTEGER, "
                + "$COLUMN_UNIT_PRICE REAL, "
                + "$COLUMN_CUSTOMIZATIONS TEXT)")

        db.execSQL(createAccountsTable)
        db.execSQL(createInventoryTable)
        db.execSQL(createOrdersTable)
        db.execSQL(createOrderDetailsTable)

        // Insert default owner account
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val ownerValues = ContentValues().apply {
            put(COLUMN_USERNAME, "MarkJayson")
            put(COLUMN_PASSWORD_HASH, hashPassword("VSCodeGlazer"))
            put(COLUMN_LAST_LOGIN, currentTime)
        }
        db.insert(TABLE_ACCOUNTS, null, ownerValues)

        // Pre-populate initial food items requested by user
        val initialItems = listOf(
            Triple("1 pc Chicken w/ rice", 3.50, 50),
            Triple("4 pc Chicken Nugget w/ rice", 4.00, 40),
            Triple("1 Porkchop w/ rice", 4.50, 30),
            Triple("burger", 2.50, 60),
            Triple("hotdog", 2.00, 60),
            Triple("fries (small)", 1.50, 80),
            Triple("sundae vanilla ice cream", 1.75, 45)
        )

        for ((name, price, qty) in initialItems) {
            val values = ContentValues().apply {
                put(COLUMN_NAME, name)
                put(COLUMN_PRICE, price)
                put(COLUMN_QUANTITY, qty)
                put(COLUMN_AVAILABILITY, if (qty > 0) 1 else 0)
            }
            db.insert(TABLE_INVENTORY, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDER_DETAILS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INVENTORY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ACCOUNTS")
        onCreate(db)
    }

    // --- Account Operations ---

    fun isOwnerAccountExists(): Boolean {
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_ACCOUNTS", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()

        if (count == 0) {
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val values = ContentValues().apply {
                put(COLUMN_USERNAME, "MarkJayson")
                put(COLUMN_PASSWORD_HASH, hashPassword("VSCodeGlazer"))
                put(COLUMN_LAST_LOGIN, currentTime)
            }
            db.insert(TABLE_ACCOUNTS, null, values)
            count = 1
        }
        db.close()
        return count > 0
    }

    fun registerOwner(username: String, password: String): Boolean {
        val db = this.writableDatabase
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD_HASH, hashPassword(password))
            put(COLUMN_LAST_LOGIN, currentTime)
        }
        val result = db.insert(TABLE_ACCOUNTS, null, values)
        db.close()
        return result != -1L
    }

    fun authenticateOwner(username: String, password: String): Boolean {
        isOwnerAccountExists() // Ensure default account exists

        val db = this.writableDatabase
        val hash = hashPassword(password)
        val cursor = db.query(
            TABLE_ACCOUNTS,
            arrayOf(COLUMN_ID),
            "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD_HASH = ?",
            arrayOf(username, hash),
            null, null, null
        )
        val isValid = cursor.moveToFirst()
        if (isValid) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            cursor.close()
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val values = ContentValues().apply {
                put(COLUMN_LAST_LOGIN, currentTime)
            }
            db.update(TABLE_ACCOUNTS, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        } else {
            cursor.close()
        }
        db.close()
        return isValid
    }

    fun getAllOwnerAccounts(): List<OwnerAccount> {
        val list = mutableListOf<OwnerAccount>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_ID, $COLUMN_USERNAME, $COLUMN_LAST_LOGIN FROM $TABLE_ACCOUNTS", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
                val lastLogin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_LOGIN)) ?: "Never"
                list.add(OwnerAccount(id, username, lastLogin))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    // --- Inventory Operations ---

    fun getAllInventory(): List<FoodItem> {
        val list = mutableListOf<FoodItem>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_INVENTORY", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE))
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY))
                val availability = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AVAILABILITY))
                list.add(FoodItem(id, name, price, quantity, availability))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    fun addFoodItem(name: String, price: Double, quantity: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_PRICE, price)
            put(COLUMN_QUANTITY, quantity)
            put(COLUMN_AVAILABILITY, if (quantity > 0) 1 else 0)
        }
        val id = db.insert(TABLE_INVENTORY, null, values)
        db.close()
        return id != -1L
    }

    fun updateFoodItem(id: Int, name: String, price: Double, quantity: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_PRICE, price)
            put(COLUMN_QUANTITY, quantity)
            put(COLUMN_AVAILABILITY, if (quantity > 0) 1 else 0)
        }
        val rows = db.update(TABLE_INVENTORY, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return rows > 0
    }

    fun updateStockQuantity(id: Int, newQuantity: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_QUANTITY, newQuantity)
            put(COLUMN_AVAILABILITY, if (newQuantity > 0) 1 else 0)
        }
        val rows = db.update(TABLE_INVENTORY, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return rows > 0
    }

    fun deleteFoodItem(id: Int): Boolean {
        val db = this.writableDatabase
        val rows = db.delete(TABLE_INVENTORY, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return rows > 0
    }

    // --- Order Operations ---

    fun placeOrder(orderType: String, totalPrice: Double, cartItems: List<CartItem>): Boolean {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            for (cartItem in cartItems) {
                // Only validate stock for items tracked in inventory (positive IDs)
                if (cartItem.foodItem.id > 0) {
                    val cursor = db.query(
                        TABLE_INVENTORY,
                        arrayOf(COLUMN_QUANTITY),
                        "$COLUMN_ID = ?",
                        arrayOf(cartItem.foodItem.id.toString()),
                        null, null, null
                    )
                    if (cursor.moveToFirst()) {
                        val currentStock = cursor.getInt(0)
                        cursor.close()
                        if (currentStock < cartItem.quantity) {
                            db.endTransaction()
                            db.close()
                            return false
                        }
                    } else {
                        cursor.close()
                        db.endTransaction()
                        db.close()
                        return false
                    }
                }
            }

            val orderValues = ContentValues().apply {
                put(COLUMN_ORDER_TYPE, orderType)
                put(COLUMN_TOTAL_PRICE, totalPrice)
                put(COLUMN_TIMESTAMP, System.currentTimeMillis().toString())
            }
            val orderId = db.insert(TABLE_ORDERS, null, orderValues)
            if (orderId == -1L) {
                db.endTransaction()
                db.close()
                return false
            }

            for (cartItem in cartItems) {
                val detailValues = ContentValues().apply {
                    put(COLUMN_ORDER_ID, orderId)
                    put(COLUMN_ITEM_ID, cartItem.foodItem.id)
                    put(COLUMN_QUANTITY, cartItem.quantity)
                    put(COLUMN_UNIT_PRICE, cartItem.foodItem.price)
                    put(COLUMN_CUSTOMIZATIONS, cartItem.customizations)
                }
                db.insert(TABLE_ORDER_DETAILS, null, detailValues)

                // Only reduce stock for items tracked in inventory (positive IDs)
                if (cartItem.foodItem.id > 0) {
                    val newStock = cartItem.foodItem.quantity - cartItem.quantity
                    val updatedStock = if (newStock < 0) 0 else newStock
                    val invValues = ContentValues().apply {
                        put(COLUMN_QUANTITY, updatedStock)
                        put(COLUMN_AVAILABILITY, if (updatedStock > 0) 1 else 0)
                    }
                    db.update(TABLE_INVENTORY, invValues, "$COLUMN_ID = ?", arrayOf(cartItem.foodItem.id.toString()))
                }
            }

            db.setTransactionSuccessful()
            db.endTransaction()
            db.close()
            return true
        } catch (e: Exception) {
            db.endTransaction()
            db.close()
            return false
        }
    }

    fun getAllOrders(): List<OrderTransaction> {
        val list = mutableListOf<OrderTransaction>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ORDERS ORDER BY $COLUMN_ID DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val orderId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val orderType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_TYPE))
                val totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_PRICE))
                val timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))

                val detailsList = mutableListOf<OrderDetailItem>()
                val detailCursor = db.rawQuery(
                    "SELECT d.$COLUMN_ITEM_ID, d.$COLUMN_QUANTITY, d.$COLUMN_UNIT_PRICE, i.$COLUMN_NAME " +
                            "FROM $TABLE_ORDER_DETAILS d LEFT JOIN $TABLE_INVENTORY i ON d.$COLUMN_ITEM_ID = i.$COLUMN_ID " +
                            "WHERE d.$COLUMN_ORDER_ID = ?",
                    arrayOf(orderId.toString())
                )
                if (detailCursor.moveToFirst()) {
                    do {
                        val itemId = detailCursor.getInt(detailCursor.getColumnIndexOrThrow(COLUMN_ITEM_ID))
                        val qty = detailCursor.getInt(detailCursor.getColumnIndexOrThrow(COLUMN_QUANTITY))
                        val price = detailCursor.getDouble(detailCursor.getColumnIndexOrThrow(COLUMN_UNIT_PRICE))

                        val foodName = if (itemId > 0) {
                            detailCursor.getString(detailCursor.getColumnIndexOrThrow(COLUMN_NAME)) ?: "Food Item"
                        } else {
                            when (itemId) {
                                -1 -> "Water"
                                -2 -> "Coke"
                                -3 -> "Sprite"
                                -4 -> "Royal"
                                -5 -> "Juice"
                                else -> "Beverage"
                            }
                        }

                        detailsList.add(OrderDetailItem(foodName, qty, price))
                    } while (detailCursor.moveToNext())
                }
                detailCursor.close()

                val formattedTime = try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    sdf.format(Date(timestamp.toLong()))
                } catch (e: Exception) {
                    timestamp
                }

                list.add(OrderTransaction(orderId, orderType, totalPrice, formattedTime, detailsList))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    fun clearAllOrders(): Boolean {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $TABLE_ORDER_DETAILS")
        val rows = db.delete(TABLE_ORDERS, null, null)
        db.close()
        return rows >= 0
    }

    fun deleteOrder(orderId: Int): Boolean {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_ORDER_DETAILS, "$COLUMN_ORDER_ID = ?", arrayOf(orderId.toString()))
            val rows = db.delete(TABLE_ORDERS, "$COLUMN_ID = ?", arrayOf(orderId.toString()))
            db.setTransactionSuccessful()
            db.endTransaction()
            db.close()
            return rows > 0
        } catch (e: Exception) {
            db.endTransaction()
            db.close()
            return false
        }
    }
}
