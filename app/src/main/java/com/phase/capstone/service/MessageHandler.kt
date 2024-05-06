package com.phase.capstone.service

import android.content.Context
import android.os.StrictMode
import android.util.Log
import android.widget.EditText
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.phase.capstone.viewmodels.ValidationHandler
import org.json.JSONObject

class MessageHandler {
    private val validationHandler = ValidationHandler()

    companion object {
        const val BASE_URL = "https://fcm.googleapis.com/fcm/send"
        const val SERVER_KEY = "key=TESTGIT"
    }

    fun sendMessage(context: Context, token: String, sender: String, message: String, etMessage: EditText) {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())

        val json = JSONObject()
        json.put("to", token)

        val notification = JSONObject()
        notification.put("title", sender)
        notification.put("body", message)

        json.put("notification", notification)

        try {
            val request = object : JsonObjectRequest(Method.POST, BASE_URL, json,
                Response.Listener {
                    Log.i("TAG", "sendMessageSUCCESS: $it")
                    validationHandler.notifyMessage(context, "Message Sent", true)
                    etMessage.text.clear()
                }, Response.ErrorListener {
                    Log.i("TAG", "sendMessageERROR: $it")
                    validationHandler.notifyMessage(context, "Slow or No Internet Connection", true)
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["Content-Type"] = "application/json"
                    params["Authorization"] = SERVER_KEY
                    return params
                }
            }

            Log.i("TAG", "SENDER: $sender")
            Log.i("TAG", "TOKEN: $token")
            Volley.newRequestQueue(context).add(request)
        } catch (e: Exception) {
            Log.i("TAG", "sendMessageCATCH: $e")
        }
    }

}
