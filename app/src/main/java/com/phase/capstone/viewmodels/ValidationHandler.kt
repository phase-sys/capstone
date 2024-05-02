package com.phase.capstone.viewmodels

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import com.phase.capstone.repo.GroupMeta
import com.phase.capstone.repo.User

class ValidationHandler {
    fun notifyMessage(context: Context, input: String, customize: Boolean = false): Boolean{
        val customizedToast = Toast.makeText(context, input, Toast.LENGTH_SHORT)
        val defaultToast = Toast.makeText(context, "Please fill up your $input", Toast.LENGTH_SHORT)
        if(customize){
            customizedToast.show()
        } else {
            defaultToast.show()
        }
        return false
    }

    fun signInCheckInputs(context: Context, email: String, password: String): Boolean {
        return if (email.isBlank()) {
            notifyMessage(context, "email")
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            notifyMessage(context, "Please provide your email domain, such as @gmail.com", true)
        } else if (password.isBlank()) {
            notifyMessage(context, "password")
        } else if (password.length < 8 ) {
            notifyMessage(context, "Password should be greater than 8 characters", true)
        } else {
            return true
        }
    }

    fun signUpCheckInputs(context: Context, nickname: String, user: User, password: String, oldPassword: String, isEdit: Boolean): Boolean{
        return if (user.email.isBlank() && !isEdit) {
            notifyMessage(context, "email")
        } else if (!Patterns.EMAIL_ADDRESS.matcher(user.email).matches() && !isEdit) {
            notifyMessage(context, "Please provide your email domain, such as @gmail.com", true)
        } else if (isEdit && oldPassword.isBlank()) {
            notifyMessage(context, "current password")
        } else if (isEdit && oldPassword.length < 8 ) {
            notifyMessage(context, "Current password should be greater than 8 characters", true)
        }else if (password.isBlank()) {
            notifyMessage(context, "password")
        } else if (password.length < 8 ) {
            notifyMessage(context, "Confirm/New Password should be greater than 8 characters", true)
        } else if(nickname.isBlank()){
            notifyMessage(context, "nickname")
        } else if(user.firstName.isBlank()){
            notifyMessage(context, "first name")
        } else if(user.middleName.isBlank()){
            notifyMessage(context, "middle name")
        } else if(user.lastName.isBlank()){
            notifyMessage(context, "last name")
        } else if(user.gender.isBlank()){
            notifyMessage(context, "gender")
        } else if(user.contactNumber.length != 11){
            notifyMessage(context, "contact number")
        } else if(!user.contactNumber.startsWith("09")){
            notifyMessage(context, "Invalid Contact Number", true)
        } else if(user.address.isBlank()){
            notifyMessage(context, "address")
        } else if(user.hairColor.isBlank()) {
            notifyMessage(context, "hair color")
        } else if(user.eyeColor.isBlank()) {
            notifyMessage(context, "eye color")
        } else if(user.scars.isBlank()){
            notifyMessage(context, "scars")
        } else if(user.marks.isBlank()){
            notifyMessage(context, "marks")
        } else if(user.tattoos.isBlank()){
            notifyMessage(context, "tattoos")
        } else if(user.medicalStatus.isBlank()){
            notifyMessage(context, "medical status")
        } else {
            return true
        }
    }

    fun createGroupCheckInputs(context: Context, group: GroupMeta): Boolean {
        return if(group.groupId.isBlank()){
            notifyMessage(context, "home ID")
        } else if(group.name.isBlank()){
            notifyMessage(context, "home name")
        } else if(group.secretKey.isBlank()){
            notifyMessage(context, "secret key")
        } else if(group.secretKey.length < 8){
            notifyMessage(context, "Secret Key should be greater than 8 characters", true)
        } else if(group.latitude == null || group.longitude == null){
            notifyMessage(context, "Please tap on the map to set home location", true)
        } else {
            return true
        }
    }

    fun joinGroupCheckInputs(context: Context, groupId: String, secretKey: String): Boolean {
        return if(groupId.isBlank()){
            notifyMessage(context, "home ID")
        } else if(secretKey.isBlank()){
            notifyMessage(context, "secret key")
        } else if(secretKey.length < 8){
            notifyMessage(context, "Secret Key should be greater than 8 characters", true)
        } else {
            return true
        }
    }
}