package it.polito.mad.group27.carpooling

import android.app.Activity
import android.content.ContentValues
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
import com.lyrebirdstudio.aspectratiorecyclerviewlib.aspectratio.model.AspectRatio
import com.lyrebirdstudio.croppylib.Croppy
import com.lyrebirdstudio.croppylib.main.CropRequest
import java.io.File
import java.io.OutputStream


class EditProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: Bitmap
    private var imageUri: Uri? = null
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
        CROP_IMAGE,
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

        val profileImageFile = File(filesDir, getString(R.string.profile_image))
        if (profileImageFile.exists() && profileImageFile.absolutePath != null) {
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

        fullNameEdit.addTextChangedListener(Watcher(
            { fullNameEdit.text.isEmpty() || fullNameEdit.text.trim().split("\\s+".toRegex()).size < 2 },
            { fullNameEdit.error = "You must insert both your name and your surname"
                invalidateOptionsMenu()
            },
            { fullNameEdit.error = null
                invalidateOptionsMenu()
            }
        ))

        nickNameEdit.addTextChangedListener(Watcher(
            { nickNameEdit.text.length < 4 },
            { nickNameEdit.error = "Nickname must be at least 4 characters long"
                invalidateOptionsMenu()
            },
            { nickNameEdit.error = null
                invalidateOptionsMenu()
            }
        ))

        emailEdit.addTextChangedListener(Watcher(
            { emailEdit.text.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailEdit.text).matches() },
            { emailEdit.error = "You must insert an e-mail address"
                invalidateOptionsMenu()
            },
            { emailEdit.error = null
                invalidateOptionsMenu()
            }
        ))

        locationEdit.addTextChangedListener(Watcher(
            //TODO check location format
            { locationEdit.text.isEmpty() },
            { locationEdit.error = "You must insert a location"
                invalidateOptionsMenu()
            },
            { locationEdit.error = null
                invalidateOptionsMenu()
            }
        ))
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

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)
        menu!!.findItem(R.id.save_profile).isEnabled = validateFields()
        return true
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
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        imageUri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        )
        Log.d(getLogTag(), "$imageUri")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        intent.putExtra("android.intent.extras.CAMERA_FACING", 1)
        intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
        intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
        startActivityForResult(intent, RequestCodes.TAKE_PHOTO.ordinal)
    }

    private fun selectImageInAlbum() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        Log.d(getLogTag(), "selectedImageInAlbum")
        startActivityForResult(intent, RequestCodes.SELECT_IMAGE_IN_ALBUM.ordinal)
    }

    private fun runCroppy(sourceUri: Uri) {
        val manualCropRequest = CropRequest.Manual(
            sourceUri = sourceUri,
            destinationUri = File(filesDir, getString(R.string.profile_image_tmp)).toUri(),
            requestCode = RequestCodes.CROP_IMAGE.ordinal,
            excludedAspectRatios = arrayListOf(
                AspectRatio.ASPECT_FREE,
                AspectRatio.ASPECT_INS_4_5,
                AspectRatio.ASPECT_INS_STORY,
                AspectRatio.ASPECT_5_4,
                AspectRatio.ASPECT_3_4,
                AspectRatio.ASPECT_4_3,
                AspectRatio.ASPECT_FACE_POST,
                AspectRatio.ASPECT_FACE_COVER,
                AspectRatio.ASPECT_PIN_POST,
                AspectRatio.ASPECT_3_2,
                AspectRatio.ASPECT_9_16,
                AspectRatio.ASPECT_16_9,
                AspectRatio.ASPECT_1_2,
                AspectRatio.ASPECT_YOU_COVER,
                AspectRatio.ASPECT_TWIT_POST,
                AspectRatio.ASPECT_TWIT_HEADER,
                AspectRatio.ASPECT_A_4,
                AspectRatio.ASPECT_A_5,
            )
        )
        Croppy.start(this, cropRequest = manualCropRequest)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RequestCodes.TAKE_PHOTO.ordinal -> {
                Log.d(
                    getLogTag(),
                    "returned $resultCode from camera with ${imageUri ?: "no image"}"
                )
                if (resultCode == Activity.RESULT_OK && imageUri != null) {
                    runCroppy(imageUri!!)
                }
            }
            RequestCodes.SELECT_IMAGE_IN_ALBUM.ordinal -> {
                Log.d(getLogTag(), "returned $resultCode from gallery with ${data ?: "no image"}")
                if (resultCode == Activity.RESULT_OK && data != null) {
                    runCroppy(data.data!!)
                }
            }
            RequestCodes.CROP_IMAGE.ordinal -> {
                Log.d(getLogTag(), "returned $resultCode from croppy with ${data ?: "no image"}")
                if (resultCode == Activity.RESULT_OK && data != null) {
                    profileImage = MediaStore.Images.Media.getBitmap(contentResolver, data.data)
                    imageProfileView.setImageBitmap(profileImage)
                    profileImageChanged = true
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
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

    private fun validateFields() : Boolean {
       return fullNameEdit.text.isNotEmpty()
               && fullNameEdit.text.trim().split("\\s+".toRegex()).size >= 2
               && nickNameEdit.text.length >= 4
               && emailEdit.text.isNotEmpty()
               && android.util.Patterns.EMAIL_ADDRESS.matcher(emailEdit.text).matches()
               && locationEdit.text.isNotEmpty()
    }

    private fun saveProfile() {
        if (profileImageChanged) {
            openFileOutput(getString(R.string.profile_image), Context.MODE_PRIVATE).use {
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("profileImageChanged", profileImageChanged)
        Log.d(getLogTag(), "saved to bundle: $profileImageChanged")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        profileImageChanged = savedInstanceState.getBoolean("profileImageChanged")
        Log.d(getLogTag(), "got from bundle: $profileImageChanged")
        if (profileImageChanged) {
            val profileImageFile = File(filesDir, getString(R.string.profile_image_tmp))
            profileImage = BitmapFactory.decodeFile(profileImageFile.absolutePath)
            imageProfileView.setImageBitmap(profileImage)
        }
    }
}