package com.surya.parfum

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.surya.parfum.databinding.ActivityAddEditProductBinding
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.util.UUID

class AddEditProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditProductBinding
    private lateinit var db: FirebaseFirestore
    private var productId: String? = null
    private var selectedImageUri: Uri? = null
    private var uploadedImageUrl: String? = null

    // Variabel baru untuk URI kamera
    private var cameraImageUri: Uri? = null

    // Launcher untuk mengambil gambar dari Galeri
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivProductPreview.setImageURI(it)
        }
    }

    // Launcher baru untuk mengambil gambar dari Kamera
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            // Jika foto berhasil diambil, gunakan URI yang sudah kita siapkan
            selectedImageUri = cameraImageUri
            binding.ivProductPreview.setImageURI(selectedImageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        db = FirebaseFirestore.getInstance()
        productId = intent.getStringExtra("PRODUCT_ID")

        if (productId != null) {
            binding.topAppBar.title = "Edit Produk"
            loadProductData(productId!!)
        } else {
            binding.topAppBar.title = "Tambah Produk Baru"
        }

        binding.btnSelectImage.setOnClickListener {
            // Tampilkan dialog pilihan Kamera atau Galeri
            showImageSourceDialog()
        }

        binding.btnSaveProduct.setOnClickListener {
            if (selectedImageUri != null) {
                uploadImageAndSaveProduct()
            } else {
                saveProductData()
            }
        }
    }

    // Fungsi baru untuk menampilkan dialog pilihan
    private fun showImageSourceDialog() {
        val options = arrayOf("Kamera", "Galeri")
        AlertDialog.Builder(this)
            .setTitle("Pilih Sumber Gambar")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { // Kamera
                        cameraImageUri = createImageUri()
                        // ===== PERBAIKAN DI SINI =====
                        // Gunakan 'let' untuk memastikan cameraImageUri tidak null
                        cameraImageUri?.let { uri ->
                            cameraLauncher.launch(uri)
                        }
                    }
                    1 -> { // Galeri
                        galleryLauncher.launch("image/*")
                    }
                }
            }
            .show()
    }

    // Fungsi baru untuk membuat URI yang aman untuk kamera
    private fun createImageUri(): Uri? {
        val imageFile = File(applicationContext.cacheDir, "camera_photo.jpg")
        return FileProvider.getUriForFile(
            applicationContext,
            "${applicationContext.packageName}.fileprovider",
            imageFile
        )
    }

    private fun uploadImageAndSaveProduct() {
        binding.btnSaveProduct.isEnabled = false
        binding.btnSaveProduct.text = "Mengunggah Gambar..."

        lifecycleScope.launch {
            try {
                val fileName = "${UUID.randomUUID()}.jpg"
                val inputStream: InputStream? = contentResolver.openInputStream(selectedImageUri!!)
                val bytes = inputStream?.readBytes()

                if (bytes != null) {
                    val bucket = SupabaseClient.client.storage.from("Parfum") // Pastikan nama bucket benar
                    bucket.upload(fileName, bytes) {
                        upsert = false
                    }
                    uploadedImageUrl = bucket.publicUrl(fileName)
                    saveProductData()
                } else {
                    throw Exception("Gagal membaca file gambar")
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddEditProductActivity, "Upload Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                binding.btnSaveProduct.isEnabled = true
                binding.btnSaveProduct.text = "Simpan Produk"
            }
        }
    }

    private fun saveProductData() {
        binding.btnSaveProduct.text = "Menyimpan Data..."

        val name = binding.etProductName.text.toString().trim()
        val desc = binding.etProductDesc.text.toString().trim()
        val pricePerMl = binding.etPricePerMl.text.toString().toLongOrNull() ?: 0
        val sizesString = binding.etAvailableSizes.text.toString()
        val availableSizes = sizesString.split(",").mapNotNull { it.trim().toIntOrNull() }
        val stock = binding.etStock.text.toString().toIntOrNull() ?: 0

        val productMap = hashMapOf<String, Any>(
            "name" to name,
            "description" to desc,
            "pricePerMl" to pricePerMl,
            "availableSizes" to availableSizes,
            "stock" to stock
        )

        if (uploadedImageUrl != null) {
            productMap["imageUrl"] = uploadedImageUrl!!
        }

        val task = if (productId != null) {
            db.collection("products").document(productId!!).update(productMap)
        } else {
            db.collection("products").add(productMap)
        }

        task.addOnSuccessListener {
            Toast.makeText(this, "Produk berhasil disimpan!", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
            binding.btnSaveProduct.isEnabled = true
            binding.btnSaveProduct.text = "Simpan Produk"
        }
    }

    private fun loadProductData(id: String) {
        db.collection("products").document(id).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val product = document.toObject(Product::class.java)
                    product?.let {
                        binding.etProductName.setText(it.name)
                        binding.etProductDesc.setText(it.description)
                        binding.etPricePerMl.setText(it.pricePerMl.toString())
                        binding.etAvailableSizes.setText(it.availableSizes.joinToString(","))
                        binding.etStock.setText(it.stock.toString())

                        Glide.with(this)
                            .load(it.imageUrl)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .error(R.drawable.ic_image_placeholder)
                            .into(binding.ivProductPreview)
                    }
                }
            }
    }
}