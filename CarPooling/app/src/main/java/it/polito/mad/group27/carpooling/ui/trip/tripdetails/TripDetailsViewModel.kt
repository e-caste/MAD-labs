package it.polito.mad.group27.carpooling.ui.trip.tripdetails

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.group27.carpooling.Profile
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.trip.Trip
import it.polito.mad.group27.carpooling.ui.trip.TripDB

class TripDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var tripDocument:DocumentReference
    private val db = FirebaseFirestore.getInstance()
    private val users = db.collection("users")

    val trip: MutableLiveData<Trip> = MutableLiveData(null)
    var stopsExpanded: Int = View.GONE
    var interestedExpanded: Int = View.GONE
    var acceptedExpanded: Int = View.GONE

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

    fun loadInterestedUsers() : List<Profile> {
        var passengersList: MutableList<Profile> = mutableListOf()
        users.whereIn("uid",trip.value!!.interestedUsersUids).get().addOnSuccessListener { value ->
            passengersList.addAll(value.toObjects(Profile::class.java))
        }
        return passengersList
    }

    fun loadAcceptedUsers() : List<Profile> {
        var passengersList: MutableList<Profile> = mutableListOf()
        users.whereIn("uid",trip.value!!.acceptedUsersUids).get().addOnSuccessListener { value ->
            passengersList.addAll(value.toObjects(Profile::class.java))
        }
        return passengersList
    }


}

