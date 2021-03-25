package it.polito.mad.group27.carpooling

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

fun AppCompatActivity.getLogTag(): String {
    return getString(R.string.log_tag)
}

class ShowProfileActivity : AppCompatActivity() {

    private val profile = Profile()

    enum class RequestCodes {
        EDIT_PROFILE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        // if something in storage -> set it
        // TODO get data from storage
        // else use defaults from strings.xml
        profile.fullName = getString(R.string.default_fullname)
        profile.nickName = getString(R.string.default_nickname)
        profile.email = getString(R.string.default_email)
        profile.location = getString(R.string.default_location)
        profile.registrationDate = getString(R.string.default_registration_date)
        profile.rating = getString(R.string.default_rating).toFloat()
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
}