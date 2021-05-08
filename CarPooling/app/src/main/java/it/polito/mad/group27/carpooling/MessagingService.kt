package it.polito.mad.group27.carpooling

import android.os.Parcelable
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MessagingService : FirebaseMessagingService() {

    companion object oauth {
        lateinit var oauthToken: String

        fun sendNotification(userToken: String?, notification: AndroidNotification){
            val projectId = 378735075553
            val host = "fcm.googleapis.com"
            val path = "/v1/projects/$projectId/messages:send"

            if(userToken == null)
                return

            Thread{
                val url = URL("https://$host$path")
                var client = url.openConnection() as HttpURLConnection
                client.requestMethod = "POST"
                client.setRequestProperty("Content-Type", "application/json")
                client.setRequestProperty("Authorization", "Bearer $oauthToken")
                client.setRequestProperty("Host", host)
                client.setRequestProperty("Connection", "keep-alive")
                val notificationObject = NotificationObject(userToken, notification)
                val message = Json.encodeToString(Message(notificationObject, false)).toByteArray()
                client.setRequestProperty("Content-Length", message.size.toString())
                client.doOutput = true
                Log.d("MAD-group27", Json.encodeToString(Message(notificationObject, false)))

                // connect
                val outputPost = BufferedOutputStream(client.outputStream)


                outputPost.write(message)
                outputPost.flush()
                outputPost.close()

                var line : String? = ""
                val isr = InputStreamReader(client.getInputStream())
                val reader = BufferedReader(isr)
                val sb = StringBuilder()
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line.toString() + "\n")
                }

                Log.d("MAD-group27", client.responseCode.toString()+ " " + client.responseMessage)

                client.disconnect()
            }.start()

        }
    }

    override fun onNewToken(token: String) {
        ProfileViewModel.updateUserNotificationToken(token)
        super.onNewToken(token)
    }


}

@Parcelize
@Serializable
data class AndroidNotification(
    val title: String,
    val body: String,
    private var image: String?
) : Parcelable {
    init {
        if (image == null){
            image = "gs://madcarpooling.appspot.com/ic_launcher.png"
        }
    }
}

@Parcelize
@Serializable
private data class NotificationObject(val token:String, val notification: AndroidNotification): Parcelable

@Parcelize
@Serializable
private data class Message(val message: NotificationObject, val validate_only: Boolean): Parcelable