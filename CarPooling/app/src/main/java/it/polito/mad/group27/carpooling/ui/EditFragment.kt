package it.polito.mad.group27.carpooling.ui

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.canhub.cropper.CropImage
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.getLogTag

open class EditFragment(layoutId: Int,
                        optionsMenuId: Int,
                        titleId: Int?): BaseFragmentWithToolbar(layoutId, optionsMenuId, titleId) {

    protected var imageUri: Uri? = null
    protected var image: Bitmap? = null
    protected lateinit var imageView: ImageView
    protected var imageChanged: Boolean = false


    private enum class RequestCodes {
        PERMISSION_CAMERA,
        PERMISSION_STORAGE,
        TAKE_PHOTO,
        SELECT_IMAGE_IN_ALBUM
    }

    protected fun checkCameraPermissionAndTakePhoto() {
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
                ActivityCompat.requestPermissions(
                    act,
                    arrayOf(Manifest.permission.CAMERA),
                    RequestCodes.PERMISSION_CAMERA.ordinal
                )
            else
                ActivityCompat.requestPermissions(
                    act,
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
            ActivityCompat.requestPermissions(
                act,
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
        when (requestCode) {
            RequestCodes.PERMISSION_CAMERA.ordinal -> {
                if (grantResults.isEmpty() || grantResults.filter { it != PackageManager.PERMISSION_GRANTED }
                        .count() > 0) {
                    Log.d(getLogTag(), "camera permission has been denied by user")
                    //TODO change to material
                    Toast.makeText(
                        act,
                        getString(R.string.toast_camera_permission_settings),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.d(getLogTag(), "camera permission has been granted by user")
                    takePhoto()
                }
            }
            RequestCodes.PERMISSION_STORAGE.ordinal -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.d(getLogTag(), "storage permission has been denied by user")
                    // TODO change to material
                    Toast.makeText(
                        act,
                        getString(R.string.toast_storage_permission_settings),
                        Toast.LENGTH_SHORT
                    ).show()
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
        intent.putExtra("android.intent.extras.CAMERA_FACING", 1)
        intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
        intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
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
                    image = MediaStore.Images.Media.getBitmap(act.contentResolver, result?.uriContent)
                    imageView.setImageURI(result?.uriContent)
                    imageChanged = true
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    val error = result!!.error
                    //TODO set toast
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}