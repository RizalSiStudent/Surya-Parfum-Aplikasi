package com.surya.parfum

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.surya.parfum.databinding.ItemOrderDetailBinding

// Kita gunakan HashMap untuk fleksibilitas karena data item disimpan sebagai Map di Firestore
class OrderDetailAdapter(private val items: List<HashMap<String, Any>>) :
    RecyclerView.Adapter<OrderDetailAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemOrderDetailBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvItemName.text = item["productName"] as? String ?: "Nama Produk Tidak Ada"
            val size = item["selectedSize"]
            val price = item["totalPrice"]
            tvItemDetails.text = "Ukuran: $size ml - Rp $price"
        }
    }

    override fun getItemCount(): Int = items.size
}