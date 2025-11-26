package com.surya.parfum

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.surya.parfum.databinding.ItemOrderAdminBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderAdminAdapter(
    private val orderList: List<Order>,
    private val listener: OrderActionListener
) : RecyclerView.Adapter<OrderAdminAdapter.OrderViewHolder>() {

    // Tambahkan fungsi onCompleteClick di Interface
    interface OrderActionListener {
        fun onApproveClick(order: Order)
        fun onRejectClick(order: Order)
        fun onCompleteClick(order: Order) // <-- Fungsi Baru
        fun onItemClick(order: Order)
    }

    inner class OrderViewHolder(val binding: ItemOrderAdminBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]
        val context = holder.itemView.context

        holder.binding.apply {
            tvCustomerName.text = order.customerName ?: "Pengguna Anonim"
            tvOrderTotal.text = formatCurrency(order.totalAmount)
            tvFulfillment.text = "Metode: ${order.fulfillmentMethod ?: "Tidak diketahui"}"
            tvOrderStatus.text = order.status

            order.orderDate?.toDate()?.let { date ->
                val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID"))
                tvOrderDate.text = format.format(date)
            }

            updateStatusUI(tvOrderStatus, order.status, context)

            // === LOGIKA TOMBOL DINAMIS ===
            when (order.status) {
                "Diproses" -> {
                    // Pesanan Baru: Tampilkan Setujui & Tolak
                    llActionButtons.visibility = View.VISIBLE
                    btnApprove.visibility = View.VISIBLE
                    btnApprove.text = "Setujui"
                    btnReject.visibility = View.VISIBLE
                }
                "Disetujui" -> {
                    // Pesanan Sedang Dikemas: Tampilkan Tombol "Selesaikan"
                    llActionButtons.visibility = View.VISIBLE
                    btnApprove.visibility = View.VISIBLE
                    btnApprove.text = "Selesaikan" // Ganti teks tombol
                    btnReject.visibility = View.GONE // Sembunyikan tombol tolak
                }
                else -> {
                    // Pesanan Selesai/Ditolak: Sembunyikan semua tombol
                    llActionButtons.visibility = View.GONE
                }
            }

            // Listener Klik Tombol Approve (berfungsi ganda: Setujui atau Selesaikan)
            btnApprove.setOnClickListener {
                if (order.status == "Diproses") {
                    listener.onApproveClick(order)
                } else if (order.status == "Disetujui") {
                    listener.onCompleteClick(order)
                }
            }

            btnReject.setOnClickListener {
                listener.onRejectClick(order)
            }

            root.setOnClickListener {
                listener.onItemClick(order)
            }
        }
    }

    override fun getItemCount(): Int = orderList.size

    private fun formatCurrency(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }

    private fun updateStatusUI(view: View, status: String, context: Context) {
        val colorResId = when (status) {
            "Disetujui" -> android.R.color.holo_green_dark
            "Ditolak" -> android.R.color.holo_red_dark
            "Selesai" -> android.R.color.holo_blue_dark
            else -> android.R.color.holo_orange_dark
        }
        view.backgroundTintList = ContextCompat.getColorStateList(context, colorResId)
    }
}