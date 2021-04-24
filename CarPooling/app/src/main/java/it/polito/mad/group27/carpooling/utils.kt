package it.polito.mad.group27.carpooling

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.io.OutputStream

fun Fragment.getLogTag(): String {
    return getString(R.string.log_tag)
}

fun AppCompatActivity.getLogTag(): String {
    return getString(R.string.log_tag)
}

fun OutputStream.writeBitmap(
    bitmap: Bitmap,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100
) {
    use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}