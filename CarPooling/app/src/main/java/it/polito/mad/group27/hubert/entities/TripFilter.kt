package it.polito.mad.group27.hubert.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.util.*


@Parcelize
data class TripFilter(
    var from: String? = null,
    var to: String? = null,
    var priceMin: BigDecimal = BigDecimal("0.00"),
    var priceMax: BigDecimal = BigDecimal("500.00"),
    var dateTime: Calendar? = null,
    var options: MutableMap<Option, Boolean> = Option.values().map{ it to false }.toMap(mutableMapOf())
): Parcelable
