package it.polito.mad.group27.carpooling.ui.trip

import android.net.Uri
import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.*
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
object CalendarSerializer: KSerializer<Calendar> {
    private val df: DateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm")

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Calendar) {
        encoder.encodeString(df.format(value))
    }

    override fun deserialize(decoder: Decoder): Calendar {
        return Calendar.getInstance().also{
            it.time = df.parse(decoder.decodeString())!!
        }
    }
}

@Serializer(forClass = Uri::class)
object UriSerializer: KSerializer<Uri> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Uri", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Uri) {
        encoder.encodeString(value.path ?:"")
    }

    override fun deserialize(decoder: Decoder): Uri {
        return Uri.fromFile(File(decoder.decodeString()))
    }
}

@Serializer(forClass = BigDecimal::class)
object BigDecimalSerializer: KSerializer<BigDecimal> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value:BigDecimal) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        return BigDecimal(decoder.decodeString())
    }
}




@Serializable
@Parcelize
data class Trip(
    var id: Int = -1,
    @Serializable(with=UriSerializer::class)
    var carImageUri: Uri? = null,
    var totalSeats: Int? = null,
    var availableSeats: Int? = null,
    @Serializable(with=BigDecimalSerializer::class)
    var price: BigDecimal? = null,
    @Serializable(with=CalendarSerializer::class)
    var startDateTime: Calendar = Calendar.getInstance(),
    @Serializable(with=CalendarSerializer::class)
    var endDateTime: Calendar =  {
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.HOUR, +1)
                            calendar
                        }(),
    var from: String = "",
    var to: String = "",
    val stops: MutableList<Stop> = mutableListOf(),
    val options: MutableList<Option> = mutableListOf(),
    var otherInformation: String? = null
): Parcelable

@Serializable
@Parcelize
data class Hour(var hour: Int, var minute: Int): Parcelable{
    constructor(calendar: Calendar) : this(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    @RequiresApi(Build.VERSION_CODES.O)
    constructor(dateTime: LocalTime) : this(dateTime.hour , dateTime.minute)
    override fun toString() = "${if (hour < 10) "0" else ""}$hour:${if (minute < 10) "0" else ""}$minute"

}

@Serializable
@Parcelize
data class Stop(var place: String, @Serializable(with=CalendarSerializer::class) var dateTime: Calendar): Parcelable

enum class Option {
    ANIMALS,
    LUGGAGE,
    SMOKE,
}