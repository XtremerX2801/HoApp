package com.trading.thesis_trading_app.ui.User

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trading.thesis_trading_app.R
import com.trading.thesis_trading_app.utils.Constants
import com.trading.thesis_trading_app.utils.GlideLoader
import com.google.firebase.firestore.FirebaseFirestore
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class RankingActivity : AppCompatActivity() {

    private var adapter: RankingUsersAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        rankingUser()
    }

    private fun rankingUser() {
        FirebaseFirestore.getInstance().collection(Constants.TOTAL_POINT).document("5Tw5en3twZNtk7tk5bKD").get().addOnSuccessListener {
            val point = it.data?.get(Constants.POINTS_LIST) as MutableList<Long>
            val users = it.data?.get(Constants.USERS_LIST) as MutableList<String>

            val indexList: MutableList<Int> = mutableListOf()
            for (i in 0 until point.size) {
                indexList.add(i)
            }
            var best = 0
            var sub: Int

            for (a in 0 until indexList.size - 1) {
                for (b in 0 until indexList.size - 1) {
                    best = if (point[b] < point[b + 1]) {
                        b + 1
                    } else {
                        b
                    }
                }
                if (best != a) {
                    sub = indexList[a]
                    indexList[a] = indexList[best]
                    indexList[best] = sub
                }
                best = a + 1
            }

            val arrangedUsers: MutableList<String> = mutableListOf()
            val arrangedPoints: MutableList<Long> = mutableListOf()
            for (index in 0 until indexList.size) {
                arrangedUsers.add(users[indexList[index]])
                arrangedPoints.add(point[indexList[index]])
            }

            val productRecyclerView = findViewById<View>(R.id.ranking_recyclerView) as RecyclerView
            adapter = RankingUsersAdapter(arrangedUsers, arrangedPoints)
            productRecyclerView.adapter = adapter
            productRecyclerView.layoutManager = LinearLayoutManager(this)
            productRecyclerView.setHasFixedSize(true)
            productRecyclerView.itemAnimator = SlideInUpAnimator()
        }
    }

    private inner class RankingUsersAdapter(private val users_list: List<String>, private val points_list: List<Long>) : RecyclerView.Adapter<RankingUsersAdapter.ViewHolder>(){
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val userName: TextView = view.findViewById(R.id.user_name)
            val userRank: TextView = view.findViewById(R.id.user_rank)
            val userPoint: TextView = view.findViewById(R.id.user_point)
            val userAvatar: ImageView = view.findViewById(R.id.user_avatar)
        }

        override fun onBindViewHolder(productViewHolder: RankingUsersAdapter.ViewHolder, position: Int) {
            productViewHolder.userName.text = users_list[position]
            productViewHolder.userRank.text = (position + 1).toString()
            productViewHolder.userPoint.text = points_list[position].toString()
            FirebaseFirestore.getInstance().collection(Constants.USERS).whereEqualTo(Constants.USER_ID, users_list[position]).get().addOnSuccessListener {
                for (document in it) {
                    val lastName = document.data[Constants.USER_LAST_NAME] as String
                    val firstName = document.data[Constants.USER_FIRST_NAME] as String
                    val avatar = document.data[Constants.USER_IMAGE] as String

                    productViewHolder.userName.text = "$lastName $firstName"
                    if (position < 3) {
                        productViewHolder.userName.setTextColor(getColor(R.color.viewfinder_laser))
                    }
                    GlideLoader(this@RankingActivity).loadUserPicture(avatar, productViewHolder.userAvatar)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingUsersAdapter.ViewHolder {
            val view = LayoutInflater.from(this@RankingActivity).inflate(
                R.layout.item_user_ranking,
                parent,
                false
            )
            return ViewHolder(view)
        }

        override fun getItemCount() = users_list.size
    }

}