package it.polito.mad.group27.carpooling.ui.trip.tripedit

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.group27.carpooling.Profile
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.ui.trip.Trip

class TripEditViewModel(private val context: Context) : ViewModel() {

    lateinit var trip: Trip
    lateinit var newTrip : Trip

class TripEditViewModel : ViewModel() {
    // TODO: Implement the ViewModel
}