package com.example.rathings

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

class CapitalizedTextView(context: Context, attrs: AttributeSet) : TextView(context, attrs) {

    override fun setText(text: CharSequence, type: TextView.BufferType) {
        var text = text
        if (text.length > 0) {
            text = text[0].toString().toUpperCase() + text.subSequence(1, text.length)
        }
        super.setText(text, type)
    }
}