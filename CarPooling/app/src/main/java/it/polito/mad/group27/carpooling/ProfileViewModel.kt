package it.polito.mad.group27.carpooling

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel(application: Application) : AndroidViewModel(application) {


    var profile: MutableLiveData<Profile?> = MutableLiveData(null)

    lateinit var profileDocument:DocumentReference

    private var db = FirebaseFirestore.getInstance()


    fun loadProfile(currentUser: FirebaseUser) {
        profileDocument = db.collection("users").document(currentUser.uid)
        profileDocument.get().addOnSuccessListener {

            if (!it.exists()) {
                profileDocument.set(
                    Profile(
                        currentUser.uid,
                        currentUser.photoUrl?.toString(),
                        currentUser.displayName ?: "",
                        email = currentUser.email ?: ""
                    )
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

        profileDocument.addSnapshotListener{
            value, e ->
            if(e!=null){
                //TODO manage errors
            }else {
                profile.value = value!!.toObject(Profile::class.java)
            }
        }
    }

    fun updateProfile(profileTmp: Profile) {
        profileDocument.set(profileTmp).addOnFailureListener{
            Toast.makeText(
                getApplication<Application>().applicationContext,
                "Error in saving profile",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


}