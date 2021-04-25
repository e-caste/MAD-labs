package it.polito.mad.group27.carpooling.ui.trip.tripedit

import android.content.Context
import android.text.format.DateFormat.is24HourFormat
import android.widget.TextView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import it.polito.mad.group27.carpooling.ui.trip.Hour

fun getTimePicker(view: TextView, hour: Hour, context: Context, update: (MaterialTimePicker) -> Hour): MaterialTimePicker {
    val isSystem24Hour = is24HourFormat(context)
    val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

    val timePicker =
        MaterialTimePicker.Builder()
            .setTimeFormat(clockFormat)
            .setHour(hour.hour)
            .setMinute(hour.minute)
            .build()
    timePicker.addOnPositiveButtonClickListener {
        val newHour: Hour = update(timePicker)
        view.text = newHour.toString()
    }
    timePicker.addOnDismissListener {
        timePicker.dismiss()
    }
    return timePicker
}

fun Hour.updateTime(timePicker: MaterialTimePicker): Hour {
    this.hour = timePicker.hour
    this.minute = timePicker.minute
    return this
}