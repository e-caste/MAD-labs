package it.polito.mad.group27.carpooling.ui.trip

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
@Parcelize
data class Trip(
    var date: Date =
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            Date(
            LocalDateTime.now().dayOfMonth ,
            LocalDateTime.now().monthValue,
            LocalDateTime.now().year)
        else
            Date(java.util.Calendar.getInstance()),
    var startHour: Hour =
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            Hour(
                LocalDateTime.now().hour ,
                LocalDateTime.now().minute,)
        else
            Hour(java.util.Calendar.getInstance()),
    var endHour: Hour =
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            Hour(
                LocalDateTime.now().plusHours(1).hour ,
                LocalDateTime.now().plusHours(1).minute,)
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
data class Date(var day: Int, var month: Int, var year: Int): Parcelable{
    constructor(calendar: Calendar) : this(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))

    companion object Months{
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
            return "$day ${Months.it_months[month]} $year"
        else if (locale == "en_UK")
            return  "$day ${Months.en_months[month]} $year"
        else
            return "${Months.en_months[month]} $day, $year"
    }

}

@Serializable
@Parcelize
data class Hour(var hour: Int, var minute: Int): Parcelable{
    constructor(calendar: Calendar) : this(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    override fun toString(): String {
        return "${hour}:${minute}"
    }
}

enum class Option{
    ANIMALS, LUGGAGE, NO_SMOKE, OTHER
}