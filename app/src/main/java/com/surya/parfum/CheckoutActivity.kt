package com.surya.parfum

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.surya.parfum.databinding.ActivityCheckoutBinding
import java.util.*

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var totalAmount: Long = 0
    // Mengganti nama variabel agar lebih jelas
    private val selectedItems = mutableListOf<CartItem>()
    private var customerLocation: GeoPoint? = null
    private val LOCATION_PERMISSION_REQ_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // === PERUBAHAN UTAMA 1: Menerima data dari Intent ===
        totalAmount = intent.getLongExtra("TOTAL_AMOUNT", 0)
        // Terima daftar item yang terpilih
        intent.getParcelableArrayListExtra<CartItem>("SELECTED_ITEMS")?.let {
            selectedItems.addAll(it)
        }

        binding.tvTotalAmount.text = "Total: Rp $totalAmount"
        binding.topAppBar.setNavigationOnClickListener { finish() }

        setupListeners()
    }

    // Fungsi setupListeners, getCurrentLocation, dan onRequestPermissionsResult tidak berubah
    private fun setupListeners() {
        // ... (kode ini tetap sama)
        binding.rgMetode.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbAntar) {
                binding.layoutPengiriman.visibility = View.VISIBLE
                binding.btnViewStoreLocation.visibility = View.GONE
            } else {
                binding.layoutPengiriman.visibility = View.GONE
                binding.btnViewStoreLocation.visibility = View.VISIBLE
            }
        }
        binding.btnViewStoreLocation.setOnClickListener {
            val tokoLatitude = -7.827650797282661
            val tokoLongitude = 112.03274612688416
            val tokoLabel = "Toko Surya Parfum"
            val gmmIntentUri = Uri.parse("geo:$tokoLatitude,$tokoLongitude?q=$tokoLatitude,$tokoLongitude($tokoLabel)")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                Toast.makeText(this, "Membuka di browser...", Toast.LENGTH_SHORT).show()
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/"))
                startActivity(webIntent)
            }
        }
        binding.btnGetLocation.setOnClickListener {
            getCurrentLocation()
        }
        binding.btnPlaceOrder.setOnClickListener {
            placeOrder()
        }
    }
    private fun getCurrentLocation() {
        // ... (kode ini tetap sama)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQ_CODE)
            return
        }
        LocationServices.getFusedLocationProviderClient(this).lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    customerLocation = GeoPoint(location.latitude, location.longitude)
                    try {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (addresses != null && addresses.isNotEmpty()) {
                            val address = addresses[0].getAddressLine(0)
                            binding.etAddress.setText(address)
                            Toast.makeText(this, "Lokasi ditemukan!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Gagal mengubah koordinat menjadi alamat", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Gagal mendapatkan lokasi. Pastikan GPS aktif.", Toast.LENGTH_SHORT).show()
                }
            }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // ... (kode ini tetap sama)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQ_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // =======================================================

    private fun placeOrder() {
        val name = binding.etName.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        val fulfillmentMethod = if (binding.rbAmbilDiToko.isChecked) "Ambil di Toko" else "Antar ke Alamat"

        if (name.isEmpty()) {
            Toast.makeText(this, "Nama penerima wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }
        if (fulfillmentMethod == "Antar ke Alamat" && (address.isEmpty() || phone.isEmpty())) {
            Toast.makeText(this, "Alamat dan nomor telepon wajib diisi untuk pengantaran", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser ?: return

        binding.btnPlaceOrder.isEnabled = false
        binding.btnPlaceOrder.text = "Memproses Pesanan..."

        // === PERUBAHAN UTAMA 2: Tidak lagi mengambil data dari Firestore ===
        // Langsung proses 'selectedItems' yang diterima dari Intent
        val itemsForOrder = mutableListOf<HashMap<String, Any>>()
        for (cartItem in selectedItems) {
            itemsForOrder.add(hashMapOf(
                "productName" to cartItem.productName,
                "selectedSize" to cartItem.selectedSize,
                "totalPrice" to cartItem.totalPrice
            ))
        }

        val orderData = hashMapOf(
            "userId" to currentUser.uid,
            "customerName" to name,
            "address" to address,
            "phone" to phone,
            "totalAmount" to totalAmount,
            "items" to itemsForOrder,
            "orderDate" to FieldValue.serverTimestamp(),
            "status" to "Diproses",
            "fulfillmentMethod" to fulfillmentMethod,
            "customerLocation" to if (fulfillmentMethod == "Antar ke Alamat") customerLocation else null
        )

        db.collection("orders").add(orderData)
            .addOnSuccessListener { clearCart() }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal membuat pesanan", Toast.LENGTH_SHORT).show()
                binding.btnPlaceOrder.isEnabled = true
                binding.btnPlaceOrder.text = "Pesan Sekarang"
            }
    }

    private fun clearCart() {
        val batch = db.batch()
        // === PERUBAHAN UTAMA 3: Hanya menghapus item yang terpilih ===
        for (item in selectedItems) {
            val docRef = db.collection("carts").document(item.id)
            batch.delete(docRef)
        }

        batch.commit().addOnSuccessListener {
            Toast.makeText(this, "Pesanan berhasil dibuat!", Toast.LENGTH_LONG).show()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal mengosongkan keranjang", Toast.LENGTH_SHORT).show()
        }
    }
}