package it.polito.mad.group27.carpooling.ui.trip.tripedit

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.group27.carpooling.Profile
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.ui.trip.Trip

class TripEditViewModel(application: Application) : AndroidViewModel(application)  {

    lateinit var trip: Trip
    lateinit var newTrip : Trip

    private lateinit var userProfiles : Map<String, Profile>
    private val db = FirebaseFirestore.getInstance()
    private val users = db.collection("users")

    private val context by lazy { getApplication<Application>().applicationContext }

    }

    fun downloadUsers(callback : ( Map<String, Profile> )-> Unit){
        val mapTmp : MutableMap<String, Profile> = mutableMapOf()
        val allUids : List<String> = newTrip.acceptedUsersUids + newTrip.interestedUsersUids

        users.whereIn("uid", allUids).get().addOnSuccessListener {
            value ->
                for (doc in value!!){
                    val profile = doc.toObject(Profile::class.java)
                    mapTmp[profile.uid!!] = profile
                }
                userProfiles = mapTmp.toMap()
                callback(userProfiles)
        }
    }

    fun getProfileByUid(uid:String): Profile{
        return userProfiles[uid] ?: Profile(nickName = "UNAVAILABLE")
    }

    fun putToAccepted(uid: String){
        newTrip.interestedUsersUids.remove(uid)
        newTrip.acceptedUsersUids.add(uid)
    }

}