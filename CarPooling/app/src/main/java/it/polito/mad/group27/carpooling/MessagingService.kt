package it.polito.mad.group27.carpooling

import android.os.Parcelable
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


class MessagingService : FirebaseMessagingService() {

    companion object Oauth {
        lateinit var oauthToken: String

        private const val projectId = 378735075553
        const val host = "fcm.googleapis.com"
        const val path = "/v1/projects/$projectId/messages:send"


        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl("https://$host/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NotificationAPI::class.java)

        }


        suspend fun sendNotification(userToken: String?, notification: AndroidNotification) : Boolean{

            if(userToken == null) {
                return false
            }

            val headers = HashMap<String, String>()
            headers["Authorization"] = "Bearer $oauthToken"
            headers["Host"] = host

            val notificationObject = NotificationObject(userToken, notification)
            val body = Message(notificationObject, false)

            val response = retrofit.sendNotification(headers, body)
            val snackText :String = when {
                response.isSuccessful -> {
                    "Notification sent correctly"
                }
                response.code() == 401 -> {
                    "Your token has expired, please reopen the app"
                }
                else -> "Generic error: ${response.body()}"
            }

            response.body()?.let { Log.d(getLogTag(), it.toString()) }
            Log.d(getLogTag(), snackText)

            return response.isSuccessful
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