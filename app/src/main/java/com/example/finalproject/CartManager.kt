package com.example.finalproject

object CartManager {
    private val cartItems = mutableListOf<CartItem>()

    fun getCartItems(): List<CartItem> = cartItems

    fun addToCart(item: CartItem) {
        val existing = cartItems.find { it.foodItem.id == item.foodItem.id && it.customizations == item.customizations }
        if (existing != null) {
            existing.quantity += item.quantity
        } else {
            cartItems.add(item)
        }
    }

    fun updateQuantity(item: CartItem, delta: Int) {
        item.quantity += delta
        if (item.quantity <= 0) {
            cartItems.remove(item)
        }
    }

    fun removeItem(item: CartItem) {
        cartItems.remove(item)
    }

    fun clearCart() {
        cartItems.clear()
    }

    fun getTotalPrice(): Double {
        return cartItems.sumOf { it.foodItem.price * it.quantity }
    }

    fun getTotalItemCount(): Int {
        return cartItems.sumOf { it.quantity }
    }
}
