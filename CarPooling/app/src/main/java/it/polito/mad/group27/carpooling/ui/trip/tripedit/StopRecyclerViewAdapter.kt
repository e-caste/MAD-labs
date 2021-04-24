package it.polito.mad.group27.carpooling.ui.trip.tripedit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.ui.trip.Hour
import it.polito.mad.group27.carpooling.ui.trip.Stop


class StopRecyclerViewAdapter(val stops: MutableList<Stop>, val context: Context): RecyclerView.Adapter<StopRecyclerViewAdapter.ItemViewHolder>(){
    class ItemViewHolder(v: View, val context: Context): RecyclerView.ViewHolder(v){
        private val placeView = v.findViewById<TextInputEditText>(R.id.stop_place)
        private val hourView = v.findViewById<TextInputEditText>(R.id.stop_hour)
        private var timePicker : MaterialTimePicker? = null
        //todo set hints for the various stops

        fun bind(stop: Stop) {
            placeView.setText(stop.place)
            hourView.setText(stop.hour.toString())
            hourView.setOnClickListener {
                if(timePicker == null || !timePicker?.isVisible!!) {
                    timePicker = getTimePicker(
                        hourView,
                        stop.hour
                    ){
                        stop.hour.updateTime(it)
                    }
                    timePicker!!.show(
                        (context as AppCompatActivity).getSupportFragmentManager(),
                        "timePickerTag"
                    )
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.place_hour, parent, false)
        return ItemViewHolder(layout, context)
    }

    override fun getItemCount(): Int {
        return stops.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(stops[position])
    }

    fun add(stop: Stop){
        stops.add(stop)
        this.notifyItemInserted(stops.size - 1)
    }

    fun remove(){
        stops.removeAt(stops.size-1)
        this.notifyItemRemoved(stops.size)
    }

}