package it.polito.mad.group27.carpooling.ui.trip

import android.net.Uri
import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import it.polito.mad.group27.carpooling.calendarToTimestamp
import it.polito.mad.group27.carpooling.timestampToCalendar
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.osmdroid.views.MapView
import java.io.File
import java.math.BigDecimal
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

@Serializer(forClass = Date::class)
object CalendarSerializer : KSerializer<Calendar> {
    private val df: DateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Calendar) {
        encoder.encodeString(df.format(value.time))
    }

    override fun deserialize(decoder: Decoder): Calendar {
        return Calendar.getInstance().also {
            it.time = df.parse(decoder.decodeString())!!
        }
    }
}

@Serializer(forClass = Uri::class)
object UriSerializer : KSerializer<Uri> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Uri", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Uri) {
        encoder.encodeString(value.path ?: "")
    }

    override fun deserialize(decoder: Decoder): Uri {
        return Uri.fromFile(File(decoder.decodeString()))
    }
}

@Serializer(forClass = BigDecimal::class)
object BigDecimalSerializer : KSerializer<BigDecimal> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        return BigDecimal(decoder.decodeString())
    }
}

@Serializer(forClass = org.osmdroid.util.GeoPoint::class)
object OsmdroidGeoPointSerializer : KSerializer<org.osmdroid.util.GeoPoint> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("osmdroidGeoPoint", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): org.osmdroid.util.GeoPoint {
        val decoded = decoder.decodeString().split(" ")
        return org.osmdroid.util.GeoPoint(decoded[0].toDoubleOrNull() ?: 0.0, decoded[1].toDoubleOrNull() ?: 0.0)
    }

    override fun serialize(encoder: Encoder, value: org.osmdroid.util.GeoPoint) {
        encoder.encodeString("${value.latitude} ${value.longitude}")
    }
}

// do we need the Trip/TripDB id? YES, in OthersTripList
// we should use the document id. How to get it automatically? DONE in saveTrip method of tripEditFragment

@Serializable
@Parcelize
data class Trip(
    // primary keys
    var id: String? = null,
    var ownerUid: String = "testUid",
    // other fields
    @Serializable(with = UriSerializer::class)
    var carImageUri: Uri? = null,
    var totalSeats: Int? = null,
    @Deprecated("Will no longer be used, use the list size")
    var availableSeats: Int? = null,
    @Serializable(with = BigDecimalSerializer::class)
    var price: BigDecimal? = null,
    @Serializable(with = CalendarSerializer::class)
    var startDateTime: Calendar = (Calendar.getInstance()
        .clone() as Calendar).also { it.add(Calendar.HOUR, +1) },
    @Serializable(with = CalendarSerializer::class)
    var endDateTime: Calendar = (Calendar.getInstance()
        .clone() as Calendar).also { it.add(Calendar.HOUR, +2) },
    var from: String = "",
    @Serializable(with = OsmdroidGeoPointSerializer::class)
    var fromGeoPoint: org.osmdroid.util.GeoPoint = org.osmdroid.util.GeoPoint(0.0, 0.0),
    var to: String = "",
    @Serializable(with = OsmdroidGeoPointSerializer::class)
    var toGeoPoint: org.osmdroid.util.GeoPoint = org.osmdroid.util.GeoPoint(0.0, 0.0),
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
            fromGeoPoint = GeoPoint(fromGeoPoint.latitude, fromGeoPoint.longitude),
            toGeoPoint = GeoPoint(toGeoPoint.latitude, toGeoPoint.longitude)
        )
}

@Serializable
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

@Serializable
@Parcelize
data class Stop(
    var place: String,
    @Serializable(with = CalendarSerializer::class) var dateTime: Calendar,
    @Serializable(with = OsmdroidGeoPointSerializer::class)
    var geoPoint: org.osmdroid.util.GeoPoint = org.osmdroid.util.GeoPoint(0.0, 0.0)
) : Parcelable {
    fun toStopDB():StopDB{
        return StopDB(
            place,
            calendarToTimestamp(dateTime),
            GeoPoint(geoPoint.latitude, geoPoint.longitude))
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



