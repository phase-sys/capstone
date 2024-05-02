package com.phase.capstone.viewmodels

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DatabaseException
import com.google.maps.android.SphericalUtil
import com.phase.capstone.R
import com.phase.capstone.repo.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt

class GroupViewModel : ViewModel() {
    private val groupRepo = GroupRepository()
    private val locRepo = LocationRepository()

    private val validationHandler = ValidationHandler()
    private val networkHandler = NetworkHandler()

    private val _isViewOnly = MutableLiveData<Boolean>()
    val isViewOnly = _isViewOnly

    private var _memberList = MutableLiveData<List<String>>()
    val memberList = _memberList

    fun getMemberList(context: Context) {
        viewModelScope.launch {
            try {
                _memberList.value = groupRepo.getMemberList()
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

    private var _userList = MutableLiveData<List<UserState>>()
    val userList = _userList

    fun getUserList(context: Context, memberList: List<String>) {
        viewModelScope.launch {
            try {
                _userList.value = groupRepo.getUserList(memberList)
                Log.i("TAG", "getUserList: memberList: $memberList")
                Log.i("TAG", "getUserList: ${groupRepo.getUserList(memberList)}")
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

    private val _groupMeta = MutableLiveData<GroupMeta>()
    val groupMeta: LiveData<GroupMeta> = _groupMeta

    fun getGroupMeta(context: Context) {
        viewModelScope.launch {
            try {
                _groupMeta.value = groupRepo.getGroupMeta()
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

    fun editGroup(context: Context, view: View, group: GroupMeta, mainGroupId: String) {
        if (!validationHandler.createGroupCheckInputs(context, group)) return

        viewModelScope.launch {
            try {
                if (groupRepo.validateEditGroup(group.groupId, mainGroupId)) {
                    groupRepo.editGroup(view, group, mainGroupId)
                } else {
                    Toast.makeText(
                        context,
                        "Home ID already exists, please try another",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: DatabaseException) {
                Log.i("TAG", "createGroup: $e")
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

    fun createGroup(context: Context, view: View, group: GroupMeta) {
        if (!validationHandler.createGroupCheckInputs(context, group)) return

        viewModelScope.launch {
            try {
                if (!(groupRepo.validateGroupExists(group.groupId))) {
                    groupRepo.createGroup(view, group)
                } else {
                    validationHandler.notifyMessage(
                        context,
                        "Home ID already exists, please try another",
                        true
                    )
                }
            } catch (e: DatabaseException) {
                Log.i("TAG", "createGroup: $e")
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

    fun joinGroup(context: Context, view: View, groupId: String, secretKey: String) {
        if (!validationHandler.joinGroupCheckInputs(context, groupId, secretKey)) return

        viewModelScope.launch {
            try {
                if (groupRepo.validateJoinGroup(context, groupId, secretKey)) {
                    groupRepo.joinGroup(view, groupId)
                }
            } catch (e: DatabaseException) {
                validationHandler.notifyMessage(
                    context,
                    "Home does not exist",
                    true
                )
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

    private val _isGrouped = MutableLiveData<Boolean>()
    val isGrouped = _isGrouped

    fun checkUserGroup(context: Context) {
        viewModelScope.launch {
            try {
                if (groupRepo.getUserGroupUnique() != "") {
                    _isGrouped.value = true
                }

            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

    fun leaveGroup(context: Context, view: View) {
        viewModelScope.launch {
            try {
                groupRepo.leaveGroup(view)
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }


    fun setToTrackUser(context: Context, userId: String, userGroupUnique: String) {
        viewModelScope.launch {
            try {
                groupRepo.setToTrackUser(context, userId, userGroupUnique)
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

    fun removeToTrackUser(context: Context, userId: String, userGroupUnique: String) {
        viewModelScope.launch {
            try {
                groupRepo.removeToTrackUser(context, userId, userGroupUnique)
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

    private val _userGroupUnique = MutableLiveData<String>()
    val userGroupUnique = _userGroupUnique
    fun getUserGroupUnique(context: Context) {
        viewModelScope.launch {
            try {
                _userGroupUnique.value = groupRepo.getUserGroupUnique()
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

    private val _isTracked = MutableLiveData<Boolean>()
    val isTracked = _isTracked
    fun getToTrackUser(context: Context, userId: String, userGroupUnique: String) {
        viewModelScope.launch {
            try {
                _isTracked.value = groupRepo.getToTrackUser(userId, userGroupUnique)
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

    companion object {
        const val DISTANCE_TOLERANCE = 10
    }
    
    @SuppressLint("MissingPermission")
    fun getToTrackGroup(context: Context, CHANNEL_ID: String, notificationManager: NotificationManager) {
        viewModelScope.launch {
            try {
                val trackGroup = locRepo.getTrackingMemberLocationFromDB()
                val trackCurrentUser = locRepo.getCurrentUserLocationFromDB()
                trackGroup.forEachIndexed { index, it ->
                    val channelId = index + 2

                    val distance = SphericalUtil.computeDistanceBetween(
                        LatLng(
                            trackCurrentUser["latitude"]!!.toDouble(),
                            trackCurrentUser["longitude"]!!.toDouble()
                        ), LatLng(it.latitude, it.longitude)
                    )
                    if (distance > DISTANCE_TOLERANCE) {
                        val notification: Notification = Notification.Builder(context, CHANNEL_ID)
                            .setContentTitle("Tracking")
                            .setContentText("${it.nickname} is ${distance.roundToInt()} meter/s away far from you")
                            .setSmallIcon(R.drawable.ic_baseline_track_changes)
                            .setGroup(CHANNEL_ID)
                            .build()

                        NotificationManagerCompat.from(context).notify(channelId, notification)

                        Log.i("TAG", "Distance: $distance is Greater than 10 Meters")
                    } else {
                        notificationManager.cancel(channelId)
                        Log.i("TAG", "Distance: $distance is Less than 10 Meters")
                    }
                }
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

}