package it.polito.mad.group27.carpooling.entities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class Review(
    val tripId: DocumentReference,  // implicitly contains driverUid
    val passengerUid: DocumentReference,
    val rating: Long,
    val comment: String,
    val isForDriver: Boolean,
    val timestamp: Timestamp,
)
