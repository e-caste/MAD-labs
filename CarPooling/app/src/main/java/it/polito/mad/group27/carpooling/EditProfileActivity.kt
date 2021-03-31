package it.polito.mad.group27.carpooling

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageButton

class EditProfileActivity : AppCompatActivity() {

    private lateinit var profile: Profile
    private lateinit var imageButton: ImageButton
    private lateinit var fullNameEdit : EditText
    private lateinit var nickNameEdit : EditText
    private lateinit var emailEdit : EditText
    private lateinit var locationEdit : EditText

    private enum class RequestCodes {
        TAKE_PHOTO,
        SELECT_IMAGE_IN_ALBUM,
    }

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


        registerForContextMenu(imageButton)
        imageButton.setOnClickListener {
            Log.d(getLogTag(), "image button clicked")
            openContextMenu(imageButton)
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

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.select_image_source_menu, menu)
        Log.d(getLogTag(), "context menu created")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.camera ->{
                Log.d(getLogTag(), "taking picture...")
                takePhoto()
                return true
            }
            R.id.gallery ->{
                Log.d(getLogTag(), "choosing picture from gallery...")
                selectImageInAlbum()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, RequestCodes.TAKE_PHOTO.ordinal)
        }
    }

    private fun selectImageInAlbum() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, RequestCodes.SELECT_IMAGE_IN_ALBUM.ordinal)
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