package it.polito.mad.group27.carpooling

import android.app.Activity
import android.content.Intent
import android.media.Rating
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView

fun AppCompatActivity.getLogTag(): String {
    return getString(R.string.log_tag)
}

class ShowProfileActivity : AppCompatActivity() {

    private var profile = Profile()
    private lateinit var profileImageView: ImageView
    private lateinit var fullNameView : TextView
    private lateinit var nickNameView : TextView
    private lateinit var emailView : TextView
    private lateinit var locationView : TextView
    private lateinit var registrationDateView: TextView
    private lateinit var reputationBar: RatingBar

    enum class RequestCodes {
        EDIT_PROFILE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        // if something in storage -> set it
        // TODO get data from storage
        // else use defaults from Profile

        profileImageView = findViewById(R.id.profileImageView)
        fullNameView = findViewById(R.id.fullNameView)
        nickNameView = findViewById(R.id.nicknameView)
        emailView = findViewById(R.id.emailView)
        locationView = findViewById(R.id.locationView)
        registrationDateView = findViewById(R.id.registrationDateView)
        reputationBar = findViewById(R.id.ratingBar)

        updateFields()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.show_profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.edit_profile -> {
                editProfile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun editProfile() {
        Log.d(getLogTag(), "edit button clicked")

        val editIntent = Intent(this, EditProfileActivity::class.java)
            .also { it.putExtra("group27.lab1.profile", profile) }

//        Json.decodeFromString<Profile>(Json.encodeToString(profile))
        startActivityForResult(editIntent, RequestCodes.EDIT_PROFILE.ordinal)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            RequestCodes.EDIT_PROFILE.ordinal -> {
                Log.d(getLogTag(), "returned $resultCode with ${data?.getParcelableExtra<Profile>("group27.lab1.profileresult")?.toString()}")
                if(resultCode == Activity.RESULT_OK){
                    val newProfile = data?.getParcelableExtra<Profile>("group27.lab1.profileresult")
                    if(profile != newProfile && newProfile != null){
                        profile = newProfile
                        updateFields()
                        //TODO SharedPreferences

                    }

                    updateFields()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun updateFields() {
        //TODO img
        fullNameView.text = profile.fullName
        nickNameView.text = profile.nickName
        emailView.text = profile.email
        locationView.text = profile.location
        registrationDateView.text = profile.registrationDate
        reputationBar.rating = profile.rating
    }
}