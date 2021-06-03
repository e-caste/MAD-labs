package it.polito.mad.group27.carpooling.entities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class Review(
    // implicitly contains driverUid
    val tripId: DocumentReference? = null,
    val passengerUid: DocumentReference? = null,
    val rating: Long = 0L,
    val comment: String = "",
    @field:JvmField
    val isForDriver: Boolean = false,
    val timestamp: Timestamp = Timestamp.now(),
)
