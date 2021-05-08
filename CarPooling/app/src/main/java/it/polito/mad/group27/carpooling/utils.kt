package it.polito.mad.group27.carpooling

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.Timestamp
import java.io.OutputStream
import it.polito.mad.group27.carpooling.ui.trip.Option
import it.polito.mad.group27.carpooling.ui.trip.Stop
import it.polito.mad.group27.carpooling.ui.trip.Trip
import it.polito.mad.group27.carpooling.ui.trip.triplist.TripList
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random.Default.nextDouble

const val TAG = "MAD-group-27"

fun Fragment.getLogTag() = TAG
fun AppCompatActivity.getLogTag() = TAG
fun AndroidViewModel.getLogTag() = TAG
fun OutputStream.getLogTag() = TAG

fun OutputStream.writeBitmap(
    bitmap: Bitmap,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100,
    targetHeight: Int? = null,
) {
    use { out ->
        if (targetHeight != null) {
            Log.d(getLogTag(), "scaling bitmap: ${bitmap.width}x${bitmap.height} -> ${targetHeight * bitmap.width / bitmap.height}x$targetHeight")
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetHeight * bitmap.width / bitmap.height, targetHeight, true)
            scaledBitmap.compress(format, quality, out)
        } else {
            bitmap.compress(format, quality, out)
        }
        out.flush()
    }
}

fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? {
    var drawable = ContextCompat.getDrawable(context, drawableId)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        drawable = DrawableCompat.wrap(drawable!!).mutate()
    }
    val bitmap = Bitmap.createBitmap(
        drawable!!.intrinsicWidth,
        drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun TimestampToCalendar(dateTime: Timestamp): Calendar =
    Calendar.getInstance().also { it.timeInMillis = dateTime.seconds * 1000 }

fun CalendarToTimestamp(dateTime: Calendar): Timestamp =
    Timestamp(dateTime.time)
