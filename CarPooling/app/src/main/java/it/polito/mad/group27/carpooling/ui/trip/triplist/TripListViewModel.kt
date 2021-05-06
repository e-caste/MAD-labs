package it.polito.mad.group27.carpooling.ui.trip.triplist

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.trip.Trip

class TripListViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()

    private val _trips = MutableLiveData<List<Trip>>()
    val trips: LiveData<List<Trip>>
        get() = _trips

    companion object {
        private const val coll = "trips"
    }

    fun loadTrips(currentUser: FirebaseUser, showCurrentUserTrips: Boolean) {
        val filteredTrips = if (showCurrentUserTrips) {
            db.collection(coll).whereEqualTo("ownerUid", currentUser.uid)
        } else {
            db.collection(coll).whereNotEqualTo("ownerUid", currentUser.uid)
        }
        filteredTrips
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    Log.d(getLogTag(), "error while retrieving $coll from Firebase: $error")
                } else {
                    if (documents != null) {
                        val tmpList = mutableListOf<Trip>()
                        for (doc in documents) {
                            tmpList.add(doc.toObject(Trip::class.java))
                        }
                        _trips.value = tmpList
                    } else {
                        Log.d(getLogTag(), "documents collection $coll is null")
                    }
                }
            }
    }
}