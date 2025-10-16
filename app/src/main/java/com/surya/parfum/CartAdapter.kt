package com.surya.parfum

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.surya.parfum.databinding.ItemCartBinding

class CartAdapter(
    private val cartItems: MutableList<CartItem>,
    private val onSelectionChanged: () -> Unit, // Callback untuk memberitahu Activity
    private val onDeleteClick: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = cartItems[position]
        holder.binding.apply {
            tvProductName.text = item.productName
            tvProductSize.text = "Ukuran: ${item.selectedSize} ml"
            tvProductPrice.text = "Rp ${item.totalPrice}"

            Glide.with(holder.itemView.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(ivProductImage)

            // Atur status checkbox tanpa memicu listener
            cbSelectItem.setOnCheckedChangeListener(null)
            cbSelectItem.isChecked = item.isSelected

            // Tambahkan listener baru setelah status diatur
            cbSelectItem.setOnCheckedChangeListener { _, isChecked ->
                item.isSelected = isChecked
                onSelectionChanged() // Panggil callback agar Activity bisa update total harga
            }

            ivDeleteItem.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }

    override fun getItemCount(): Int = cartItems.size
}