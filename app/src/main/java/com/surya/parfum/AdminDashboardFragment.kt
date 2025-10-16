package com.surya.parfum

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.surya.parfum.databinding.FragmentAdminDashboardBinding

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var productAdapter: ProductAdminAdapter
    private val productList = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        setupRecyclerView() // Now this function exists
        fetchProducts()     // And this one too

        binding.fabAddProduct.setOnClickListener {
            startActivity(Intent(requireActivity(), AddEditProductActivity::class.java))
        }

        // The button "Lihat Pesanan Masuk" is now part of this fragment's layout
        // but the navigation is handled by AdminHomeActivity's BottomNavigationView,
        // so we can remove the button or its listener if it's still there.
    }

    // =====================================================================
    // ===== FUNGSI YANG HILANG SUDAH DITAMBAHKAN DI BAWAH INI =====
    // =====================================================================

    private fun setupRecyclerView() {
        productAdapter = ProductAdminAdapter(
            productList,
            onEditClick = { product ->
                val intent = Intent(requireActivity(), AddEditProductActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                startActivity(intent)
            },
            onDeleteClick = { product ->
                showDeleteConfirmationDialog(product)
            }
        )
        binding.rvAdminProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }
    }

    private fun fetchProducts() {
        db.collection("products").addSnapshotListener { snapshots, error ->
            if (error != null) {
                Toast.makeText(requireContext(), "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            snapshots?.let {
                productList.clear()
                for (document in it.documents) {
                    val product = document.toObject(Product::class.java)
                    if (product != null) {
                        product.id = document.id
                        productList.add(product)
                    }
                }
                productAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun showDeleteConfirmationDialog(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Produk")
            .setMessage("Anda yakin ingin menghapus '${product.name}'?")
            .setPositiveButton("Ya, Hapus") { _, _ ->
                deleteProduct(product.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteProduct(productId: String) {
        db.collection("products").document(productId).delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Produk berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal menghapus produk: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}