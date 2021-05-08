package it.polito.mad.group27.carpooling

import android.accounts.AccountManager
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.TaskExecutors
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var navView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    @Deprecated("Will be deleted")
    lateinit var profile: Profile
    @Deprecated("Will be deleted")
    var profileImage: Bitmap?= null
    lateinit var navHeader: View
    lateinit var profileImageView: ImageView
    lateinit var profileNameTextView: TextView
    lateinit var profileEmailTextView: TextView

    lateinit var profileViewModel : ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        profileViewModel.profile.observe(this, Observer { loadProfile(it) })

        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)

        navView = findViewById(R.id.nav_view)
        val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)!!.findNavController()
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_trips, R.id.nav_my_trips, R.id.nav_profile),
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
        signIn()
        if(currentUser != null){
            Log.d(TAG,"current user: ${currentUser.uid}, ${currentUser.displayName}, ${currentUser.email}, ${currentUser.photoUrl}")
            profileViewModel.loadProfile(currentUser)
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

                loadToken(account)

                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                //TODO back to loginPage
            }
        }
    }

    private fun loadToken(account: GoogleSignInAccount) {
        val am: AccountManager = AccountManager.get(this)
        val options = Bundle()

        am.getAuthToken(
            account.account,                     // Account retrieved using getAccountsByType()
            "oauth2: https://www.googleapis.com/auth/firebase.messaging",            // Auth scope
            options,                        // Authenticator-specific options
            this,                           // Your activity
            { MessagingService.oauthToken = it.result.getString(AccountManager.KEY_AUTHTOKEN)!! }, // Callback called when a token is successfully acquired
            Handler (Looper.getMainLooper()){
                Log.d(getLogTag(), "ERROR")
                // TODO back to login page
                true
            }              // Callback called if an error occurs
        )
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    profileViewModel.loadProfile(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    //TODO back to login page
                }
            }
    }

    private fun signIn() {
        if(FirebaseAuth.getInstance().currentUser==null) {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }else{
            googleSignInClient.silentSignIn().addOnSuccessListener {
                loadToken(it)
            }
        }
    }

    companion object {
        private const val TAG = "MAD-group-27"
        private const val RC_SIGN_IN = 9001
    }

    private fun loadProfile(_profile: Profile? = null){
        if (_profile!=null){
            if (_profile.profileImageUri != null)
                Glide.with(this).load(_profile.profileImageUri).into(profileImageView)
            else
                profileImageView.setImageResource(R.drawable.ic_baseline_person_24)
            profileNameTextView.text = _profile.fullName
            profileEmailTextView.text = _profile.email
        }
    }
}