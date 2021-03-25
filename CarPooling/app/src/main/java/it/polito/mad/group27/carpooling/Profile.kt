package it.polito.mad.group27.carpooling

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Profile (
    var fullName: String = "",
    var nickName: String = "",
    var email: String = "",
    var location: String = "",
    var registrationDate: String = "",
    var rating: Float = .0f,
): Parcelable