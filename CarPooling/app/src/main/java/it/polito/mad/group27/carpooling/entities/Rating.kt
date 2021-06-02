package it.polito.mad.group27.carpooling.entities

import com.google.firebase.firestore.DocumentReference

data class Rating(
    val tripId: DocumentReference,  // implicitly contains driverUid
    val passengerUid: DocumentReference,
    val rating: Int,
    val comment: String,
)
