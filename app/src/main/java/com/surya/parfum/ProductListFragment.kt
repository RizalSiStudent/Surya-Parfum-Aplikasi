package com.surya.parfum

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.surya.parfum.databinding.FragmentProductListBinding
import kotlin.apply
import kotlin.jvm.java

class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private val TAG = "ProductListDebug"

    private lateinit var db: FirebaseFirestore
    private lateinit var productAdapter: ProductUserAdapter
    private val productList = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        setupRecyclerView()
        fetchProducts()
    }

    private fun setupRecyclerView() {
        // Ganti ProductUserAdapter dengan adapter yang Anda buat untuk user
        productAdapter = ProductUserAdapter(productList)
        binding.rvUserProducts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productAdapter
        }
    }

    private fun fetchProducts() {
        Log.d(TAG, "Mulai mengambil data dari Firestore...") // Log 1: Memastikan fungsi dipanggil
        binding.progressBar.visibility = View.VISIBLE
        binding.rvUserProducts.visibility = View.GONE

        db.collection("products")
            .get()
            .addOnSuccessListener { result ->
                // Log 2: Memeriksa apakah pengambilan data berhasil
                Log.d(TAG, "Sukses! Ditemukan ${result.size()} dokumen.")

                binding.progressBar.visibility = View.GONE
                if (!result.isEmpty) {
                    productList.clear()
                    for (document in result) {
                        try {
                            val product = document.toObject(Product::class.java)
                            product.id = document.id
                            productList.add(product)
                            // Log 3: Memastikan setiap produk berhasil ditambahkan ke list
                            Log.d(TAG, "Menambahkan produk: ${product.name}")
                        } catch (e: Exception) {
                            // Log 4: Jika ada error saat mengubah data (misal tipe data tidak cocok)
                            Log.e(TAG, "Error converting document ${document.id}", e)
                        }
                    }
                    productAdapter.notifyDataSetChanged()
                    binding.rvUserProducts.visibility = View.VISIBLE
                } else {
                    Toast.makeText(context, "Belum ada produk.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                // Log 5: Jika ada error besar (misal masalah koneksi atau izin)
                Log.e(TAG, "Gagal mengambil data!", exception)
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Gagal memuat data: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}