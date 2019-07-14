package com.example.rathings

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class MultimediaPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    var NUM_ITEMS = 0
    var ITEMS: MutableList<String> = ArrayList()

    override fun getCount(): Int {
        return NUM_ITEMS
    }

    override fun getItem(position: Int): Fragment {
        val fragment = MultimediaFragment()
        return MultimediaFragment.newInstance(ITEMS[position])
    }
}