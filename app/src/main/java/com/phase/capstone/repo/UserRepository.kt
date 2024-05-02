package com.phase.capstone.repo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage
import com.phase.capstone.R
import com.phase.capstone.service.LocationForegroundService
import com.phase.capstone.viewmodels.ValidationHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class UserRepository {

    private val auth: FirebaseAuth = Firebase.auth
    private lateinit var dbRef: DatabaseReference

    private val validationHandler = ValidationHandler()

    private val storageRef = Firebase.storage.reference

    private lateinit var userInfo: User
    private lateinit var userState: UserState

    suspend fun getUserInfo(userId: String): User {
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference

        var userIdInit = auth.currentUser!!.uid
        if (userId.isNotBlank() || userId.isNotEmpty()) {
            userIdInit = userId
        }

        dbRef.child("user_info").child(userIdInit).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    userInfo = User(
                        it.child("address").value.toString(),
                        it.child("contactNumber").value.toString(),
                        it.child("email").value.toString(),
                        it.child("eyeColor").value.toString(),
                        it.child("firstName").value.toString(),
                        it.child("gender").value.toString(),
                        it.child("hairColor").value.toString(),
                        it.child("lastName").value.toString(),
                        it.child("marks").value.toString(),
                        it.child("medicalStatus").value.toString(),
                        it.child("middleName").value.toString(),
                        it.child("scars").value.toString(),
                        it.child("tattoos").value.toString()
                    )
                }
            }.await()
        return userInfo
    }

    suspend fun getUserState(userId: String): UserState {
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference

        var userIdInit = auth.currentUser!!.uid
        if (userId.isNotBlank() || userId.isNotEmpty()) {
            userIdInit = userId
        }

        dbRef.child("user").child(userIdInit).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    userState = UserState(
                        userIdInit,
                        it.child("nickname").value.toString(),
                        it.child("guardian").value.toString().toBoolean()
                    )
                }
            }
            .addOnFailureListener {
                Log.i("TAG", "getUserState: $it")
            }.await()
        return userState
    }

    suspend fun uploadImage(context: Context, path: Uri) {
        storageRef.child("images/${auth.currentUser?.uid!!}").putFile(path)
            .addOnSuccessListener {
                validationHandler.notifyMessage(context, "Image Uploaded", true)
            }.await()
    }

    suspend fun getImage(context: Context, ivProfilePic: ImageView, userId: String) {
        var userIdState = auth.currentUser!!.uid
        if (userId.isNotBlank() || userId.isNotEmpty()) {
            userIdState = userId
        }

        storageRef.child("images/$userIdState").downloadUrl
            .addOnSuccessListener {
                Glide.with(context)
                    .load(it)
                    .into(ivProfilePic)
            }
            .addOnFailureListener {
                ivProfilePic.setImageResource(R.drawable.ic_baseline_person)
            }.await()
    }

    suspend fun shareUser(activity: Activity, userId: String) {
        val localFile = withContext(Dispatchers.IO) {
            File.createTempFile("images", "jpg")
        }

        storageRef.child("images/${userId}").getFile(localFile)
            .addOnSuccessListener {
                val content = SharePhotoContent.Builder()
                    .addPhoto(
                        SharePhoto.Builder()
                            .setBitmap(BitmapFactory.decodeFile(localFile.absolutePath))
                            .build()
                    )
                    .build()
                ShareDialog.show(activity, content)
            }.addOnFailureListener {
                validationHandler.notifyMessage(
                    activity.baseContext,
                    "Target User Has No Profile Picture",
                    true
                )
            }.await()
    }

    suspend fun signInUser(activity: Activity, view: View, email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Navigation.findNavController(view)
                    .navigate(R.id.action_navigation_login_to_mainActivity)
                activity.finish()
            }.await()
    }

    suspend fun signUpUser(activity: Activity, view: View, nickname: String, isGuardian: Boolean, user: User, password: String) {
        auth.createUserWithEmailAndPassword(user.email, password).addOnSuccessListener {
            updateUserDatabase(
                R.id.action_register_to_mainActivity,
                activity,
                view,
                auth.currentUser!!,
                nickname,
                isGuardian,
                user
            )
        }.await()
    }

    suspend fun editUser(activity: Activity, view: View, nickname: String, isGuardian: Boolean, user: User, password: String, oldPassword: String) {
        val currentUser = auth.currentUser ?: return

        currentUser.reauthenticate(
            EmailAuthProvider.getCredential(
                currentUser.email.toString(),
                oldPassword
            )
        ).await()

        currentUser.updatePassword(password).addOnSuccessListener {
            updateUserDatabase(
                R.id.action_profile_edit_to_authActivity,
                activity,
                view,
                currentUser,
                nickname,
                isGuardian,
                user
            )
            signOutUser(activity)
        }.await()
    }

    private fun updateUserDatabase(id: Int, activity: Activity, view: View, currentUser: FirebaseUser, nickname: String, isGuardian: Boolean, user: User) {
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference

        val uid = currentUser.uid
        user.email = currentUser.email.toString()

        dbRef.child("user_info").child(uid).setValue(user)
            .addOnSuccessListener {
                dbRef.child("user").child(uid).setValue(UserState(uid, nickname, isGuardian))
                    .addOnSuccessListener {
                        Navigation.findNavController(view).navigate(id)
                        activity.finish()
                    }
            }
    }

    fun signOutUser(activity: Activity) {
        val intent = Intent(activity.baseContext, LocationForegroundService::class.java)
        activity.stopService(intent)
        auth.signOut()
    }

    fun userIsSignedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun uploadToken() {
        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference

        var token = ""
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            token = task.result
            Log.i("TAG", "uploadToken: $token")
        }).await()

        if (token != "") {
            dbRef.child("messaging_token").child(auth.currentUser?.uid!!).setValue(token).await()
        }
    }

    suspend fun getToken(userId: String): HashMap<String, String> {
        val messageMeta = HashMap<String, String>()

        dbRef = FirebaseDatabase.getInstance(
            "https://capstoneproject-8478a-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference

        dbRef.child("messaging_token").child(userId).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    messageMeta["token"] = it.value.toString()
                }
            }.await()

        dbRef.child("user").child(auth.currentUser?.uid!!).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    messageMeta["nickname"] = it.child("nickname").value.toString()
                }
            }.await()

        return messageMeta
    }
}
