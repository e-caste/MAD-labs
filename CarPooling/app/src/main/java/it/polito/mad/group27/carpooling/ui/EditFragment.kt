package it.polito.mad.group27.carpooling.ui

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.canhub.cropper.CropImage
import com.google.android.material.snackbar.Snackbar
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.profile.editprofile.EditProfileFragment
import it.polito.mad.group27.carpooling.writeBitmap
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

open class EditFragment(layoutId: Int,
                        optionsMenuId: Int,
                        titleId: Int?): BaseFragmentWithToolbar(layoutId, optionsMenuId, titleId) {

    private var imageUri: Uri? = null
    private var imageChanged: Boolean = false
    protected var image: Bitmap? = null
    protected lateinit var imageView: ImageView


    private enum class RequestCodes {
        PERMISSION_CAMERA,
        PERMISSION_STORAGE,
        TAKE_PHOTO,
        SELECT_IMAGE_IN_ALBUM
    }

    private fun checkCameraPermissionAndTakePhoto() {
        val cameraPermission = ContextCompat.checkSelfPermission(act, Manifest.permission.CAMERA)
        val writePermission =
            ContextCompat.checkSelfPermission(act, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (cameraPermission == PackageManager.PERMISSION_GRANTED &&
            (writePermission == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT > 28)) {
            Log.d(getLogTag(), "camera permission is already granted, not asking user...")
            takePhoto()
        } else {
            Log.d(getLogTag(), "asking user for permission to use camera...")
            if (Build.VERSION.SDK_INT > 28)
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    RequestCodes.PERMISSION_CAMERA.ordinal
                )
            else
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    RequestCodes.PERMISSION_CAMERA.ordinal
                )
        }
    }

    protected fun checkStoragePermissionAndGetPhoto() {
        val storagePermission =
            ContextCompat.checkSelfPermission(act, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (storagePermission == PackageManager.PERMISSION_GRANTED) {
            Log.d(getLogTag(), "storage permission is already granted, not asking user...")
            selectImageInAlbum()
        } else {
            Log.d(getLogTag(), "asking user for permission to access storage...")
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                RequestCodes.PERMISSION_STORAGE.ordinal
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(getLogTag(), "Permissions handler")
        when (requestCode) {
            RequestCodes.PERMISSION_CAMERA.ordinal -> {
                if (grantResults.isEmpty() || grantResults.filter { it != PackageManager.PERMISSION_GRANTED }
                        .count() > 0) {
                    Log.d(getLogTag(), "camera permission has been denied by user")

                    Snackbar.make(requireView(),  getString(R.string.toast_camera_permission_settings), Snackbar.LENGTH_LONG)
                        .setAction("Retry") {
                            // Responds to click on the action
                            checkCameraPermissionAndTakePhoto()
                        }
                        .show()
                } else {
                    Log.d(getLogTag(), "camera permission has been granted by user")
                    takePhoto()
                }
            }
            RequestCodes.PERMISSION_STORAGE.ordinal -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.d(getLogTag(), "storage permission has been denied by user")

                    Snackbar.make(requireView(),  getString(R.string.toast_storage_permission_settings), Snackbar.LENGTH_LONG)

                        .setAction(getString(R.string.retry)) {
                            // Responds to click on the action
                            checkStoragePermissionAndGetPhoto()
                        }
                        .show()
                } else {
                    Log.d(getLogTag(), "storage permission has been granted by user")
                    selectImageInAlbum()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun takePhoto() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        imageUri = act.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        )
        Log.d(getLogTag(), "$imageUri")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        if(this is EditProfileFragment) {
            intent.putExtra("android.intent.extras.CAMERA_FACING", 1)
            intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
        }else{
            intent.putExtra("android.intent.extras.CAMERA_FACING", 0)
            intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 0)
            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", false)
        }
        startActivityForResult(intent, RequestCodes.TAKE_PHOTO.ordinal)
    }

    private fun selectImageInAlbum() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        Log.d(getLogTag(), "selectedImageInAlbum")
        startActivityForResult(
            intent,
            RequestCodes.SELECT_IMAGE_IN_ALBUM.ordinal
        )
    }

    private fun runCropper(sourceUri: Uri) {
        CropImage
            .activity(sourceUri)
            .setAspectRatio(4, 3)
            .setRequestedSize(800, 600)
            .start(requireContext(), this)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RequestCodes.TAKE_PHOTO.ordinal -> {
                Log.d(
                    getLogTag(),
                    "returned $resultCode from camera with ${imageUri ?: "no image"}"
                )
                if (resultCode == Activity.RESULT_OK && imageUri != null) {
                    runCropper(imageUri!!)
                }
            }
            RequestCodes.SELECT_IMAGE_IN_ALBUM.ordinal -> {
                Log.d(getLogTag(), "returned $resultCode from gallery with ${data ?: "no image"}")
                if (resultCode == Activity.RESULT_OK && data != null) {
                    runCropper(data.data!!)
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ->{
                Log.d(getLogTag(), "returned $resultCode from cropper with ${data ?: "no image"}")
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    val resultUri: Uri? = result?.uriContent
                    image = MediaStore.Images.Media.getBitmap(act.contentResolver, resultUri)
                    imageView.setImageURI(result?.uriContent)
                    imageChanged = true
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Snackbar.make(requireView(), getString(R.string.crop_error),
                        Snackbar.LENGTH_LONG)
                        .show()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }


    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = act.menuInflater
        inflater.inflate(R.menu.select_image_source_menu, menu)
        if (image != null) {
            var deleteItem = menu?.findItem(R.id.delete)
            if (deleteItem != null) {
                deleteItem.isVisible = true
            }
        }
        Log.d(getLogTag(), "context menu created")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.camera -> {
                Log.d(getLogTag(), "taking picture...")
                checkCameraPermissionAndTakePhoto()
                return true
            }
            R.id.gallery -> {
                Log.d(getLogTag(), "choosing picture from gallery...")
                checkStoragePermissionAndGetPhoto()
                return true
            }
            R.id.delete -> {
                Log.d(getLogTag(), "deleting picture...")
                if(this is EditProfileFragment)
                    imageView.setImageResource(R.drawable.ic_baseline_person_24)
                else
                    imageView.setImageResource(R.drawable.ic_baseline_directions_car_24)
                image = null
                imageChanged = true
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    protected fun saveImg(fileName: String){
        if (imageChanged) {
            if (image != null) {
                act.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                    it.writeBitmap(image!!)
                }
            } else{
                File(act.filesDir, fileName).delete()
            }
            File(act.filesDir, getString(R.string.temporary_edit_image_file))
        }
    }

    protected inline fun <reified T> writeParcelable(parcelable: T, name: String){
        val sharedPref = act.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(name, Json.encodeToString(parcelable))
            apply()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(getString(R.string.image_changed_flag), imageChanged)
        Log.d(getLogTag(), "saved to bundle: $imageChanged")
        if(imageChanged && image!=null)
            act.openFileOutput(getString(R.string.temporary_edit_image_file), Context.MODE_PRIVATE).use {
                it.writeBitmap(image!!)
            }
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        imageChanged = savedInstanceState?.getBoolean(getString(R.string.image_changed_flag)) ?: false
        Log.d(getLogTag(), "got from bundle: $imageChanged")
        if (imageChanged) {
            val imageFile = File(act.filesDir, getString(R.string.temporary_edit_image_file))
            image = if(imageFile.exists()) {
                BitmapFactory.decodeFile(imageFile.absolutePath)
            }else{
                null
            }

            imageView.setImageBitmap(image)
        }
    }
}