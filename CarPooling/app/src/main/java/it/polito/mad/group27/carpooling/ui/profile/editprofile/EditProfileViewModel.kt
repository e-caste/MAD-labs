package it.polito.mad.group27.carpooling.ui.profile.editprofile

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.group27.carpooling.Profile

class EditProfileViewModel(application: Application): AndroidViewModel(application) {

    lateinit var profile:Profile

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    fun updateProfile() {
        db.collection("users").document(auth.currentUser.uid).set(profile).addOnFailureListener{
            Toast.makeText(
                getApplication<Application>().applicationContext,
                "Error in saving profile", //TODO TRANSLATE
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}


