package it.polito.mad.group27.carpooling

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton

class EditProfileActivity : AppCompatActivity() {

    private lateinit var profile: Profile
    private lateinit var imageButton: ImageButton
    private lateinit var fullNameEdit : EditText
    private lateinit var nickNameEdit : EditText
    private lateinit var emailEdit : EditText
    private lateinit var locationEdit : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        profile = intent.getParcelableExtra<Profile>("group27.lab1.profile") ?: Profile()
        Log.d(getLogTag(), "received object of class ${profile::class.java}: $profile")

        imageButton = findViewById(R.id.imageProfileButton)
        fullNameEdit = findViewById(R.id.fullNameEdit)
        nickNameEdit = findViewById(R.id.nicknameEdit)
        emailEdit = findViewById(R.id.emailEdit)
        locationEdit = findViewById(R.id.locationEdit)

        // TODO img
        fullNameEdit.setText(profile.fullName)
        nickNameEdit.setText(profile.nickName)
        emailEdit.setText(profile.email)
        locationEdit.setText(profile.location)

        imageButton.setOnClickListener {
            Log.d(getLogTag(), "image button clicked")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.edit_profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.save_profile -> {
                saveProfile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveProfile() {
        //TODO img
        profile.fullName = fullNameEdit.text.toString()
        profile.nickName = nickNameEdit.text.toString()
        profile.email = emailEdit.text.toString()
        profile.location = locationEdit.text.toString()

        val intent = Intent().also {
            it.putExtra("group27.lab1.profileresult", profile)
        }

        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}