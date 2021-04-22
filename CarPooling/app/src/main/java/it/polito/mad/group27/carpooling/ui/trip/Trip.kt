package it.polito.mad.group27.carpooling.ui.trip

import android.net.Uri
import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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


@Serializable(with = DateSerializer::class)
@Parcelize
data class Trip(
    var id: Long = -1,
    var uri: Uri? = null,
    var date: Date = Date(),
    var startHour: Hour =
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            Hour(LocalTime.now())
        else{
            val calendar = java.util.Calendar.getInstance()
            Hour(calendar)
        },
    var endHour: Hour =
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            Hour(LocalTime.now().plusHours(1))
        else{
            val calendar = java.util.Calendar.getInstance()
            calendar.add(Calendar.HOUR_OF_DAY, +1)
            Hour(calendar)
        },
    var from: String = "",
    var to: String = "",
    val stops: Map<String, Hour> = mutableMapOf(),
    val options: List<Option> = mutableListOf()
): Parcelable

@Serializable
@Parcelize
data class Hour(var hour: Int, var minute: Int): Parcelable{
    constructor(calendar: Calendar) : this(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    @RequiresApi(Build.VERSION_CODES.O)
    constructor(dateTime: LocalTime) : this(dateTime.hour , dateTime.minute)
    override fun toString(): String {
        return "${hour}:${minute}"
    }
}

enum class Option{
    ANIMALS, LUGGAGE, NO_SMOKE, OTHER
}