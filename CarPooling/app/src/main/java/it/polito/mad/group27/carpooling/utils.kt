package it.polito.mad.group27.carpooling

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun Fragment.getLogTag(): String {
    return getString(R.string.log_tag)
}

fun AppCompatActivity.getLogTag(): String {
    return getString(R.string.log_tag)
}