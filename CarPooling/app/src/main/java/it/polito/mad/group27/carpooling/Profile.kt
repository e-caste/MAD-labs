package it.polito.mad.group27.carpooling

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Profile (
    var fullName: String = "John Smith",
    var nickName: String = "MadJohn",
    var email: String = "john.smith@polito.it",
    var location: String = "Turin, Italy",
    var registrationDate: String = "25/03/2021",
    var rating: Float = 4.5f,
): Parcelable