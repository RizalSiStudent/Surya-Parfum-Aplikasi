package com.surya.parfum

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Order(
    @get:Exclude
    var id: String = "",

    val customerName: String = "",
    val totalAmount: Long = 0,
    val status: String = "",
    val orderDate: Timestamp? = null,
    val fulfillmentMethod: String = "Ambil di Toko" // Tambahkan ini
)