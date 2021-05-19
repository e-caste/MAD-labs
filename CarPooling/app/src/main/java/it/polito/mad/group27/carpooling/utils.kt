package it.polito.mad.group27.carpooling

import com.google.firebase.Timestamp
import java.util.*

const val TAG = "MAD-group-27"

fun getLogTag() = TAG

fun timestampToCalendar(dateTime: Timestamp): Calendar =
    Calendar.getInstance().also { it.timeInMillis = dateTime.seconds * 1000 }

fun calendarToTimestamp(dateTime: Calendar): Timestamp =
    Timestamp(dateTime.time)
