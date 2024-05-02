package com.phase.capstone.recyclerview

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.phase.capstone.R
import com.phase.capstone.main.home.HomeMainDirections
import com.phase.capstone.repo.UserState

class UserListRecyclerViewAdapter(
    private var userList: List<UserState>,
    val activity: Activity
) : RecyclerView.Adapter<UserListRecyclerViewAdapter.UserListViewHolder>() {
    inner class UserListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return UserListViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
        holder.itemView.apply {
            val userState = userList[position]
            val tvUserNickname = findViewById<TextView>(R.id.tvUserNickname)
            val tvUserIsGuardian = findViewById<TextView>(R.id.tvUserIsGuardian)

            val cardBody = findViewById<CardView>(R.id.cardBody)

            tvUserNickname.text = userState.nickname
            if (userState.isGuardian) {
                tvUserIsGuardian.text = "Guardian"
            } else {
                tvUserIsGuardian.text = "Dependent"
            }

            cardBody.setOnClickListener {
                val action = HomeMainDirections.actionHomeMainToNavigationProfile(
                    userState.userId,
                    userState.nickname
                )
                Navigation.findNavController(holder.itemView).navigate(action)
            }
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}