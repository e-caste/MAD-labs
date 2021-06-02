package it.polito.mad.group27.carpooling.ui.profile.editprofile

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.entities.Profile

class EditProfileViewModel(application: Application): AndroidViewModel(application) {

    lateinit var profile: Profile

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    fun updateProfile() {
        db.collection("users").document(auth.currentUser!!.uid).set(profile).addOnFailureListener{
            Toast.makeText(
                getApplication<Application>().applicationContext,
                getApplication<Application>().applicationContext.getString(R.string.save_profile_error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}


