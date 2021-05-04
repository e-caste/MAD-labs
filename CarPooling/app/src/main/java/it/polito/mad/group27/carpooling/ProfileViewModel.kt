package it.polito.mad.group27.carpooling

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel(application: Application) : AndroidViewModel(application) {


    var profile: MutableLiveData<Profile?> = MutableLiveData(null)

    private var db = FirebaseFirestore.getInstance()


    fun loadProfile(currentUser: FirebaseUser) {
        val tmpProfile = db.collection("users").document(currentUser.uid)
        tmpProfile.get().addOnSuccessListener {

            if (!it.exists()) {
                tmpProfile.set(
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

        tmpProfile.addSnapshotListener{
            value, e ->
            if(e!=null){
                //TODO manage errors
            }else {
                profile.value = value!!.toObject(Profile::class.java)
            }
        }
    }


}