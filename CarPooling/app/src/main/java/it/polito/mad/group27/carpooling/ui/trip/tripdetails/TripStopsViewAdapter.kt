package it.polito.mad.group27.carpooling.ui.trip.tripdetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.ui.trip.Stop
import it.polito.mad.group27.carpooling.ui.trip.tripdetails.dummy.DummyStageContent.DummyStage

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
        holder.stageTime.text = item.hour.toString()
        holder.stageName.text = item.place
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val stageTime : TextView = view.findViewById(R.id.tripStopTime)
        val stageName : TextView = view.findViewById(R.id.tripStopName)
    }
}