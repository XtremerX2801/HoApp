package com.trading.thesis_trading_app.ui.User

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.firestore.FirestoreClass
import com.trading.thesis_trading_app.ui.User.fragments.CustomDialogFragment
import com.trading.thesis_trading_app.utils.Constants
import com.trading.thesis_trading_app.utils.GlideLoader
import com.google.firebase.firestore.FirebaseFirestore
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.activity_product_info.*
import kotlinx.android.synthetic.main.activity_user_profile.*

class ProductInfoActivity : AppCompatActivity(), View.OnClickListener {

    private val userId: String = FirestoreClass().getCurrentUserId()
    private var adapter: CommentAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_info)

        if (intent.hasExtra(Constants.PRODUCT_BARCODE)) {
            val barcode = intent.getLongExtra(Constants.PRODUCT_BARCODE, 0)

            showProduct(barcode)

            val commentRecyclerView = findViewById<View>(R.id.comment_recyclerView) as RecyclerView

            FirebaseFirestore.getInstance().collection(Constants.PRODUCTS).whereEqualTo(Constants.PRODUCT_BARCODE, barcode).get().addOnSuccessListener {
                for (document in it) {
                    val comment = document.data["comment"] as MutableList<String>
                    val commentUser = document.data["comment_user"] as MutableList<String>

                    adapter = CommentAdapter(comment, commentUser)
                    commentRecyclerView.adapter = adapter
                    commentRecyclerView.layoutManager = LinearLayoutManager(this)
                    commentRecyclerView.setHasFixedSize(true)
                    commentRecyclerView.itemAnimator = SlideInUpAnimator()
                }
            }
        }

        product_info_location_list.setOnClickListener(this@ProductInfoActivity)
        product_info_summit_comment.setOnClickListener(this@ProductInfoActivity)
        product_info_purchase.setOnClickListener(this@ProductInfoActivity)
    }

    private fun showProduct(barcode: Long) {
        FirebaseFirestore.getInstance().collection(Constants.PRODUCTS).whereEqualTo(Constants.PRODUCT_BARCODE, barcode).get().addOnSuccessListener {
            for (document in it) {
                val proName = document.data[Constants.PRODUCT_NAME] as String
                val proCategory = document.data[Constants.PRODUCT_CATEGORY] as String
                val proPrice = document.data[Constants.PRODUCT_PRICE] as Long
                val proAmount = document.data[Constants.PRODUCT_AMOUNT] as Long
                val proImage = document.data[Constants.PRODUCT_IMAGE] as String
                val proBrand = document.data[Constants.PRODUCT_BRAND] as String
                val proMadeIn = document.data[Constants.PRODUCT_MADE_IN] as String

                product_info_name.text = "Tên sản phẩm: " + proName
                product_info_category.text = "Loại hàng: " + proCategory
                product_info_price.text = "Giá sản phẩm: " + proPrice.toString()
                product_info_amount.text = "Số lượng trong kho: " + proAmount.toString()
                product_info_brand.text = "Thương hiệu: " + proBrand
                product_info_made_in.text = "Sản xuất ở: " + proMadeIn
                val productAvatar = findViewById<ImageView>(R.id.product_info_image)

                GlideLoader(this@ProductInfoActivity).loadProductPicture(proImage, productAvatar)
            }
        }
    }

    private inner class CommentAdapter(private val comment: MutableList<String>, private val comment_user: MutableList<String>) : RecyclerView.Adapter<CommentAdapter.ViewHolder>(){
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val commentAvatar: ImageView = view.findViewById(R.id.comment_avatar)
            val commentFirstname: TextView = view.findViewById(R.id.comment_firstname)
            val commentLastname: TextView = view.findViewById(R.id.comment_lastname)
            val userComment: TextView = view.findViewById(R.id.user_comment)
        }

        override fun onBindViewHolder(commentViewHolder: ViewHolder, position: Int) {
            commentViewHolder.userComment.text = comment[position]
            FirebaseFirestore.getInstance().collection(Constants.USERS).whereEqualTo(Constants.USER_ID, comment_user[position]).get().addOnSuccessListener {
                for (document in it) {
                    val firstName = document.data[Constants.USER_FIRST_NAME] as String
                    val lastName = document.data[Constants.USER_LAST_NAME] as String
                    val userAvatar = document.data[Constants.USER_IMAGE] as String

                    commentViewHolder.commentFirstname.text = "$firstName"
                    commentViewHolder.commentLastname.text = "$lastName"
                    GlideLoader(this@ProductInfoActivity).loadUserPicture(userAvatar, commentViewHolder.commentAvatar)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(this@ProductInfoActivity).inflate(
                R.layout.item_comment,
                parent,
                false
            )
            return ViewHolder(view)
        }

        override fun getItemCount() = comment.size
    }

    private fun refreshPage() {
        val barcode = intent.getLongExtra(Constants.PRODUCT_BARCODE, 0)
        val intent = Intent(this, ProductInfoActivity::class.java)
        intent.putExtra(Constants.PRODUCT_BARCODE, barcode)
        startActivity(intent)
        finish()
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.product_info_location_list -> {
                    val dialog = CustomDialogFragment()
                    dialog.show(supportFragmentManager, "Custom Dialog Location")
                }
                R.id.product_info_summit_comment -> {
                    val comment = product_info_enter_comment.text.toString().trim { it <= ' ' }
                    if (intent.hasExtra(Constants.PRODUCT_BARCODE)) {
                        val barcode = intent.getLongExtra(Constants.PRODUCT_BARCODE, 0)
                        if (comment.isNotEmpty()) {
                            FirebaseFirestore.getInstance().collection(Constants.PRODUCTS).whereEqualTo(Constants.PRODUCT_BARCODE, barcode).get().addOnSuccessListener {
                                for (document in it) {
                                    val commentList = document.data["comment"] as MutableList<String>
                                    val commentUserList = document.data["comment_user"] as MutableList<String>

                                    commentList.add(comment)
                                    commentUserList.add(userId)

                                    val commentHashMap = HashMap<String, Any>()
                                    commentHashMap["comment"] = commentList
                                    commentHashMap["comment_user"] = commentUserList

                                    FirebaseFirestore.getInstance().collection(Constants.PRODUCTS).document(document.id).update(commentHashMap).addOnSuccessListener {
                                        Log.e("Comment updated", "Successful")
                                    }.addOnFailureListener {
                                        Log.e("Comment updated", "Failed")
                                    }

                                    refreshPage()
                                }
                            }
                        }
                    }
                }
                R.id.product_info_purchase -> {
                    val barcode = intent.getLongExtra(Constants.PRODUCT_BARCODE, 0)
                    FirebaseFirestore.getInstance().collection(Constants.PRODUCTS).whereEqualTo(Constants.PRODUCT_BARCODE, barcode).get().addOnSuccessListener { it0 ->
                        for (document0 in it0) {
                            val proPrice = document0.data[Constants.PRODUCT_PRICE] as Long

                            FirebaseFirestore.getInstance().collection(Constants.CARTS).whereEqualTo(Constants.USER_ID, userId).get().addOnSuccessListener {
                                for (document in it) {
                                    val cartProNum = document.data[Constants.CART_PRODUCT_NUM] as Long
                                    val cartPrice = document.data[Constants.CART_PRICE] as Long
                                    val cartProducts = document.data[Constants.CART_PRODUCTS] as MutableList<Long>
                                    val cartProductAmount = document.data[Constants.CART_PRODUCT_AMOUNT] as MutableList<Int>

                                    var same = 0
                                    val amount = 1
                                    if (cartProducts.isEmpty()) {
                                        cartProductAmount.add(amount)
                                        cartProducts.add(barcode)
                                    } else {
                                        for (i in 0 until cartProducts.size) {
                                            if (cartProducts[i] == barcode) {
                                                cartProductAmount[i] = cartProductAmount[i] + 1
                                                same = 1
                                            }
                                        }
                                        if (same == 0) {
                                            cartProductAmount.add(amount)
                                            cartProducts.add(barcode)
                                        }
                                    }

                                    val cartHashMap = HashMap<String, Any>()
                                    cartHashMap[Constants.CART_PRODUCT_NUM] = cartProNum + 1
                                    cartHashMap[Constants.CART_PRICE] = cartPrice + proPrice
                                    cartHashMap[Constants.CART_PRODUCTS] = cartProducts
                                    cartHashMap[Constants.CART_PRODUCT_AMOUNT] = cartProductAmount

                                    FirebaseFirestore.getInstance().collection(Constants.CARTS).document(document.id).update(cartHashMap).addOnSuccessListener {
                                        Toast.makeText(this, "Đã thêm sản phẩm thành công", Toast.LENGTH_SHORT).show()
                                    }.addOnFailureListener {
                                        Toast.makeText(this, "Không thêm được sản phẩm", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}