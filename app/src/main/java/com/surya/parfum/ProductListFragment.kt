package com.surya.parfum

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager // Saran: Gunakan Grid untuk tampilan User
import com.google.firebase.firestore.FirebaseFirestore
import com.surya.parfum.databinding.FragmentProductListBinding
import java.util.*

class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private val TAG = "ProductListDebug"

    private lateinit var db: FirebaseFirestore
    private lateinit var productAdapter: ProductUserAdapter

    // List Master (Menyimpan semua data asli)
    private val allProducts = mutableListOf<Product>()

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
        setupSearch() // Inisialisasi Search
        fetchProducts()
    }

    private fun setupRecyclerView() {
        // Inisialisasi dengan list kosong
        productAdapter = ProductUserAdapter(mutableListOf())

        binding.rvUserProducts.apply {
            // Saran: Gunakan GridLayoutManager (2 kolom) agar lebih menarik untuk user
            layoutManager = GridLayoutManager(context, 2)
            // Jika ingin list biasa: layoutManager = LinearLayoutManager(context)
            adapter = productAdapter
        }
    }

    // === LOGIKA PENCARIAN ===
    private fun setupSearch() {
        binding.searchViewUser.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterProducts(newText)
                return true
            }
        })
    }

    private fun filterProducts(query: String?) {
        if (query != null) {
            val filteredList = ArrayList<Product>()
            val searchQuery = query.lowercase(Locale.getDefault())

            for (product in allProducts) {
                if (product.name.lowercase(Locale.getDefault()).contains(searchQuery)) {
                    filteredList.add(product)
                }
            }

            // Tampilkan/Sembunyikan pesan kosong
            if (filteredList.isEmpty()) {
                binding.tvEmptySearch.visibility = View.VISIBLE
                binding.rvUserProducts.visibility = View.GONE
            } else {
                binding.tvEmptySearch.visibility = View.GONE
                binding.rvUserProducts.visibility = View.VISIBLE
                productAdapter.updateList(filteredList)
            }
        } else {
            // Jika query kosong, tampilkan semua
            binding.tvEmptySearch.visibility = View.GONE
            binding.rvUserProducts.visibility = View.VISIBLE
            productAdapter.updateList(allProducts)
        }
    }

    private fun fetchProducts() {
        Log.d(TAG, "Mulai mengambil data dari Firestore...")
        binding.progressBar.visibility = View.VISIBLE
        binding.rvUserProducts.visibility = View.GONE

        db.collection("products")
            .get()
            .addOnSuccessListener { result ->
                Log.d(TAG, "Sukses! Ditemukan ${result.size()} dokumen.")
                binding.progressBar.visibility = View.GONE

                if (!result.isEmpty) {
                    allProducts.clear() // Reset Master Data

                    for (document in result) {
                        try {
                            val product = document.toObject(Product::class.java)
                            product.id = document.id
                            allProducts.add(product)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error converting document ${document.id}", e)
                        }
                    }

                    // Tampilkan semua data awal
                    productAdapter.updateList(allProducts)
                    binding.rvUserProducts.visibility = View.VISIBLE
                } else {
                    Toast.makeText(context, "Belum ada produk.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
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