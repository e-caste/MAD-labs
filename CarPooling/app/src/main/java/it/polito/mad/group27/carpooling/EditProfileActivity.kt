package it.polito.mad.group27.carpooling

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton

class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val profile = intent.getSerializableExtra("group27.lab1.profile") as Profile  // use as? if null can be received
        Log.d(getLogTag(), "received object of class ${profile::class.java}: $profile")

        val imageButton = findViewById<ImageButton>(R.id.imageProfileButton)
        val fullNameEdit = findViewById<EditText>(R.id.fullNameEdit)
        val nickNameEdit = findViewById<EditText>(R.id.nicknameEdit)
        val emailEdit = findViewById<EditText>(R.id.emailEdit)
        val locationEdit = findViewById<EditText>(R.id.locationEdit)

        imageButton.setOnClickListener {
            Log.d(getLogTag(), "image button clicked")
        }
    }
}