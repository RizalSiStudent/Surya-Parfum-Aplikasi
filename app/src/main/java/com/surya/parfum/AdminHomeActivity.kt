package com.surya.parfum

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.surya.parfum.databinding.ActivityAdminHomeBinding

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminHomeBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Penting: Jadikan Toolbar sebagai Action Bar
        setSupportActionBar(binding.topAppBar)

        auth = FirebaseAuth.getInstance()

        // Tampilkan fragment awal (Dashboard)
        if (savedInstanceState == null) {
            loadFragment(AdminDashboardFragment())
        }

        // Listener untuk navigasi bawah (tidak berubah)
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_admin_dashboard -> {
                    loadFragment(AdminDashboardFragment())
                    true
                }
                R.id.nav_admin_orders -> {
                    loadFragment(AdminOrderListFragment())
                    true
                }
                else -> false
            }
        }
    }

    // ===== 1. FUNGSI UNTUK MEMBUAT DAN MEMODIFIKASI MENU =====
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // 'Inflate' atau pasang layout menu kita ke toolbar
        menuInflater.inflate(R.menu.main_menu, menu)

        // Cari item keranjang dan sembunyikan
        val cartItem = menu?.findItem(R.id.action_cart)
        cartItem?.isVisible = false

        return true
    }

    // ===== 2. FUNGSI UNTUK MENANGANI KLIK PADA ITEM MENU =====
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                // Tampilkan popup menu saat ikon profil diklik
                showPopupMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.admin_fragment_container, fragment)
            .commit()
    }

    private fun showPopupMenu() {
        // Kita butuh 'anchor' untuk popup, kita bisa gunakan view dari toolbar
        val anchorView = findViewById<View>(R.id.action_profile)
        val popup = PopupMenu(this, anchorView)
        popup.menuInflater.inflate(R.menu.profile_popup_menu, popup.menu)

        popup.menu.findItem(R.id.action_popup_history)?.isVisible = false

        popup.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_popup_logout) {
                auth.signOut()
                Toast.makeText(this, "Admin telah logout", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

                true
            } else {
                false
            }
        }
        popup.show()
    }
}