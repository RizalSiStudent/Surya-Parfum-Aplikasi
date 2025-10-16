package com.surya.parfum

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.surya.parfum.databinding.ItemOrderAdminBinding
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent

class OrderAdminAdapter(private val orderList: List<Order>) : RecyclerView.Adapter<OrderAdminAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemOrderAdminBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orderList[position]
        holder.binding.apply {
            tvCustomerName.text = order.customerName
            tvOrderTotal.text = "Total: Rp ${order.totalAmount}"
            tvOrderStatus.text = order.status
            tvFulfillment.text = "Metode: ${order.fulfillmentMethod}" // Tambahkan ini

            // Format tanggal agar mudah dibaca
            order.orderDate?.toDate()?.let { date ->
                val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                tvOrderDate.text = format.format(date)
            }
        }
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, AdminOrderDetailActivity::class.java).apply {
                putExtra("ORDER_ID", order.id)
            }
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int = orderList.size
}