package com.surya.parfum

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.surya.parfum.databinding.ActivityOrderHistoryBinding

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderHistoryBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var historyAdapter: OrderHistoryAdapter
    private val orderList = mutableListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        setupRecyclerView()
        fetchUserOrders()
    }

    private fun setupRecyclerView() {
        historyAdapter = OrderHistoryAdapter(orderList)
        binding.rvOrderHistory.apply {
            layoutManager = LinearLayoutManager(this@OrderHistoryActivity)
            adapter = historyAdapter
        }
    }

    private fun fetchUserOrders() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Silakan login untuk melihat riwayat", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Ambil data dari 'orders' yang userId-nya sama dengan pengguna saat ini
        db.collection("orders")
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    orderList.clear()
                    for (document in snapshots.documents) {
                        val order = document.toObject(Order::class.java)
                        if (order != null) {
                            order.id = document.id
                            orderList.add(order)
                        }
                    }
                    historyAdapter.notifyDataSetChanged()
                    checkIfEmpty()
                }
            }
    }

    private fun checkIfEmpty() {
        if (orderList.isEmpty()) {
            binding.tvEmptyHistory.visibility = View.VISIBLE
            binding.rvOrderHistory.visibility = View.GONE
        } else {
            binding.tvEmptyHistory.visibility = View.GONE
            binding.rvOrderHistory.visibility = View.VISIBLE
        }
    }
}