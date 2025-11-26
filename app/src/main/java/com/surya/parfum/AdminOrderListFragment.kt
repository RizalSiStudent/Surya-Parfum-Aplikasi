package com.surya.parfum

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.surya.parfum.databinding.FragmentAdminOrderListBinding

class AdminOrderListFragment : Fragment(), OrderAdminAdapter.OrderActionListener {

    private var _binding: FragmentAdminOrderListBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var orderAdapter: OrderAdminAdapter
    private val orderList = mutableListOf<Order>()

    // Default filter
    private var filterType: String = "new"

    companion object {
        fun newInstance(filterType: String): AdminOrderListFragment {
            val fragment = AdminOrderListFragment()
            val args = Bundle()
            args.putString("FILTER_TYPE", filterType)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            filterType = it.getString("FILTER_TYPE", "new") ?: "new"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminOrderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        setupRecyclerView()
        fetchOrders()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdminAdapter(orderList, this)
        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }
    }

    private fun fetchOrders() {
        var query: Query = db.collection("orders")

        // === LOGIKA 3 TAB ===
        when (filterType) {
            "new" -> {
                // Tab 1: Hanya pesanan baru yang butuh persetujuan
                query = query.whereEqualTo("status", "Diproses")
            }
            "packing" -> {
                // Tab 2: Pesanan yang sudah disetujui dan sedang dikemas
                query = query.whereEqualTo("status", "Disetujui")
            }
            "history" -> {
                // Tab 3: Pesanan yang sudah selesai atau ditolak
                query = query.whereIn("status", listOf("Selesai", "Ditolak"))
            }
        }

        query = query.orderBy("orderDate", Query.Direction.DESCENDING)

        query.addSnapshotListener { snapshots, error ->
            if (error != null) {
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
                orderAdapter.notifyDataSetChanged()
            }
        }
    }

    // === AKSI ===

    override fun onApproveClick(order: Order) {
        // "Diproses" -> "Disetujui" (Akan pindah dari Tab 1 ke Tab 2)
        updateOrderStatus(order.id, "Disetujui")
    }

    override fun onRejectClick(order: Order) {
        // "Diproses" -> "Ditolak" (Akan pindah dari Tab 1 ke Tab 3)
        updateOrderStatus(order.id, "Ditolak")
    }

    override fun onCompleteClick(order: Order) {
        // "Disetujui" -> "Selesai" (Akan pindah dari Tab 2 ke Tab 3)
        updateOrderStatus(order.id, "Selesai")
    }

    override fun onItemClick(order: Order) {
        val intent = Intent(requireContext(), AdminOrderDetailActivity::class.java).apply {
            putExtra("ORDER_ID", order.id)
        }
        startActivity(intent)
    }

    private fun updateOrderStatus(orderId: String, newStatus: String) {
        val updateMap = mapOf("status" to newStatus)

        db.collection("orders").document(orderId)
            .update(updateMap)
            .addOnSuccessListener {
                val message = when(newStatus) {
                    "Disetujui" -> "Pesanan disetujui. Pindah ke tab 'Sedang Dikemas'"
                    "Selesai" -> "Pesanan selesai. Pindah ke tab 'Riwayat'"
                    "Ditolak" -> "Pesanan ditolak. Pindah ke tab 'Riwayat'"
                    else -> "Status diperbarui"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}