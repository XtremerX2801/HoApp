package com.trading.thesis_trading_app.ui.Admin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.ui.Authentication.BaseActivity
import com.trading.thesis_trading_app.ui.TestOnly.Config5
import com.trading.thesis_trading_app.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : BaseActivity() {

    var txtvalue: EditText? = null
    var btnfetch: Button? = null
    var listview: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtvalue = findViewById<View>(R.id.editText) as EditText
        btnfetch = findViewById<View>(R.id.buttonfetch) as Button
        listview = findViewById<View>(R.id.listView) as ListView
        btnfetch!!.setOnClickListener { data }
    }

    private val data: Unit

        get() {
            val value = txtvalue!!.text.toString().trim { it <= ' ' }
            if (value == "") {
                Toast.makeText(this, "Please Enter Data Value", Toast.LENGTH_LONG).show()
                return
            }
            val url: String = Config5.DATA_URL + txtvalue!!.text.toString().trim { it <= ' ' }
            val stringRequest = StringRequest(url,
                { response -> showJSON(response) }
            ) { error ->
                Toast.makeText(
                    this@MainActivity,
                    error.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
            val requestQueue = Volley.newRequestQueue(this)
            requestQueue.add(stringRequest)
        }

    private fun showJSON(response: String) {
        try {
            val jsonObject = JSONObject(response)
            val result = jsonObject.getJSONArray(Config5.JSON_ARRAY)
            for (i in 0 until result.length()) {
                var barcode = ""
                val jo = result.getJSONObject(i)
                val product_name = jo.getString("product_name")
                val bar_code = jo.getString("bar_code")
                if (bar_code == "combo-muasachtangqua") {
                    barcode = "0"
                } else {
                    bar_code.split(" ")
                    for (element in bar_code) {
                        barcode += element
                    }
                }
                val date = jo.getString("date")
                val id = jo.getString("id")
                val category_id = jo.getString("category_id")
                val brand = jo.getString("brand")
                val made_in =jo.getString("made_in")
                val image = jo.getString("image_url")
                val listImageWord = image.split("/")

                FirebaseStorage.getInstance().reference.child(Constants.PRODUCTS).listAll().addOnSuccessListener { result1 ->
                    result1.items.forEachIndexed{ index, item ->
                        item.downloadUrl.addOnSuccessListener {

                            Log.e("Image", it.toString())
                            Log.e("SubString", "products%2F" + listImageWord[1])
                            if (it.toString().contains("products%2F" + listImageWord[1], false)) {
                                Log.e("ImageTrue", it.toString())
                                val dataImage = it.toString()
                                uploadProduct(product_name, barcode, date, id, brand, made_in, category_id, dataImage)
                            }

                        }

                    }
                }

            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun generateSearchKeywords(inputText: String): List<String> {
        val inputString = inputText.toLowerCase(Locale.ROOT)
        val keywords = mutableListOf<String>()

        val words = inputString.split(" ")

        var appendString = ""
        val length = words.size

        for (index in 0 until length) {
            for (i in index until length) {
                appendString += if (appendString == "") {
                    words[i]
                } else {
                    " " + words[i]
                }
                keywords.add(appendString)
            }
            appendString = ""
        }

        return keywords
    }

    private fun uploadProduct(product_name: String, barcode: String, date: String, id: String, brand: String, made_in: String, category_id: String, dataImage: String) {

        val list = ArrayList<HashMap<String, Any?>>()

        val employees = HashMap<String, Any?>()
        employees["product_name"] = product_name
        employees["product_barcode"] = barcode.toLong()
        employees["product_created_at"] = date
        employees["product_id"] = id.toInt()
        employees["product_amount"] = 100
        employees["product_brand"] = brand
        employees["product_made_in"] = made_in
        employees["product_price"] = 1000
        employees["product_image"] = dataImage
        employees["comment"] = mutableListOf<String>()
        employees["comment_user"] = mutableListOf<String>()
        employees["product_category"] = when (category_id) {
            "1" -> "ĐỒ UỐNG CÁC LOẠI"
            "2" -> "SỮA UỐNG CÁC LOẠI"
            "3" -> "BÁNH KẸO CÁC LOẠI"
            "4" -> "MÌ, CHÁO, PHỞ, BÚN"
            "5" -> "DẦU ĂN, GIA VỊ"
            "6" -> "GẠO, BỘT, ĐỒ KHÔ"
            "7" -> "ĐỒ MÁT, ĐÔNG LẠNH"
            "8" -> "TẢ, ĐỒ CHO BÉ"
            "9" -> "CHĂM SÓC CÁ NHÂN"
            "10" -> "VỆ SINH NHÀ CỬA"
            "11" -> "ĐỒ DÙNG GIA ĐÌNH"
            else -> "VĂN PHÒNG PHẨM"
        }
        employees["product_search_keyword"] = generateSearchKeywords(product_name)
        list.add(employees)
        for (ii in 0 until list.size) {
            FirebaseFirestore.getInstance().collection(Constants.PRODUCTS).add(list[ii])
        }
    }

}