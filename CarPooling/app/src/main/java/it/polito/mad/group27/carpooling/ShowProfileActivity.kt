package it.polito.mad.group27.carpooling

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.OutputStream


fun AppCompatActivity.getLogTag(): String {
    return getString(R.string.log_tag)
}

class ShowProfileActivity : AppCompatActivity() {

    private var profile = Profile()
    private lateinit var profileImageView: ImageView
    private lateinit var fullNameView: TextView
    private lateinit var nickNameView: TextView
    private lateinit var emailView: TextView
    private lateinit var locationView: TextView
    private lateinit var registrationDateView: TextView
    private lateinit var reputationBar: RatingBar

    enum class RequestCodes {
        EDIT_PROFILE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        // if something in storage -> set it
        val savedProfileJson = getPreferences(MODE_PRIVATE)
            .getString(getString(R.string.saved_profile_preference), null)
        if (savedProfileJson != null) try {
            profile = Json.decodeFromString(savedProfileJson)
        } catch (e: SerializationException) {
            Log.d(getLogTag(), "Cannot parse saved preference profile")
        }

        profileImageView = findViewById(R.id.imageProfileView)
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

        startActivityForResult(editIntent, RequestCodes.EDIT_PROFILE.ordinal)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RequestCodes.EDIT_PROFILE.ordinal -> {
                Log.d(
                    getLogTag(),
                    "returned $resultCode with ${
                        data?.getParcelableExtra<Profile>("group27.lab1.profileresult")?.toString()
                    }"
                )
                if (resultCode == Activity.RESULT_OK) {
                    val newProfile = data?.getParcelableExtra<Profile>("group27.lab1.profileresult")
                    if (profile != newProfile && newProfile != null) {
                        profile = newProfile
                        updateFields()

                        val sharedPref = getPreferences(MODE_PRIVATE) ?: return
                        with(sharedPref.edit()) {
                            putString(
                                getString(R.string.saved_profile_preference), Json.encodeToString(
                                    profile
                                )
                            )
                            apply()
                        }
                    }
                    updateFields()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun updateFields() {

        if (File(filesDir, getString(R.string.profile_image)).exists()) {
            val profileImageFile = File(filesDir, getString(R.string.profile_image))
            val bitmap = BitmapFactory.decodeFile(profileImageFile.absolutePath)
            profileImageView.setImageBitmap(bitmap)
        }

        fullNameView.text = profile.fullName
        nickNameView.text = profile.nickName
        emailView.text = profile.email
        locationView.text = profile.location
        registrationDateView.text = profile.registrationDate
        reputationBar.rating = profile.rating
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("profile", profile)
        Log.d(getLogTag(), "saved to bundle: $profile")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d(getLogTag(), "got from bundle: $profile")
        profile = savedInstanceState.getParcelable("profile") ?: Profile()
    }
    
    private fun OutputStream.writeBitmap(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100
    ) {
        use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }

}