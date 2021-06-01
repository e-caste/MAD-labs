package it.polito.mad.group27.carpooling

import android.os.Parcelable
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.gson.Gson
import it.polito.mad.group27.carpooling.ui.trip.tripedit.SearchAPI
import it.polito.mad.group27.carpooling.ui.trip.tripedit.Suggestion
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MessagingService : FirebaseMessagingService() {

    companion object Oauth {
        lateinit var oauthToken: String

        const val projectId = 378735075553
        const val host = "fcm.googleapis.com"
        const val path = "/v1/projects/$projectId/messages:send"


        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl("https://$host/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NotificationAPI::class.java)

        }


        fun sendNotification(userToken: String?, notification: AndroidNotification){

            if(userToken == null) {
                //TODO notify no auth token
                return
            }

            MainScope().launch{

                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer $oauthToken")
                headers.put("Host", host)

                val notificationObject = NotificationObject(userToken, notification)
                val body = Message(notificationObject, false)

                val response = retrofit.sendNotification(headers, body)
                lateinit var snackText :String
                if(response.isSuccessful){
                    snackText = "Notification sent correclty"
                }else if(response.code() == 401){
                    snackText = "Your token has expired, please reopen the app"
                }else
                    snackText = "Generic error: ${response.body()}"

                //TODO make snackbar properly and format response body
                response.body()?.let { Log.d(getLogTag(), it.toString()) }
                Log.d(getLogTag(), snackText)
            }

        }
    }

    override fun onNewToken(token: String) {
        ProfileViewModel.updateUserNotificationToken(token)
        super.onNewToken(token)
    }


}

interface NotificationAPI{
    @POST(MessagingService.path)
    suspend fun sendNotification(@HeaderMap headers:Map<String, String>,
                                 @Body notification: Message): Response<Gson>
}

@Parcelize
@Serializable
data class AndroidNotification(
    val title: String,
    val body: String,
    var image: String?
) : Parcelable {
    init {
        if (image == null){
            image = "gs://madcarpooling.appspot.com/ic_launcher.png"
        }
    }
}

@Parcelize
@Serializable
data class NotificationObject(val token:String, val notification: AndroidNotification): Parcelable

@Parcelize
@Serializable
data class Message(val message: NotificationObject, val validate_only: Boolean): Parcelable