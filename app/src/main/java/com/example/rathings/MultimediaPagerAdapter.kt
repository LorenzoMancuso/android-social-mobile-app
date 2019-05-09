package com.example.rathings

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

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