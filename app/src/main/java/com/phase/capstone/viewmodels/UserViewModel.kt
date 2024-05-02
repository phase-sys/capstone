package com.phase.capstone.viewmodels

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.storage.StorageException
import com.phase.capstone.R
import com.phase.capstone.repo.NetworkHandler
import com.phase.capstone.repo.User
import com.phase.capstone.repo.UserRepository
import com.phase.capstone.repo.UserState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel: ViewModel() {
    private val userRepo = UserRepository()
    private val validationHandler = ValidationHandler()
    private val networkHandler = NetworkHandler()

    private val _userState = MutableLiveData<UserState>()
    val userState: LiveData<UserState> = _userState

    private val _userInfo = MutableLiveData<User>()
    val userInfo: LiveData<User> = _userInfo

    fun getUserProfile(context:Context, userId: String){
        viewModelScope.launch {
            try {
                _userInfo.value = userRepo.getUserInfo(userId)
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

    fun getUserState(context:Context, userId: String){
        viewModelScope.launch {
            try {
                _userState.value = userRepo.getUserState(userId)
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

    fun uploadImage(context:Context, path: Uri){
        viewModelScope.launch {
            try {
                validationHandler.notifyMessage(context, "Uploading Image", true)
                userRepo.uploadImage(context, path)
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

    fun getImage(context: Context, ivProfilePic: ImageView, userId: String) {
        viewModelScope.launch {
            try {
                userRepo.getImage(context, ivProfilePic, userId)
            } catch (e: StorageException) {
                ivProfilePic.setImageResource(R.drawable.ic_baseline_person)
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

    fun shareUser(activity: Activity, context: Context, userId: String) {
        viewModelScope.launch (Dispatchers.IO) {
            try {
                userRepo.shareUser(activity, userId)
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

    fun signInUser(activity: Activity, context: Context, view:View, email: String, password: String) {
        if (!validationHandler.signInCheckInputs(context, email, password)) return

        viewModelScope.launch {
            try {
                userRepo.signInUser(activity, view, email, password)
            } catch (e: FirebaseAuthInvalidUserException) {
                validationHandler.notifyMessage(context, "User does not exist", true)
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                validationHandler.notifyMessage(context, "Username or Password is incorrect", true)
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

    fun signUpUser(isEdit: Boolean, activity:Activity, context: Context, view: View,
        nickname: String, isGuardian: Boolean, user: User, password: String, oldPassword: String) {

        if (!validationHandler.signUpCheckInputs(context, nickname, user, password, oldPassword, isEdit)) return

        if(isEdit){
            viewModelScope.launch {
                try {
                    userRepo.editUser(activity, view, nickname, isGuardian, user, password, oldPassword)
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    validationHandler.notifyMessage(context, "The password does not match or is invalid", true)
                } catch (e: Exception) {
                    networkHandler.checkNetwork(context)
                }
            }
        } else {
            viewModelScope.launch {
                try {
                    userRepo.signUpUser(activity, view, nickname, isGuardian, user, password)
                } catch (e: FirebaseAuthUserCollisionException) {
                    validationHandler.notifyMessage(context, "Email is already taken", true)
                } catch (e: Exception) {
                    Log.i("TAG", "$e")
                    networkHandler.checkNetwork(context)
                }
            }
        }
    }

    fun userIsSignedIn(): Boolean{
        return userRepo.userIsSignedIn()
    }

    fun signOutUser(activity:Activity, view: View){
        viewModelScope.launch {
            userRepo.signOutUser(activity)
            Navigation.findNavController(view).navigate(R.id.action_navigation_profile_to_authActivity)
            activity.finish()
        }
    }

    fun uploadToken() {
        viewModelScope.launch {
            try {
                userRepo.uploadToken()
            } catch (e: Exception) {
                Log.i("TAG", "uploadToken: $e")
            }
        }
    }

    var messageToken = MutableLiveData<HashMap<String, String>>()
    fun getToken(context: Context, userId: String) {
        viewModelScope.launch {
            try {
                messageToken.value = userRepo.getToken(userId)
            } catch (e: Exception) {
                networkHandler.checkNetwork(context)
            }
        }
    }

}