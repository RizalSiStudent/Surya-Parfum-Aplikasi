package com.surya.parfum

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import com.google.firebase.auth.FirebaseAuth
import com.surya.parfum.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // ===== PEMERIKSA SESI DITAMBAHKAN DI SINI =====
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.email.equals("admin@suryaparfum.com", ignoreCase = true)) {
            // Jika pengguna saat ini adalah admin, langsung alihkan ke dasbor admin
            goToAdminDashboard()
            // Penting: Hentikan eksekusi sisa kode di onCreate
            return
        }

        // Kode di bawah ini hanya akan berjalan jika pengguna BUKAN admin
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProductListFragment())
                .commit()
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_cart -> {
                    if (auth.currentUser != null) {
                        startActivity(Intent(this, CartActivity::class.java))
                    } else {
                        Toast.makeText(this, "Silakan login untuk melihat keranjang", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    true
                }
                R.id.action_profile -> {
                    if (auth.currentUser != null) {
                        showPopupMenu()
                    } else {
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun showPopupMenu() {
        // Find the view of the profile icon to use as an "anchor" for the popup
        val anchorView = findViewById<View>(R.id.action_profile)

        // Create the PopupMenu
        val popup = PopupMenu(this, anchorView)
        popup.menuInflater.inflate(R.menu.profile_popup_menu, popup.menu)

        // Add a listener for the items inside the popup
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                // THIS IS THE NEW LOGIC FOR THE "ORDER HISTORY" ITEM
                R.id.action_popup_history -> {
                    startActivity(Intent(this, OrderHistoryActivity::class.java))
                    true
                }
                R.id.action_popup_logout -> {
                    // The existing logout logic
                    auth.signOut()
                    Toast.makeText(this, "Anda telah logout", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        // Show the popup
        popup.show()
    }

    // ===== FUNGSI BARU UNTUK MENGALIHKAN ADMIN =====
    private fun goToAdminDashboard() {
        val intent = Intent(this, AdminHomeActivity::class.java)
        // Hapus semua activity sebelumnya dari tumpukan
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        // Tutup MainActivity agar tidak bisa diakses dengan tombol kembali
        finish()
    }
}