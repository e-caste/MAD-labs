package it.polito.mad.group27.carpooling

import android.net.Uri
import android.os.Parcelable
import it.polito.mad.group27.carpooling.ui.trip.CalendarSerializer
import it.polito.mad.group27.carpooling.ui.trip.UriSerializer
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@Parcelize
data class Profile (
    val uid: String,
    @Serializable(with= UriSerializer::class)
    var profileImageUri: Uri? = null,
    var fullName: String = "John Smith",
    var nickName: String = "MadJohn",
    var email: String = "john.smith@polito.it",
    var location: String = "Turin, Italy",
    @Serializable(with= CalendarSerializer::class)
    var registrationDate: Calendar = Calendar.getInstance(),
    var rating: Float = 4.5f
    // TODO implement rating logic
): Parcelable