package it.polito.mad.group27.carpooling

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
        // else use defaults from Profile
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
}