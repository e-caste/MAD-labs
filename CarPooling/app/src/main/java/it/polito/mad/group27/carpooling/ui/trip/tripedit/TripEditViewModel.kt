package it.polito.mad.group27.carpooling.ui.trip.tripedit

import android.app.Application
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.group27.carpooling.Profile
import it.polito.mad.group27.carpooling.ui.trip.Trip
import it.polito.mad.group27.carpooling.ui.trip.TripDB

class TripEditViewModel(application: Application) : AndroidViewModel(application)  {

    lateinit var newTrip : Trip

    val newAcceptedUsers = mutableListOf<String>()

    var totalSeats : MutableLiveData<Int?> = MutableLiveData(null)

    private lateinit var userProfiles : Map<String, Profile>
    private val db = FirebaseFirestore.getInstance()
    private val users = db.collection("users")

    private val context by lazy { getApplication<Application>().applicationContext }

    var acceptedVisibility = View.VISIBLE
    var interestedVisibility = View.VISIBLE
    var acceptedExpandVisibility = MutableLiveData(View.VISIBLE)
    var interestedExpandVisibility = MutableLiveData(View.VISIBLE)

    fun downloadUsers(callback : ( Map<String, Profile> )-> Unit){
        val mapTmp : MutableMap<String, Profile> = mutableMapOf()
        val allUids : List<String> = newTrip.acceptedUsersUids + newTrip.interestedUsersUids
        if (allUids.isNotEmpty())
            users.whereIn("uid", allUids).get().addOnSuccessListener {
                value ->
                if (value != null){
                    for (doc in value){
                        val profile = doc.toObject(Profile::class.java)
                        mapTmp[profile.uid!!] = profile
                    }
                    userProfiles = mapTmp.toMap()
                    callback(userProfiles)
                }
            }
    }

    fun getProfileByUid(uid:String): Profile{
        return userProfiles[uid] ?: Profile(nickName = "UNAVAILABLE")
    }

    fun putToAccepted(uid: String){
        newTrip.interestedUsersUids.remove(uid)
        newTrip.acceptedUsersUids.add(uid)
        newAcceptedUsers.add(uid)
    }

    fun getNewId(): String{
        return db.collection("trips").document().id
    }

    fun updateTrip(tripDB: TripDB) {
        val tripDocument = db.collection("trips").document(tripDB.id!!)
        tripDocument.set(tripDB).addOnFailureListener{
            Toast.makeText(
                getApplication<Application>().applicationContext,
                "Error in saving trip",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}