package it.polito.mad.group27.carpooling.ui.trip.tripdetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.ui.trip.Hour
import it.polito.mad.group27.carpooling.ui.trip.Stop
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class TripStopsViewAdapter(
        private val values: List<Stop>
) : RecyclerView.Adapter<TripStopsViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_trip_stop_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        val date = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ITALY).format(item.dateTime.timeInMillis).toString()
        val time = Hour(item.dateTime[Calendar.HOUR_OF_DAY], item.dateTime[Calendar.MINUTE]).toString()

        holder.stopDateTime.text = "$date, $time"
        holder.stopName.text = item.place
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val stopDateTime : TextView = view.findViewById(R.id.tripStopDateTime)
        val stopName : TextView = view.findViewById(R.id.tripStopName)
    }
}