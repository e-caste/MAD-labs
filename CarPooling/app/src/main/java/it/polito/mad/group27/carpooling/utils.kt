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
import java.io.OutputStream
import it.polito.mad.group27.carpooling.ui.trip.Hour
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
import kotlin.math.round
import kotlin.random.Random.Default.nextDouble


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

// used for car images, sine converting to PNG is too slow
fun OutputStream.writeBitmapJPEG(
    bitmap: Bitmap,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    quality: Int = 100,
    targetHeight: Int = 720,
) {
    use { out ->
        Log.d("MAD-group-27", "${bitmap.width}x${bitmap.height} -> ${targetHeight * bitmap.width / bitmap.height}x$targetHeight")
        val bm = Bitmap.createScaledBitmap(bitmap, targetHeight * bitmap.width / bitmap.height, targetHeight, true)
        bm.compress(format, quality, out)
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

fun TripList.createSampleDataIfNotPresent(tripsNumber: Int = 20, forceReset: Boolean = false) {

    val carImages = listOf(
        R.drawable.audi_a6,
        R.drawable.bmw_x6,
        R.drawable.citroen_c4,
        R.drawable.dmc_delorean,
        R.drawable.fiat_500,
        R.drawable.fiat_500l,
        R.drawable.fiat_punto,
        R.drawable.ford_f150,
        R.drawable.ford_fiesta,
        R.drawable.ford_focus,
        R.drawable.lamborghini_urus,
        R.drawable.lancia_y,
        R.drawable.mercedes_cla45_amg,
        R.drawable.mini_cooper,
        R.drawable.nissan_gt_r,
        R.drawable.porsche_panamera,
        R.drawable.smart_fortwo,
        R.drawable.tesla_cybertruck,
        R.drawable.tesla_model_3,
        R.drawable.tesla_model_s,
    )
    val days = (1..31)
    val hours = (0..23)
    val minutes = (0..59)
    val priceUntil = 10000.0
    val places = listOf("Torino Centro", "Milano Porta Garibaldi", "Ancona", "La Spezia Manarola", "New New York", "Sacred Heart Hospital", "Springfield", "Marte", "Giza")

    fun getRandomImageUri() = File(activity?.filesDir, "${carImagePrefix}${carImages.indices.random()}").toUri()

    // Date is a deprecated class. I can see why...
    fun getRandomDate() = Date(2021 - 1900, 4, days.random())

    // BigDecimals are evil and I'm not sure we need them
    fun getRandomPrice() = BigDecimal("%.2f".format(nextDouble(priceUntil)).replace(",", ".").toDouble()).setScale(2, RoundingMode.HALF_EVEN)

    fun getRandomHour() = Hour(hours.random(), minutes.random())

    fun getRandomStops(): MutableList<Stop> {
        val res = mutableListOf<Stop>()
        for (i in 0..(0..10).random()) {
            res.add(Stop(places.random(), getRandomHour()))
        }
        return res
    }

    fun getRandomOptions(): MutableList<Option> {
        val res = mutableListOf<Option>()
        for (i in 0..(0..Option.values().size).random()) {
            val tmp = Option.values().random()
            if (!res.contains(tmp))
                res.add(tmp)
        }
        return res
    }

    fun getRandomTrip(id: Long) = Trip(
        id = id,
        carImageUri = getRandomImageUri(),
        date = getRandomDate(),
        totalSeats = (1..6).random(),
        availableSeats = 1,
        price = getRandomPrice(),
        startHour = getRandomHour(),
        endHour = getRandomHour(),
        from = places.random(),
        to = places.random(),
        stops = getRandomStops(),
        options = getRandomOptions(),
    )

    // TODO: check for EXTERNAL_STORAGE permission here
    fun saveCarImagesToStorage() {
        if (!File(activity?.filesDir, "${carImagePrefix}0").exists() || forceReset) {
            for ((i, img) in carImages.withIndex()) {
                val bitmap = BitmapFactory.decodeResource(resources, img)
                activity?.openFileOutput("$carImagePrefix$i", Context.MODE_PRIVATE).use {
                    it?.writeBitmapJPEG(bitmap)
                }
                Log.d(getLogTag(), "saved car image $carImagePrefix$i to storage")
            }
            Log.d(getLogTag(), "saved sample car images to storage with prefix $carImagePrefix")
        }
    }

    fun saveTripsToStorage(trips: List<Trip>) {
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val counter = sharedPreferences.getString(counterName, null)?.toInt()
        // first time saving || data has changed
        if (counter == null || counter != trips.size || forceReset) {
            // TODO: should use EditFragment.writeParcelable()
            with(sharedPreferences.edit()) {
                putString(counterName, Json.encodeToString(trips.size))
                Log.d(getLogTag(), "saved shared preference: $counterName with data ${trips.size}")
                for ((i, trip) in trips.withIndex()) {
                    putString("$tripPrefix$i", Json.encodeToString(trip))
                    Log.d(getLogTag(), "saved shared preference: $tripPrefix$i with data $trip")
                }
                apply()
            }
        }
    }

    saveCarImagesToStorage()
    val trips = (0 until tripsNumber).map { getRandomTrip(it.toLong()) }
    saveTripsToStorage(trips)
}