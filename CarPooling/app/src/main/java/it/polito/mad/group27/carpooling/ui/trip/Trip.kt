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
object DateSerializer: KSerializer<Date> {
    private val df: DateFormat = SimpleDateFormat("dd/MM/yyyy")

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(df.format(value))
    }

    override fun deserialize(decoder: Decoder): Date {
        return df.parse(decoder.decodeString())!!
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
    var id: Long = -1,
    @Serializable(with=UriSerializer::class)
    var carImageUri: Uri? = null,
    @Serializable(with=DateSerializer::class)
    var date: Date = Date(),
    var totalSeats: Int? = null,
    var availableSeats: Int? = null,
    @Serializable(with=BigDecimalSerializer::class)
    var price: BigDecimal? = null,
    var startHour: Hour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Hour(LocalTime.now())
                          else Hour(Calendar.getInstance()),
    var endHour: Hour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Hour(LocalTime.now().plusHours(1))
                        else {
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.HOUR_OF_DAY, +1)
                            Hour(calendar)
                        },
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
data class Stop(val place: String, val hour: Hour): Parcelable

enum class Option {
    ANIMALS,
    LUGGAGE,
    NO_SMOKE,
}