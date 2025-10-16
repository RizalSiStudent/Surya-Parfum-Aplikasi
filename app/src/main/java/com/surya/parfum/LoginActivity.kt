package com.surya.parfum

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.surya.parfum.databinding.ActivityLoginBinding
// Import yang tidak perlu dihapus agar lebih bersih

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Listener untuk tombol Login
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            checkUserRole(email)
                        } else {
                            Toast.makeText(this, "Login Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Email dan Password tidak boleh kosong.", Toast.LENGTH_SHORT).show()
            }
        }

        // --- LISTENER UNTUK TEKS REGISTRASI SEKARANG DI SINI ---
        binding.tvGoToRegister.setOnClickListener {
            // Perintah untuk membuka RegisterActivity saat teks diklik
            // Nama class diperbaiki dari Register-Activity menjadi RegisterActivity
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun checkUserRole(email: String) {
        // Ganti email admin sesuai kebutuhan Anda
        if (email.equals("admin@suryaparfum.com", ignoreCase = true)) {
            // Jika Admin, arahkan ke Dashboard Admin
            Toast.makeText(this, "Selamat datang, Admin!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, AdminHomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            // Jika Pembeli, tutup halaman login
            Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()
            finish() // Menutup LoginActivity akan otomatis kembali ke MainActivity
        }
    }
}