package com.example.polito_mad_01.adapters

import androidx.fragment.app.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.polito_mad_01.ui.*
import com.example.polito_mad_01.viewmodel.EditProfileViewModel

class ShowProfilePageAdapter(
    fragmentActivity: FragmentActivity,
    private val vm: EditProfileViewModel
    ): FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> EditProfile()
            1 -> EditSkill()
            else -> throw (Exception("Invalid position"))}
    }

}