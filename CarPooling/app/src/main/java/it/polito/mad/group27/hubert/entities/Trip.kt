package it.polito.mad.group27.hubert.entities

import android.net.Uri
import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import it.polito.mad.group27.hubert.calendarToTimestamp
import it.polito.mad.group27.hubert.timestampToCalendar
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.time.LocalTime
import java.util.*


// do we need the Trip/TripDB id? YES, in OthersTripList
// we should use the document id. How to get it automatically? DONE in saveTrip method of tripEditFragment


@Parcelize
data class Trip(
    // primary keys
    var id: String? = null,
    var ownerUid: String = "testUid",
    // other fields
    var carImageUri: Uri? = null,
    var totalSeats: Int? = null,
    var price: BigDecimal? = null,
    var startDateTime: Calendar = (Calendar.getInstance()
        .clone() as Calendar).also { it.add(Calendar.HOUR, +1) },
    var endDateTime: Calendar = (Calendar.getInstance()
        .clone() as Calendar).also { it.add(Calendar.HOUR, +2) },
    var from: String = "",
    var fromGeoPoint: org.osmdroid.util.GeoPoint? = null,
    var to: String = "",
    var toGeoPoint: org.osmdroid.util.GeoPoint? = null,
    val stops: MutableList<Stop> = mutableListOf(),
    val options: MutableList<Option> = mutableListOf(),
    var otherInformation: String? = null,
    val acceptedUsersUids: MutableList<String> = mutableListOf(),
    val interestedUsersUids: MutableList<String> = mutableListOf(),
    var advertised: Boolean = true
) : Parcelable {

    fun toTripDB() = TripDB(
            id = id,
            ownerUid = ownerUid,
            carImageUri = carImageUri?.toString(),
            totalSeats = totalSeats!!,
            price = price!!.also { it.setScale(2) }.toString(),
            startDateTime = calendarToTimestamp(startDateTime),
            endDateTime = calendarToTimestamp(endDateTime),
            from = from,
            to = to,
            stops = stops.map { it.toStopDB() }.toMutableList(),
            options = options.map { it.ordinal.toLong() }.toMutableList(),
            acceptedUsersUids = acceptedUsersUids,
            interestedUsersUids = interestedUsersUids,
            otherInformation = otherInformation,
            advertised = advertised,
            fromGeoPoint = GeoPoint(fromGeoPoint!!.latitude, fromGeoPoint!!.longitude),
            toGeoPoint = GeoPoint(toGeoPoint!!.latitude, toGeoPoint!!.longitude)
        )
}

@Parcelize
data class Hour(var hour: Int, var minute: Int) : Parcelable {
    constructor(calendar: Calendar) : this(
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE)
    )

    @RequiresApi(Build.VERSION_CODES.O)
    constructor(dateTime: LocalTime) : this(dateTime.hour, dateTime.minute)

    override fun toString() =
        "${if (hour < 10) "0" else ""}$hour:${if (minute < 10) "0" else ""}$minute"

}


@Parcelize
data class Stop(
    var place: String,
    var dateTime: Calendar,
    var geoPoint: org.osmdroid.util.GeoPoint? = null
) : Parcelable {
    fun toStopDB(): StopDB {
        return StopDB(
            place,
            calendarToTimestamp(dateTime),
            GeoPoint(geoPoint!!.latitude, geoPoint!!.longitude))
    }
}

enum class Option {
    ANIMALS,
    LUGGAGE,
    SMOKE,
}


data class TripDB(
    // primary keys
    var id: String?=null,
    var ownerUid: String="",
    var carImageUri: String?=null,
    var totalSeats: Int=0,
    var price: String="0.00",
    var startDateTime: Timestamp=Timestamp.now(),
    var endDateTime: Timestamp=Timestamp.now(),
    var from: String="",
    var fromGeoPoint: GeoPoint = GeoPoint(0.0, 0.0),
    var to: String="",
    var toGeoPoint: GeoPoint = GeoPoint(0.0, 0.0),
    val stops: MutableList<StopDB> = mutableListOf(),
    val options: MutableList<Long> = mutableListOf(),
    var otherInformation: String?=null,
    val acceptedUsersUids: MutableList<String> = mutableListOf(),
    val interestedUsersUids: MutableList<String> = mutableListOf(),
    var advertised: Boolean = true
) {

    fun toTrip() = Trip(
            id = id,
            ownerUid = ownerUid,
            carImageUri = if (carImageUri == null) null else Uri.parse(carImageUri),
            totalSeats = totalSeats,
            price = BigDecimal(price).also{it.setScale(2)},
            startDateTime = timestampToCalendar(startDateTime),
            endDateTime = timestampToCalendar(endDateTime),
            from = from,
            to = to,
            stops = stops.map { it.toStop() }.toMutableList(),
            options = options.map { Option.values()[it.toInt()] }.toMutableList(),
            acceptedUsersUids = acceptedUsersUids,
            interestedUsersUids = interestedUsersUids,
            otherInformation = otherInformation,
            advertised = advertised,
            fromGeoPoint = org.osmdroid.util.GeoPoint(fromGeoPoint.latitude, fromGeoPoint.longitude),
            toGeoPoint = org.osmdroid.util.GeoPoint(toGeoPoint.latitude, toGeoPoint.longitude)
        )
}

data class StopDB(var place: String="",
                  var dateTime: Timestamp=Timestamp.now(),
                  var geoPoint: GeoPoint = GeoPoint(0.0, 0.0)){
    fun toStop(): Stop {
        return Stop(
            place,
            dateTime = timestampToCalendar(dateTime),
            org.osmdroid.util.GeoPoint(geoPoint.latitude, geoPoint.longitude))
    }
}



