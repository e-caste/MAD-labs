package it.polito.mad.group27.carpooling

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class EditViewModel(application: Application): AndroidViewModel(application) {

    var imageUri: Uri? = null
    var imageChanged: Boolean = false
    var image: Bitmap? = null
    var imagePresent : Boolean = false



    fun uploadBitmap(bitmap: Bitmap, filename:String, baseDir: String ="." , callback: (String?, Boolean)->Unit) {
        //TODO add listeners for errors
        val storage = FirebaseStorage.getInstance()
        val reference = storage.reference.child(baseDir).child(filename)
        reference.putBytes(bitmap.convertToByteArray()).addOnSuccessListener {
            it.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri->  callback( uri.toString(), true) }
        }
    }

    fun deleteImage( filename:String, baseDir: String ="." , callback: (String?, Boolean)->Unit){
        val storage = FirebaseStorage.getInstance()
        storage.reference.child(baseDir).child(filename).delete().addOnSuccessListener { callback(null, true) }
    }

}

/**
 * Convert bitmap to byte array using ByteBuffer.
 */
fun Bitmap.convertToByteArray(): ByteArray {
    val outputStream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    return outputStream.toByteArray()

}