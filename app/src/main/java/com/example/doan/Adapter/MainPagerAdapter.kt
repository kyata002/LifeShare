// MainPagerAdapter.kt
package com.example.doan.Adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.doan.ui.fragment.FileCommunityFragment
import com.example.doan.ui.fragment.FileDeviceFragment
import com.example.doan.ui.fragment.FileShareFragment

class MainPagerAdapter(
    activity: AppCompatActivity,
    private val onRequestLogin: () -> Unit // Callback để yêu cầu đăng nhập
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FileDeviceFragment()
            1 -> {
                onRequestLogin() // Kiểm tra và yêu cầu đăng nhập nếu cần
                FileCommunityFragment()
            }
            else -> {
//                onRequestLogin() // Kiểm tra và yêu cầu đăng nhập nếu cần
                FileShareFragment()
            }
        }
    }
}
