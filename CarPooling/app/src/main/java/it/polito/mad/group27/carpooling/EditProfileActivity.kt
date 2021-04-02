package it.polito.mad.group27.carpooling

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.lyrebirdstudio.croppylib.Croppy
import java.io.File
import java.io.OutputStream
import com.lyrebirdstudio.croppylib.main.CropRequest
import java.io.ByteArrayOutputStream


class EditProfileActivity : AppCompatActivity() {


    private lateinit var profileImage: Bitmap
    private var profileImageChanged = false
    private lateinit var profile: Profile
    private lateinit var imageProfileView: ImageView
    private lateinit var imageButton: ImageButton
    private lateinit var fullNameEdit: EditText
    private lateinit var nickNameEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var locationEdit: EditText


    private enum class RequestCodes {
        TAKE_PHOTO,
        SELECT_IMAGE_IN_ALBUM,
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        profile = intent.getParcelableExtra<Profile>("group27.lab1.profile") ?: Profile()
        Log.d(getLogTag(), "received object of class ${profile::class.java}: $profile")

        imageProfileView = findViewById(R.id.imageProfileView)
        imageButton = findViewById(R.id.imageProfileButton)
        fullNameEdit = findViewById(R.id.fullNameEdit)
        nickNameEdit = findViewById(R.id.nicknameEdit)
        emailEdit = findViewById(R.id.emailEdit)
        locationEdit = findViewById(R.id.locationEdit)

        //TODO get profile image filename through function (or even file)
        val profileImageFile =  File(filesDir,"profile.png")
        if(profileImageFile.exists() && profileImageFile.absolutePath != null) {
            profileImage = BitmapFactory.decodeFile(profileImageFile.absolutePath)
            imageProfileView.setImageBitmap(profileImage)
        }
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
            R.id.camera -> {
                Log.d(getLogTag(), "taking picture...")
                takePhoto()
                return true
            }
            R.id.gallery -> {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            RequestCodes.TAKE_PHOTO.ordinal -> {
                Log.d(getLogTag(), "returned $resultCode from camera with ${data ?: "no image"}")
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val profileImageTmp = data.extras!!.get("data") as Bitmap

                    openFileOutput("profile_tmp.png", Context.MODE_PRIVATE).use {
                        it.writeBitmap(profileImageTmp)
                    }
                    val manualCropRequest = CropRequest.Manual(
                        sourceUri = File(filesDir,"profile_tmp.png").toUri(),
                        destinationUri = File(filesDir,"profile_tmp.png").toUri(),
                        requestCode = 101
                    )
                    Croppy.start(this, cropRequest = manualCropRequest)
//                    imageProfileView.setImageBitmap(profileImage)
//                    profileImageChanged = true

                }
            }
            RequestCodes.SELECT_IMAGE_IN_ALBUM.ordinal -> {
                Log.d(getLogTag(), "returned $resultCode from gallery with ${data ?: "no image"}")
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val manualCropRequest = CropRequest.Manual(
                            sourceUri = data.data!!,
                            destinationUri = File(filesDir,"profile_tmp.png").toUri(),
                            requestCode = 101
                    )
//                    imageProfileView.setImageURI(data.data)
                    Croppy.start(this, cropRequest = manualCropRequest)
                }
            }
            101 -> {
                Log.d(getLogTag(), "returned $resultCode from croppy with ${data ?: "no image"}")
                if (resultCode == Activity.RESULT_OK && data != null){
                    profileImage = MediaStore.Images.Media.getBitmap(contentResolver, data.data)
                    imageProfileView.setImageBitmap(profileImage)
                    profileImageChanged = true
                    }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun OutputStream.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100) {
        use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }

    private fun saveProfile() {
        if (profileImageChanged){

            openFileOutput("profile.png", Context.MODE_PRIVATE).use {
                it.writeBitmap(profileImage)
            }
        }
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