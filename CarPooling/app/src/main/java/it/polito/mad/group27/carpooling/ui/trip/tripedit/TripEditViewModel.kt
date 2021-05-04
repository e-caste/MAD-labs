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

    private lateinit var userProfiles : Map<String, Profile>
    private val db = FirebaseFirestore.getInstance()
    private val users = db.collection("users")

    fun downloadUsers(callback : ( Map<String, Profile> )-> Unit){
        var mapTmp : MutableMap<String, Profile> = mutableMapOf()
        for (uid in newTrip.acceptedUsersUids){
            mapTmp[uid] = downloadProfileByUid(uid)
        }
        for (uid in newTrip.interestedUsersUids){
            mapTmp[uid] = downloadProfileByUid(uid)
        }
        userProfiles = mapTmp.toMap()
        callback(userProfiles)
    }

    private fun downloadProfileByUid(uid:String) : Profile{
        return users.document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(context.getString(R.string.log_tag), "DocumentSnapshot data: ${document.data}")
                    document as Profile
                } else {
                    Log.d(context.getString(R.string.log_tag), "No such document")
                    Profile(nickName = "UNAVAILABLE")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(context.getString(R.string.log_tag), "get failed with ", exception)
                Profile(nickName = "UNAVAILABLE")
            } as Profile
    }

    fun getProfileByUid(uid:String): Profile{
        return userProfiles[uid] ?: Profile(nickName = "UNAVAILABLE")
    }

    fun putToAccepted(uid: String){
        newTrip.interestedUsersUids.remove(uid)
        newTrip.acceptedUsersUids.add(uid)
    }

}