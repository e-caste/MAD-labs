package it.polito.mad.group27.carpooling

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

class MainActivity : AppCompatActivity() {

    lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var navView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var profile: Profile
    lateinit var profileImage: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)


        navView = findViewById(R.id.nav_view)
        val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)!!.findNavController()
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_trips, R.id.nav_profile ), drawerLayout)
        navView.setupWithNavController(navController)

        loadProfile()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun loadProfile(){
        // if something in storage -> set it
        val savedProfileJson = getPreferences(MODE_PRIVATE)
            ?.getString(getString(R.string.saved_profile_preference), null)
        if (savedProfileJson != null) {
            try {
                profile = Json.decodeFromString(savedProfileJson)
            } catch (e: SerializationException) {
                Log.d(getLogTag(), "Cannot parse saved preference profile")
            }
        }else{
            profile = Profile()
        }


        if (File(filesDir, getString(R.string.profile_image)).exists()) {
            val profileImageFile = File(filesDir, getString(R.string.profile_image))
            profileImage = BitmapFactory.decodeFile(profileImageFile.absolutePath)
            Log.d(getLogTag(), "image is set, using user selected image...")
        } else {
            profileImage = BitmapFactory.decodeResource(resources, R.drawable.ic_baseline_person_24)
            Log.d(getLogTag(), "image is removed, setting default icon...")
        }
    }
}