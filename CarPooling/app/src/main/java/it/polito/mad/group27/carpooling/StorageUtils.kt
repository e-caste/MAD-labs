package it.polito.mad.group27.carpooling

import android.graphics.Bitmap
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream



fun uploadBitmap(bitmap: Bitmap, filename:String, baseDir: String ="." , callback: (String?)->Unit) {
    //TODO add listeners for errors
    val storage = FirebaseStorage.getInstance()
    val reference = storage.reference.child(baseDir).child(filename)
    reference.putBytes(bitmap.convertToByteArray()).addOnSuccessListener {
        it.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri->  callback( uri.toString()) }
    }
}

fun deleteImage( filename:String, baseDir: String ="." , callback: (String?)->Unit){
    val storage = FirebaseStorage.getInstance()
    storage.reference.child(baseDir).child(filename).delete().addOnSuccessListener { callback(null) }
}

/**
 * Convert bitmap to byte array using ByteBuffer.
 */
fun Bitmap.convertToByteArray(): ByteArray {
    val baos = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    return baos.toByteArray()

}