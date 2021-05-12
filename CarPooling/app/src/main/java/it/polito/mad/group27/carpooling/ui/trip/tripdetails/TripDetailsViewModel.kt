package it.polito.mad.group27.carpooling.ui.trip.tripdetails

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.trip.Trip
import it.polito.mad.group27.carpooling.ui.trip.TripDB

class TripDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var tripDocument:DocumentReference
    private val db = FirebaseFirestore.getInstance()
    val trip: MutableLiveData<Trip> = MutableLiveData(null)

    fun loadTrip(tripId : String) : Trip {
        tripDocument = db.collection("trips").document(tripId)

        tripDocument.addSnapshotListener { value, e ->
            if (e != null) {
                throw Exception("No trip found")
            } else {
                Log.d(getLogTag(),"got here (SnapshotListener)")
                trip.value = value!!.toObject(TripDB::class.java)!!.toTrip()
            }
        }

        return trip.value ?: Trip()

    }
}

