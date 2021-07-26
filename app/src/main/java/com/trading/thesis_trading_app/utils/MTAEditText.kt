package com.trading.thesis_trading_app.utils

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class MTAEditText(context: Context, attrs: AttributeSet): AppCompatEditText(context,attrs) {

    init {
        // Call the function to apply the font to the components
        applyFont()
    }

    private fun applyFont(){
        // Use to get file from assets folder and set it to the textView
        val typeFace: Typeface = Typeface.createFromAsset(context.assets, "Montserrat-Regular.ttf")
        typeface = typeFace
    }

}