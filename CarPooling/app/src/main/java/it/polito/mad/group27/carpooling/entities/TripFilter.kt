package it.polito.mad.group27.carpooling.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.*


@Serializable
@Parcelize
data class TripFilter(
    var from: String? = null,
    var to: String? = null,
    @Serializable(with= BigDecimalSerializer::class)
    var priceMin: BigDecimal = BigDecimal("0.00"),
    @Serializable(with= BigDecimalSerializer::class)
    var priceMax: BigDecimal = BigDecimal("100.00"),
    @Serializable(with= CalendarSerializer::class)
    var dateTime: Calendar? = null,
    var options: MutableMap<Option, Boolean> = Option.values().map{ it to false }.toMap(mutableMapOf())
): Parcelable
