package it.polito.mad.group27.carpooling

import android.app.Application
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel(application: Application) : ProfileBaseViewModel(application) {

    companion object{
        fun updateUserNotificationToken(notificationToken:String){
            val user = FirebaseAuth.getInstance().currentUser
            if(user !=null){
                FirebaseFirestore.getInstance().collection("users")
                    .document(user.uid).update("notificationToken", notificationToken)

            }
            token = notificationToken
        }

        var token:String? = null
    }

    private lateinit var profileDocument:DocumentReference
    private val db = FirebaseFirestore.getInstance()


    fun loadProfile(currentUser: FirebaseUser) {
        profileDocument = db.collection("users").document(currentUser.uid)
        profileDocument.get().addOnSuccessListener {

            if (!it.exists()) {
                profileDocument.set(
                    Profile(
                        currentUser.uid,
                        currentUser.photoUrl?.toString(),
                        currentUser.displayName ?: "",
                        email = currentUser.email ?: "",
                        notificationToken = token
                    )
                ).addOnFailureListener {
                    Toast.makeText(
                        getApplication<Application>().applicationContext,
                        "Error in saving profile",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }else
                // updating notification token if needed
                token?.let { t -> updateUserNotificationToken(t) }
        }.addOnFailureListener {
            Toast.makeText(
                getApplication<Application>().applicationContext,
                "Error in loading profile",
                Toast.LENGTH_SHORT
            ).show()
        }

        profileDocument.addSnapshotListener{ value, e ->
            if(e!=null){
                //TODO manage errors
            }else {
                profile.value = value!!.toObject(Profile::class.java)
            }
        }
    }

}

