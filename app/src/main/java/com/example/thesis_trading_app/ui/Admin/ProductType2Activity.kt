package com.example.thesis_trading_app.ui.Admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.thesis_trading_app.R
import com.example.thesis_trading_app.utils.Constants
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_product_type2.*

class ProductType2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_type2)
    }

    private fun setBarChartValue() {
        val xValues3 = ArrayList<String>()
        xValues3.add("ĐỒ MÁT, ĐÔNG LẠNH")
        xValues3.add("TẢ, ĐỒ CHO BÉ")
        xValues3.add("CHĂM SÓC CÁ NHÂN")

        val xValues4 = ArrayList<String>()
        xValues4.add("VỆ SINH NHÀ CỬA")
        xValues4.add("ĐỒ DÙNG GIA ĐÌNH")
        xValues4.add("VĂN PHÒNG PHẨM")

        FirebaseFirestore.getInstance().collection(Constants.SALES).whereEqualTo(Constants.SALEID, "1").get().addOnSuccessListener {
            for (document in it) {
                val yValues = document.data[Constants.SALES_PRODUCT_CATEGORY_AMOUNT] as MutableList<Int>

                val yValues3 : MutableList<Float> = mutableListOf()
                val yValues4 : MutableList<Float> = mutableListOf()

                yValues3.add(yValues[6].toFloat())
                yValues3.add(yValues[7].toFloat())
                yValues3.add(yValues[8].toFloat())

                yValues4.add(yValues[9].toFloat())
                yValues4.add(yValues[10].toFloat())
                yValues4.add(yValues[11].toFloat())

                val barEntries3 = ArrayList<BarEntry>()
                val barEntries4 = ArrayList<BarEntry>()

                for (i in 0 until 3) {
                    barEntries3.add(BarEntry(yValues3[i], i))
                    barEntries4.add(BarEntry(yValues4[i], i))
                }

                val barDataSet3 = BarDataSet(barEntries3, "Sales Category2")
                val barDataSet4 = BarDataSet(barEntries4, "Sales Category2")

                barDataSet3.color = resources.getColor(R.color.colorSnackBarSuccess)
                barDataSet4.color = resources.getColor(R.color.colorSnackBarSuccess)

                val data3 = BarData(xValues3, barDataSet3)
                val data4 = BarData(xValues4, barDataSet4)

                sales_barChart3.data = data3
                sales_barChart4.data = data4

                sales_barChart3.setBackgroundColor(resources.getColor(R.color.colorThemeLightBlue))
                sales_barChart3.animateXY(3000, 3000)
                sales_barChart4.setBackgroundColor(resources.getColor(R.color.colorThemeLightBlue))
                sales_barChart4.animateXY(3000, 3000)
            }
        }
    }
}