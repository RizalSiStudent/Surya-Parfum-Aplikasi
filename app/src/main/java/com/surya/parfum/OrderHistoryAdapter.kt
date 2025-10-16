package com.surya.parfum

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.surya.parfum.databinding.ItemOrderUserBinding
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryAdapter(private val orderList: List<Order>) : RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemOrderUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orderList[position]
        holder.binding.apply {
            tvOrderTotal.text = "Total: Rp ${order.totalAmount}"
            tvOrderStatus.text = order.status

            order.orderDate?.toDate()?.let { date ->
                val format = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                tvOrderDate.text = format.format(date)
            }
        }
    }

    override fun getItemCount(): Int = orderList.size
}