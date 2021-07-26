package com.trading.thesis_trading_app.ui.User.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.ui.User.Payment.QRGeneratorActivity
import com.trading.thesis_trading_app.utils.MapsActivity
import kotlinx.android.synthetic.main.fragment_payment_custom_dialog.*
import kotlinx.android.synthetic.main.fragment_payment_custom_dialog.view.*

class PaymentCustomDialogFragment: DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_payment_custom_dialog, container, false)

        root.view_location.setOnClickListener{
            val selectID = location_radio_group.checkedRadioButtonId
            val radio = root.findViewById<RadioButton>(selectID)
            val locationResult = radio?.text.toString()
            Toast.makeText(context, locationResult, Toast.LENGTH_LONG).show()
            val intent = Intent(activity, MapsActivity::class.java)
            intent.putExtra("Store", locationResult)
            startActivity(intent)
        }

        root.choose_store.setOnClickListener{
            val selectID = location_radio_group.checkedRadioButtonId
            val radio = root.findViewById<RadioButton>(selectID)
            val locationResult = radio?.text.toString()
            Toast.makeText(context, "Bạn đã chọn $locationResult", Toast.LENGTH_LONG).show()
            val intent = Intent(activity, QRGeneratorActivity::class.java)
            var storeId = 0
            storeId = when (locationResult) {
                "Cửa hàng 1" -> 1
                "Cửa hàng 2" -> 2
                "Cửa hàng 3" -> 3
                "Cửa hàng 4" -> 4
                "Cửa hàng 5" -> 5
                else -> 0
            }
            intent.putExtra("Store", storeId)
            startActivity(intent)
            activity?.finish()
        }

        return root
    }

}