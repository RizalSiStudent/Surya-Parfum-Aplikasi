package com.surya.parfum

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.surya.parfum.databinding.FragmentAdminOrdersParentBinding

class AdminOrdersParentFragment : Fragment() {

    private var _binding: FragmentAdminOrdersParentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminOrdersParentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = OrdersPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // === ATUR JUDUL UNTUK 3 TAB ===
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Pesanan Baru"    // Status: Diproses
                1 -> tab.text = "Sedang Dikemas"  // Status: Disetujui
                2 -> tab.text = "Riwayat"         // Status: Selesai/Ditolak
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class OrdersPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        // Ubah jumlah item menjadi 3
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            // Kirim tipe filter yang berbeda untuk setiap tab
            return when (position) {
                0 -> AdminOrderListFragment.newInstance("new")      // Tab 1
                1 -> AdminOrderListFragment.newInstance("packing")  // Tab 2
                2 -> AdminOrderListFragment.newInstance("history")  // Tab 3
                else -> AdminOrderListFragment.newInstance("new")
            }
        }
    }
}