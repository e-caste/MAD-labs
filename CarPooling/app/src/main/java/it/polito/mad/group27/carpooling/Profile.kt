package it.polito.mad.group27.carpooling

import java.io.Serializable

data class Profile (
    var fullName: String = "",
    var nickName: String = "",
    var email: String = "",
    var location: String = "",
    var registrationDate: String = "",
    var rating: Float = .0f,
) : Serializable
