package it.polito.mad.group27.carpooling.ui.trip.tripedit

import android.widget.TextView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import it.polito.mad.group27.carpooling.ui.trip.Hour

fun getTimePicker(view: TextView, hour: Hour, update: (MaterialTimePicker) -> Hour): MaterialTimePicker {
    // TODO select 12H or 24H basing on Locale.getDefault()
    val timePicker =
        MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
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