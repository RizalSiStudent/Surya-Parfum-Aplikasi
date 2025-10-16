package com.surya.parfum

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.surya.parfum.databinding.ActivityCartBinding

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var cartAdapter: CartAdapter
    private val cartItemList = mutableListOf<CartItem>()

    private var totalAmount: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        setupRecyclerView()
        fetchCartItems()

        binding.btnCheckout.setOnClickListener {
            // Filter untuk mendapatkan hanya item yang terpilih
            val selectedItems = ArrayList(cartItemList.filter { it.isSelected })

            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "Pilih setidaknya satu item untuk checkout", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, CheckoutActivity::class.java).apply {
                // Kirim daftar item yang terpilih (membutuhkan CartItem jadi Parcelable)
                putParcelableArrayListExtra("SELECTED_ITEMS", selectedItems)
                // Kirim total harga yang sudah dihitung
                putExtra("TOTAL_AMOUNT", totalAmount)
            }
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            cartItemList,
            onSelectionChanged = {
                updateTotal() // Panggil updateTotal setiap kali ada perubahan centang
            },
            onDeleteClick = { cartItem ->
                deleteCartItem(cartItem)
            }
        )
        binding.rvCartItems.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
        }
    }

    private fun fetchCartItems() {
        val currentUser = auth.currentUser ?: return

        db.collection("carts")
            .whereEqualTo("userId", currentUser.uid)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                snapshots?.let {
                    cartItemList.clear()
                    for (document in it.documents) {
                        val cartItem = document.toObject(CartItem::class.java)
                        if (cartItem != null) {
                            cartItem.id = document.id
                            cartItem.isSelected = true // Set default semua item terpilih
                            cartItemList.add(cartItem)
                        }
                    }
                    cartAdapter.notifyDataSetChanged()
                    updateTotal()
                    checkIfEmpty()
                }
            }
    }

    private fun deleteCartItem(cartItem: CartItem) {
        db.collection("carts").document(cartItem.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "${cartItem.productName} dihapus dari keranjang", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menghapus item", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTotal() {
        var total: Long = 0
        // Hitung total HANYA dari item yang terpilih
        for (item in cartItemList.filter { it.isSelected }) {
            total += item.totalPrice
        }
        totalAmount = total
        binding.tvTotalPrice.text = "Rp $total"

        // Nonaktifkan tombol checkout jika tidak ada item yang dipilih (total 0)
        binding.btnCheckout.isEnabled = total > 0
    }

    private fun checkIfEmpty() {
        if (cartItemList.isEmpty()) {
            binding.tvEmptyCart.visibility = View.VISIBLE
            binding.rvCartItems.visibility = View.GONE
        } else {
            binding.tvEmptyCart.visibility = View.GONE
            binding.rvCartItems.visibility = View.VISIBLE
        }
    }
}