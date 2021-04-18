package it.polito.mad.group27.carpooling.ui.trip

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@Parcelize
data class Trip(
    val date: Date,
    val startHour: Hour,
    val from: String,
    val to: String,
    val stops: Map<String, Hour>,
    val options: List<Option>
): Parcelable

@Serializable
@Parcelize
data class Date(var day: Int, var month: Int, var year: Int): Parcelable{
    constructor(calendar: Calendar) : this(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))

    companion object pippo{
        val it_months = listOf<String>(
            "Gennaio",
            "Febbraio",
            "Marzo",
            "Aprile",
            "Maggio",
            "Giugno",
            "Luglio",
            "Agosto",
            "Settembre",
            "Ottobre",
            "Novembre",
            "Dicembre")

    val en_months = listOf<String>(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December")
}

    fun toString(l: Locale): String {
        val locale = l.toString()
        if (locale == "it_IT")
            return "$day ${pippo.it_months[month-1]} $year"
        else if (locale == "en_UK")
            return  "$day ${pippo.en_months[month-1]} $year"
        else
            return "${pippo.en_months[month-1]} $day, $year"
    }

}

@Serializable
@Parcelize
data class Hour(var hour: Int, var minute: Int): Parcelable

enum class Option{
    ANIMALS, LUGGAGE, NO_SMOKE, OTHER
}