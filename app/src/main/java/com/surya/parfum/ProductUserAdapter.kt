package com.surya.parfum

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.surya.parfum.databinding.ItemProductUserBinding
import java.text.NumberFormat
import java.util.*
import kotlin.collections.minOrNull

// Ubah constructor menjadi 'var' agar list bisa diupdate
class ProductUserAdapter(private var productList: List<Product>) :
    RecyclerView.Adapter<ProductUserAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemProductUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]
        holder.binding.apply {
            tvProductName.text = product.name

            // Tampilkan harga termurah (Logic Asli Anda)
            // Pastikan pricePerMl dan availableSizes tidak null/kosong
            val sizes = product.availableSizes
            if (sizes.isNotEmpty()) {
                val minSize = sizes.minOrNull() ?: 0
                val minPrice = product.pricePerMl * minSize
                tvProductPrice.text = "Mulai dari ${formatCurrency(minPrice)}"
            } else {
                // Fallback jika sizes kosong, mungkin pakai harga tetap atau tampilkan "-"
                if (product.price > 0) {
                    tvProductPrice.text = formatCurrency(product.price)
                } else {
                    tvProductPrice.text = "Stok Kosong"
                }
            }

            Glide.with(holder.itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(ivProductImage)
        }
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ProductDetailActivity::class.java).apply {
                putExtra("PRODUCT_ID", product.id)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = productList.size

    // === FUNGSI BARU: Update List untuk Search ===
    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }

    private fun formatCurrency(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }
}