package com.surya.parfum

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize // Tambahkan anotasi ini
data class CartItem(
    @get:Exclude
    var id: String = "",

    val productName: String = "",
    val selectedSize: Int = 0,
    val quantity: Int = 0,
    val totalPrice: Long = 0,
    val imageUrl: String = "",

    // Properti baru untuk melacak status centang
    // @get:Exclude agar tidak disimpan ke Firestore
    @get:Exclude
    var isSelected: Boolean = true // Defaultnya semua item terpilih

) : Parcelable // Implementasikan Parcelable