package it.polito.mad.group27.hubert

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import it.polito.mad.group27.hubert.entities.Profile
import org.osmdroid.config.Configuration


class MainActivity : AppCompatActivity() {

    lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navHeader: View
    private lateinit var profileImageView: ImageView
    private lateinit var profileNameTextView: TextView
    private lateinit var profileEmailTextView: TextView

    private lateinit var profileViewModel : ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        profileViewModel.profile.observe(this, { loadProfile(it) })

        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)

        navView = findViewById(R.id.nav_view)
        val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)!!.findNavController()
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.othersTripList,
                R.id.tripsOfInterestList,
                R.id.boughtTripList,
                R.id.nav_trips,
                R.id.nav_profile,
            ),
            drawerLayout
        )
        navView.setupWithNavController(navController)

        navHeader = navView.getHeaderView(0)
        profileImageView = navHeader.findViewById(R.id.drawer_profile_image_view)
        profileNameTextView = navHeader.findViewById(R.id.drawer_profile_name_text_view)
        profileEmailTextView = navHeader.findViewById(R.id.drawer_profile_email_text_view)

        Configuration.getInstance().userAgentValue = this.packageName

    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser!!

        Log.d(TAG,"current user: ${currentUser.uid}, ${currentUser.displayName}, ${currentUser.email}, ${currentUser.photoUrl}")
        profileViewModel.loadProfile(currentUser)

    }

    override fun onSupportNavigateUp(): Boolean {
        currentFocus?.clearFocus()
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
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