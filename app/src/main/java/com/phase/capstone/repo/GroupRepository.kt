package com.phase.capstone.repo

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.phase.capstone.R
import com.phase.capstone.viewmodels.ValidationHandler
import kotlinx.coroutines.tasks.await

class GroupRepository {
    private val auth: FirebaseAuth = Firebase.auth
    private lateinit var dbRef: DatabaseReference

    private val validationHandler = ValidationHandler()

    suspend fun getUserGroupUnique(): String{
        var groupId = ""
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        dbRef.child("user_group").child(auth.currentUser?.uid!!).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    groupId = it.value.toString()
                }
            }.await()

        return groupId
    }

    private suspend fun getGroup(uniqueId: String): String {
        var groupId = ""
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        dbRef.child("group").child(uniqueId).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    groupId = it.value.toString()
                }
            }.await()

        return groupId
    }

    suspend fun getMemberList(): MutableList<String> {
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        val mainUserGroupId = getUserGroupUnique()

        Log.i("TAG", "getUserGroupUnique: $mainUserGroupId")

        val memberList: MutableList<String> = mutableListOf()

        dbRef.child("group_members").child(mainUserGroupId).get()
            .addOnSuccessListener { groupMembers ->
                if (groupMembers.exists()) {
                    groupMembers.children.map { child ->
                        memberList.add(child.key!!)
                    }
                }
            }
            .addOnFailureListener {
                Log.i("TAG", "getUserList: $it")
            }.await()

        Log.i("TAG", "getMemberList: $memberList")
        return memberList
    }

    suspend fun getUserList(memberList: List<String>): MutableList<UserState> {
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        val userItem: MutableList<UserState> = mutableListOf()

        Log.i("TAG", "getUserListREPO: $memberList")

        dbRef.child("user").get().addOnSuccessListener { parent ->
            memberList.forEach { userMember ->
                parent.children.forEach { user ->
                    if (user.key == userMember && user.key != auth.currentUser?.uid!!) {
                        userItem.add(
                            UserState(
                                user.key.toString(),
                                user.child("nickname").value.toString(),
                                user.child("guardian").value as Boolean
                            )
                        )
                    }
                }
            }
        }.await()

        return userItem
    }

    private lateinit var groupMeta: GroupMeta
    suspend fun getGroupMeta(): GroupMeta {
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        try {
            val uniqueId = getUserGroupUnique()
            val groupId = getGroup(uniqueId)

            if (groupId != "") {
                dbRef.child("group_meta").child(groupId).get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            groupMeta = GroupMeta(
                                it.child("longitude").value.toString().toDouble(),
                                it.child("latitude").value.toString().toDouble(),
                                it.child("groupId").value.toString(),
                                it.child("name").value.toString(),
                                it.child("secretKey").value.toString()
                            )
                        }
                    }
                    .addOnFailureListener {
                        Log.i("TAG", "getUserState: $it")
                    }.await()
            }

        } catch (e: Exception) {
            Log.i("TAG", "getGroupMeta: $e")
        }

        return groupMeta
    }

    suspend fun editGroup(view: View, group: GroupMeta, mainGroupId: String){
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        val uniqueId = getUserGroupUnique()

        dbRef.child("group").child(uniqueId).removeValue()
        dbRef.child("group_key").child(mainGroupId).removeValue()
        dbRef.child("group_meta").child(mainGroupId).removeValue()

        dbRef.child("group").child(uniqueId).setValue(group.groupId).await()
        dbRef.child("group_key").child(group.groupId).setValue(uniqueId).await()
        dbRef.child("group_meta").child(group.groupId).setValue(group).await()

        navigateGroupOut(view)
    }

    suspend fun validateEditGroup(groupId: String, mainGroupId: String): Boolean {
        var groupState = false

        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        dbRef.child("group_key").child(groupId).get()
            .addOnSuccessListener {
                if (!it.exists() || it.key == mainGroupId) {
                    groupState = true
                }
            }.await()
        return groupState
    }

    suspend fun createGroup(view: View, group: GroupMeta){
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        val currentUserId = auth.currentUser?.uid!!
        val uniqueKey = dbRef.push().key!!

        dbRef.child("group").child(uniqueKey).setValue(group.groupId).await()
        dbRef.child("group_meta").child(group.groupId).setValue(group).await()
        dbRef.child("group_members").child(uniqueKey).child(currentUserId).setValue(true).await()

        dbRef.child("user_group").child(currentUserId).setValue(uniqueKey).await()
        dbRef.child("group_key").child(group.groupId).setValue(uniqueKey).await()

        navigateGroupOut(view)
    }

    suspend fun validateGroupExists(groupId: String): Boolean {
        var groupState = false

        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        dbRef.child("group_key").child(groupId).get()
            .addOnSuccessListener {
                if (it.exists()){
                    groupState = it.exists()
                }
            }.await()

        return groupState
    }

    suspend fun joinGroup(view: View, groupId: String) {
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        val currentUserId = auth.currentUser?.uid!!

        var groupKey = ""
        dbRef.child("group_key").child(groupId).get()
            .addOnSuccessListener {
                groupKey = it.value.toString()
            }.await()

        dbRef.child("user_group").child(currentUserId).setValue(groupKey).await()
        dbRef.child("group_members").child(groupKey).child(currentUserId).setValue(true).await()

        navigateGroupOut(view)
    }

    suspend fun validateJoinGroup(context: Context, groupId: String, secretKey: String): Boolean {
        var groupJoinState = false

        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        dbRef.child("group_meta").child(groupId).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    if (it.child("secretKey").value.toString() == secretKey){
                        groupJoinState = true
                    } else {
                        Toast.makeText(context, "Password does not match", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Home does not exist", Toast.LENGTH_SHORT).show()
                }
            }.await()

        return groupJoinState
    }

    suspend fun leaveGroup(view: View){
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        val groupId = getUserGroupUnique()

        dbRef.child("user_group").child(auth.currentUser?.uid!!).removeValue().await()
        dbRef.child("group_members").child(groupId).child(auth.currentUser?.uid!!).removeValue().await()
        dbRef.child("track_user").child(groupId).child(auth.currentUser?.uid!!).removeValue().await()

        Navigation.findNavController(view).popBackStack(R.id.homeMain, true)
        Navigation.findNavController(view).navigate(R.id.navigation_home)
    }

    private fun navigateGroupOut(view: View){
        Navigation.findNavController(view).popBackStack(R.id.homeConnection, true)
        Navigation.findNavController(view).navigate(R.id.homeMain)
    }


    suspend fun setToTrackUser(context: Context, userId: String, userGroupUnique: String) {
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference

        dbRef.child("track_user").child(userGroupUnique).child(userId).setValue(true)
            .addOnSuccessListener {
                validationHandler.notifyMessage(context, "Tracking User Enabled", true)
            }.await()
    }

    suspend fun removeToTrackUser(context: Context, userId: String, userGroupUnique: String) {
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference

        dbRef.child("track_user").child(userGroupUnique).child(userId).removeValue()
            .addOnSuccessListener {
                validationHandler.notifyMessage(context, "Tracking User Disabled", true)
            }.await()
    }

    suspend fun getToTrackUser(userId: String, userGroupUnique: String): Boolean {
        var toTrack = false

        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        dbRef.child("track_user").child(userGroupUnique).child(userId).get()
            .addOnSuccessListener {
                toTrack = it.exists()
            }.await()

        return toTrack
    }


    suspend fun getTrackMemberList(): MutableList<String?> {
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        val mainUserGroupId = getUserGroupUnique()

        val memberList: MutableList<String?> = mutableListOf()

        dbRef.child("track_user").child(mainUserGroupId).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    it.children.map { child ->
                        memberList.add(child.key)
                    }
                }
            }
            .addOnFailureListener {
                Log.i("TAG", "getUserList: $it")
            }.await()

        return memberList
    }
}
