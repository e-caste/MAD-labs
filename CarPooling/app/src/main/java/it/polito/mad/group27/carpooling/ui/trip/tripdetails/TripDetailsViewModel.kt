package it.polito.mad.group27.carpooling.ui.trip.tripdetails

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.group27.carpooling.ui.trip.Trip

class TripDetailsViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var tripDocument:DocumentReference
    private val db = FirebaseFirestore.getInstance()
    val trip: MutableLiveData<Trip?> = MutableLiveData(null)

    fun loadTrip(tripId : String) {
        tripDocument = db.collection("trips").document(tripId)
        tripDocument.get().addOnSuccessListener {

            if (!it.exists()) {
                tripDocument.set(
                    Trip()
                ).addOnFailureListener {
                    Toast.makeText(
                        getApplication<Application>().applicationContext,
                        "Error in saving profile",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(
                getApplication<Application>().applicationContext,
                "Error in loading profile",
                Toast.LENGTH_SHORT
            ).show()
        }

        tripDocument.addSnapshotListener { value, e ->
            if (e != null) {
                throw Exception("No trip found")
            } else {
                trip.value = value!!.toObject(Trip::class.java)
            }
        }
    }
}

