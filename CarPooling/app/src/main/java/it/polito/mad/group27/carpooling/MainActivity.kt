package it.polito.mad.group27.carpooling

import android.R.drawable
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.provider.MediaStore.Images.Media.getBitmap
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var navView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var profile: Profile
    var profileImage: Bitmap?= null
    lateinit var navHeader: View
    lateinit var profileImageView: ImageView
    lateinit var profileNameTextView: TextView
    lateinit var profileEmailTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth

        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)

        navView = findViewById(R.id.nav_view)
        val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)!!.findNavController()
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_trips, R.id.nav_profile),
            drawerLayout
        )
        navView.setupWithNavController(navController)

        navHeader = navView.getHeaderView(0)
        profileImageView = navHeader.findViewById(R.id.drawer_profile_image_view)
        profileNameTextView = navHeader.findViewById(R.id.drawer_profile_name_text_view)
        profileEmailTextView = navHeader.findViewById(R.id.drawer_profile_email_text_view)

    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser

        if(currentUser == null){
            signIn()
        } else {
            Log.d(TAG,"current user: ${currentUser.uid}, ${currentUser.displayName}, ${currentUser.email}, ${currentUser.photoUrl}")
            loadProfile(Profile(currentUser.displayName,currentUser.displayName,currentUser.email))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    //updateUI(null)
                }
            }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    companion object {
        private const val TAG = "MAD-group-27"
        private const val RC_SIGN_IN = 9001
    }

    fun loadProfile(_profile: Profile? = null){
        if(_profile!=null){
            profile = _profile
        }else {        // if something in storage -> set it
            val savedProfileJson = getPreferences(MODE_PRIVATE)
                ?.getString(getString(R.string.saved_profile_preference), null)
            if (savedProfileJson != null) {
                try {
                    profile = Json.decodeFromString(savedProfileJson)
                } catch (e: SerializationException) {
                    Log.d(getLogTag(), "Cannot parse saved preference profile")
                }
            } else {
                profile = Profile()
            }
        }


        if (File(filesDir, getString(R.string.profile_image)).exists()) {
            val profileImageFile = File(filesDir, getString(R.string.profile_image))
            profileImage = BitmapFactory.decodeFile(profileImageFile.absolutePath)
            Log.d(getLogTag(), "image is set, using user selected image...")
        } else {
            profileImage = null
            Log.d(getLogTag(), "image is removed, setting default icon...")
        }

        // updating drawer informations
        if (profileImage!=null)
            profileImageView.setImageBitmap(profileImage)
        else
            profileImageView.setImageResource(R.drawable.ic_baseline_person_24)
        profileNameTextView.text = profile.fullName
        profileEmailTextView.text = profile.email
    }
}