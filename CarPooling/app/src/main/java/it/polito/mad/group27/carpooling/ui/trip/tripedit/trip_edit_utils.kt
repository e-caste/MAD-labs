package it.polito.mad.group27.carpooling.ui.trip.tripedit

import android.content.Context
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.trip.Hour
import it.polito.mad.group27.carpooling.ui.trip.Trip
import java.text.DateFormat
import java.text.FieldPosition
import java.text.SimpleDateFormat
import java.util.*

val df: DateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())
val YYYYMMDD: DateFormat = SimpleDateFormat("yyyyddMM")

fun getTimePicker(view: EditText, hour: Calendar, context: Context, update: (MaterialTimePicker) -> Calendar): MaterialTimePicker {
    val isSystem24Hour = is24HourFormat(context)
    val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

    val timePicker =
        MaterialTimePicker.Builder()
            .setTimeFormat(clockFormat)
            .setHour(hour.get(Calendar.HOUR))
            .setMinute(hour.get(Calendar.MINUTE))
            .build()
    timePicker.addOnPositiveButtonClickListener {
        val newHour: Calendar = update(timePicker)
        view.setText(Hour(newHour).toString())
    }
    timePicker.addOnDismissListener {
        timePicker.dismiss()
    }
    return timePicker
}

fun getDatePicker(calendar: Calendar, view: TextInputLayout) : MaterialDatePicker<Long> {
    val constraintsBuilder =
        CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())
    val datePicker =
        MaterialDatePicker.Builder.datePicker()
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setCalendarConstraints(constraintsBuilder.build())
            .build()
    datePicker.addOnPositiveButtonClickListener {
        calendar.timeInMillis = datePicker.selection!!
        view.editText?.setText(df.format(calendar))
    }
    return datePicker
}

fun Calendar.updateTime(timePicker: MaterialTimePicker): Calendar {
    this.set(Calendar.HOUR, timePicker.hour)
    this.set(Calendar.MINUTE, timePicker.minute)
    return this
}

fun Trip.checkDateTimeStop(position: Int) : Pair<Boolean, Boolean>{

    val stop = this.stops[position]
    var validStopTime = true
    var validStopDate = true
    val newTrip = this

    if(position == 0){
        if(YYYYMMDD.format(this.stops[position].dateTime.time) < YYYYMMDD.format(newTrip.startDateTime.time)){
            validStopDate = false
        }
        else if (YYYYMMDD.format(stop.dateTime.time) == YYYYMMDD.format(newTrip.startDateTime.time)
            && Hour(stop.dateTime.get(Calendar.HOUR), stop.dateTime.get(Calendar.MINUTE)).toString() <=
            Hour(newTrip.startDateTime.get(Calendar.HOUR), newTrip.startDateTime.get(Calendar.MINUTE)).toString()){
            validStopTime = false
        }
    }else if(position == newTrip.stops.size -1){
        if(YYYYMMDD.format(stop.dateTime.time) > YYYYMMDD.format(newTrip.endDateTime.time)){
            validStopDate = false
        }
        else if (YYYYMMDD.format(stop.dateTime.time) == YYYYMMDD.format(newTrip.endDateTime.time)
            && Hour(stop.dateTime.get(Calendar.HOUR), stop.dateTime.get(Calendar.MINUTE)).toString() >=
            Hour(newTrip.endDateTime.get(Calendar.HOUR), newTrip.endDateTime.get(Calendar.MINUTE)).toString()){
            validStopTime = false
        }

        if(YYYYMMDD.format(stop.dateTime.time) < YYYYMMDD.format(newTrip.stops[position-1].dateTime.time)){
            validStopDate = false
        }
        else if (YYYYMMDD.format(stop.dateTime.time) == YYYYMMDD.format(newTrip.stops[position-1].dateTime.time)
            && Hour(stop.dateTime.get(Calendar.HOUR), stop.dateTime.get(Calendar.MINUTE)).toString() >=
            Hour(newTrip.stops[position-1].dateTime.get(Calendar.HOUR), newTrip.stops[position-1].dateTime.get(Calendar.MINUTE)).toString()){
            validStopTime = false
        }

    }else{
        if(YYYYMMDD.format(stop.dateTime.time) < YYYYMMDD.format(newTrip.stops[position-1].dateTime.time)){
            validStopDate = false
        }
        else if (YYYYMMDD.format(stop.dateTime.time) == YYYYMMDD.format(newTrip.stops[position-1].dateTime.time)
            && Hour(stop.dateTime.get(Calendar.HOUR), stop.dateTime.get(Calendar.MINUTE)).toString() >=
            Hour(newTrip.stops[position-1].dateTime.get(Calendar.HOUR), newTrip.stops[position-1].dateTime.get(Calendar.MINUTE)).toString()){
            validStopTime = false
        }
    }

    return Pair(validStopDate, validStopTime)
}