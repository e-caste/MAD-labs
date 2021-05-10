package it.polito.mad.group27.carpooling.ui.trip

import android.net.Uri
import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import it.polito.mad.group27.carpooling.CalendarToTimestamp
import it.polito.mad.group27.carpooling.TimestampToCalendar
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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

// TODO: do we need the Trip/TripDB id? YES, in OthersTripList
//  we should use the document id. How to get it automatically?

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
    var startDateTime: Calendar = Calendar.getInstance(),
    @Serializable(with = CalendarSerializer::class)
    var endDateTime: Calendar = (Calendar.getInstance()
        .clone() as Calendar).also { it.add(Calendar.HOUR, +1) },
    var from: String = "",
    var to: String = "",
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
            startDateTime = CalendarToTimestamp(startDateTime),
            endDateTime = CalendarToTimestamp(endDateTime),
            from = from,
            to = to,
            stops = stops.map { it.toStopDB() }.toMutableList(),
            options = options.map { it.ordinal.toLong() }.toMutableList(),
            acceptedUsersUids = acceptedUsersUids,
            interestedUsersUids = interestedUsersUids,
            otherInformation = otherInformation,
            advertised = advertised
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
    @Serializable(with = CalendarSerializer::class) var dateTime: Calendar
) : Parcelable {
    fun toStopDB():StopDB{
        return StopDB(place, CalendarToTimestamp(dateTime))
    }
}

enum class Option {
    ANIMALS,
    LUGGAGE,
    SMOKE,
}


@Parcelize
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
    var to: String="",
    val stops: MutableList<StopDB> = mutableListOf(),
    val options: MutableList<Long> = mutableListOf(),
    var otherInformation: String?=null,
    val acceptedUsersUids: MutableList<String> = mutableListOf(),
    val interestedUsersUids: MutableList<String> = mutableListOf(),
    var advertised: Boolean = true
) : Parcelable {

    fun toTrip() = Trip(
            id = id,
            ownerUid = ownerUid,
            carImageUri = if (carImageUri == null) null else Uri.parse(carImageUri),
            totalSeats = totalSeats,
            price = BigDecimal(price).also{it.setScale(2)},
            startDateTime = TimestampToCalendar(startDateTime),
            endDateTime = TimestampToCalendar(endDateTime),
            from = from,
            to = to,
            stops = stops.map { it.toStop() }.toMutableList(),
            options = options.map { Option.values()[it.toInt()] }.toMutableList(),
            acceptedUsersUids = acceptedUsersUids,
            interestedUsersUids = interestedUsersUids,
            otherInformation = otherInformation,
            advertised = advertised
        )
}

@Parcelize
data class StopDB(var place: String="", var dateTime: Timestamp=Timestamp.now()) : Parcelable {
    fun toStop(): Stop {
        return Stop(place, dateTime = TimestampToCalendar(dateTime))
    }
}



