package com.surya.parfum

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint // <-- TAMBAHKAN BARIS INI

data class OrderDetail(
    val customerName: String = "",
    val address: String = "",
    val phone: String = "",
    val totalAmount: Long = 0,
    val status: String = "",
    val orderDate: Timestamp? = null,
    val items: List<java.util.HashMap<String, Any>> = emptyList(),
    val fulfillmentMethod: String = "",
    val customerLocation: GeoPoint? = null
)