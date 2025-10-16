package com.surya.parfum

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.surya.parfum.databinding.ActivityAdminOrderDetailBinding
import java.text.SimpleDateFormat
import java.util.*

// Tidak ada lagi data class di sini

class AdminOrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminOrderDetailBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var itemsAdapter: OrderDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        binding.topAppBar.setNavigationOnClickListener { finish() }

        val orderId = intent.getStringExtra("ORDER_ID")
        if (orderId == null) {
            Toast.makeText(this, "ID Pesanan tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvOrderId.text = "ID Pesanan: $orderId"
        fetchOrderDetail(orderId)
    }

    private fun fetchOrderDetail(orderId: String) {
        db.collection("orders").document(orderId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Gunakan OrderDetail dari file barunya
                    val orderDetail = document.toObject(OrderDetail::class.java)
                    if (orderDetail != null) {
                        populateUi(orderDetail)
                    }
                } else {
                    Toast.makeText(this, "Data pesanan tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun populateUi(order: OrderDetail) {
        binding.tvCustomerName.text = "Nama: ${order.customerName}"
        binding.tvCustomerAddress.text = "Alamat: ${order.address}"
        binding.tvCustomerPhone.text = "Telepon: ${order.phone}"
        binding.tvOrderStatus.text = "Status: ${order.status}"
        binding.tvTotalAmount.text = "Total: Rp ${order.totalAmount}"

        order.orderDate?.toDate()?.let { date ->
            val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            binding.tvOrderDate.text = "Tanggal: ${format.format(date)}"
        }

        // ===== BAGIAN YANG DIPERBAIKI ADA DI SINI =====
        // Gunakan 'let' untuk membuat blok kode yang aman dari null
        order.customerLocation?.let { location ->
            // Cek lagi apakah metodenya memang diantar
            if (order.fulfillmentMethod == "Antar ke Alamat") {
                binding.btnViewCustomerLocation.visibility = View.VISIBLE
                binding.btnViewCustomerLocation.setOnClickListener {
                    // Di dalam blok 'let', 'location' dijamin tidak null
                    val lat = location.latitude
                    val lng = location.longitude
                    val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lng")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                }
            }
        }

        itemsAdapter = OrderDetailAdapter(order.items)
        binding.rvOrderItems.apply {
            layoutManager = LinearLayoutManager(this@AdminOrderDetailActivity)
            adapter = itemsAdapter
        }
    }
}