package com.trading.thesis_trading_app.ui.Admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.utils.Constants
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_product_type1.*

class ProductType1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_type1)

        setBarChartValue()
    }

    private fun setBarChartValue() {
        val xValues1 = ArrayList<String>()
        xValues1.add("ĐỒ UỐNG CÁC LOẠI")
        xValues1.add("SỮA UỐNG CÁC LOẠI")
        xValues1.add("BÁNH KẸO CÁC LOẠI")

        val xValues2 = ArrayList<String>()
        xValues2.add("MÌ, CHÁO, PHỞ, BÚN")
        xValues2.add("DẦU ĂN, GIA VỊ")
        xValues2.add("GẠO, BỘT, ĐỒ KHÔ")

        FirebaseFirestore.getInstance().collection(Constants.SALES).whereEqualTo(Constants.SALEID, "1").get().addOnSuccessListener {
            for (document in it) {
                val yValues = document.data[Constants.SALES_PRODUCT_CATEGORY_AMOUNT] as MutableList<Int>

                val yValues1 : MutableList<Float> = mutableListOf()
                val yValues2 : MutableList<Float> = mutableListOf()

                yValues1.add(yValues[0].toFloat())
                yValues1.add(yValues[1].toFloat())
                yValues1.add(yValues[2].toFloat())

                yValues2.add(yValues[3].toFloat())
                yValues2.add(yValues[4].toFloat())
                yValues2.add(yValues[5].toFloat())

                val barEntries1 = ArrayList<BarEntry>()
                val barEntries2 = ArrayList<BarEntry>()

                for (i in 0 until 3) {
                    barEntries1.add(BarEntry(yValues1[i], i))
                    barEntries2.add(BarEntry(yValues2[i], i))
                }

                val barDataSet1 = BarDataSet(barEntries1, "Sales Category1")
                val barDataSet2 = BarDataSet(barEntries2, "Sales Category1")

                barDataSet1.color = resources.getColor(R.color.colorSnackBarSuccess)
                barDataSet2.color = resources.getColor(R.color.colorSnackBarSuccess)

                val data1 = BarData(xValues1, barDataSet1)
                val data2 = BarData(xValues2, barDataSet2)

                sales_barChart1.data = data1
                sales_barChart2.data = data2

                sales_barChart1.setBackgroundColor(resources.getColor(R.color.colorThemeLightBlue))
                sales_barChart1.animateXY(3000, 3000)
                sales_barChart2.setBackgroundColor(resources.getColor(R.color.colorThemeLightBlue))
                sales_barChart2.animateXY(3000, 3000)

            }
        }
    }
}