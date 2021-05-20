package it.polito.mad.group27.carpooling.entities

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Profile(
    val uid: String?=null,
    var profileImageUri: String? = null,
    var fullName: String = "John Smith",
    var nickName: String = "MadJohn",
    var email: String = "john.smith@polito.it",
    var location: String = "Turin, Italy",
    var registrationDate: Timestamp = Timestamp.now(),
    var rating: Float = 4.5f,
    var notificationToken: String?=null
    // TODO implement rating logic
):Parcelable