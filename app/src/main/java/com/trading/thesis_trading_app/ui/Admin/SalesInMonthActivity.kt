package com.trading.thesis_trading_app.ui.Admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.utils.Constants
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_sales_in_month.*

class SalesInMonthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_in_month)

        setBarChartValue()
    }

    private fun setBarChartValue() {

        FirebaseFirestore.getInstance().collection(Constants.SALES).whereEqualTo(Constants.SALEID, "1").get().addOnSuccessListener {
            for (document in it) {
                val monthValue = document.data[Constants.SALES_TIME] as MutableList<String>
                dashboard_year.text = monthValue[0].split("/")[1]

                val xValues5 = ArrayList<String>()
                for (index in 0 until monthValue.size) {
                    xValues5.add(monthValue[index].split("/")[0])
                }

                val yValues = document.data[Constants.SALES_TIME_AMOUNT] as MutableList<Int>

                val yValues5 : MutableList<Float> = mutableListOf()
                for (xIndex in 0 until xValues5.size) {
                    yValues5.add(yValues[xIndex].toFloat())
                }

                val barEntries5 = ArrayList<BarEntry>()

                for (i in 0 until xValues5.size) {
                    barEntries5.add(BarEntry(yValues5[i], i))
                }

                val barDataSet5 = BarDataSet(barEntries5, "Sales In 2021")

                barDataSet5.color = resources.getColor(R.color.colorSnackBarSuccess)

                val data5 = BarData(xValues5, barDataSet5)

                sales_barChart5.data = data5

                sales_barChart5.setBackgroundColor(resources.getColor(R.color.colorThemeLightBlue))
                sales_barChart5.animateXY(3000, 3000)
            }
        }
    }
}