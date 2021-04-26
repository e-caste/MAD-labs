package it.polito.mad.group27.carpooling.ui.trip.tripedit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.Watcher
import it.polito.mad.group27.carpooling.ui.trip.Stop
import it.polito.mad.group27.carpooling.ui.trip.Trip

// TODO create string resources
class StopRecyclerViewAdapter(val trip: Trip, val context: Context) :
    RecyclerView.Adapter<StopRecyclerViewAdapter.ItemViewHolder>() {
    class ItemViewHolder(v: View, val context: Context, val trip: Trip) : RecyclerView.ViewHolder(v) {
        private val placeView = v.findViewById<TextInputLayout>(R.id.stop_place)
        private val hourView = v.findViewById<TextInputLayout>(R.id.stop_hour)
        private var timePicker: MaterialTimePicker? = null

        fun bind(stop: Stop, position: Int) {
            placeView.editText?.setText(stop.place)
            placeView.hint = "Stop ${position+1}"
            placeView?.editText?.addTextChangedListener(Watcher(
                { placeView.editText?.text?.isEmpty() ?: true },
                { placeView.error = "Destination can not be empty"
                    stop.place = placeView.editText?.text.toString()
                    (context as AppCompatActivity).invalidateOptionsMenu() },
                { placeView.error = null
                    stop.place = placeView.editText?.text.toString()
                    (context as AppCompatActivity).invalidateOptionsMenu() }
            ))
            hourView.editText?.setText(stop.hour.toString())
            hourView.editText?.setOnClickListener {
                if (timePicker == null || !timePicker?.isVisible!!) {
                    timePicker = getTimePicker(
                        hourView.editText!!,
                        stop.hour,
                        context
                    ) {
                        stop.hour.updateTime(it)
                    }
                    timePicker!!.show(
                        (context as AppCompatActivity).getSupportFragmentManager(),
                        "timePickerTag"
                    )
                }
            }
            hourView.editText?.addTextChangedListener(Watcher(
                { hourView.editText?.text.toString() <= trip.startHour.toString()
                        || hourView.editText?.text.toString() >= trip.endHour.toString()
                        || (if (position > 0)
                            hourView.editText?.text.toString() <= trip.stops[position-1].hour.toString()
                            else false)
                        || (if (position < trip.stops.size - 1)
                                hourView.editText?.text.toString() >= trip.stops[position+1].hour.toString()
                            else false)},
                { hourView.error = "Invalid stop time"
                    (context as AppCompatActivity).invalidateOptionsMenu() },
                { hourView.error = null
                    (context as AppCompatActivity).invalidateOptionsMenu() }
            ))
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.place_hour, parent, false)
        return ItemViewHolder(layout, context, trip)
    }

    override fun getItemCount(): Int {
        return trip.stops.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(trip.stops[position], position)
    }

    fun add(stop: Stop) {
        trip.stops.add(stop)
        this.notifyItemInserted(trip.stops.size - 1)
    }

    fun remove() {
        trip.stops.removeAt(trip.stops.size - 1)
        this.notifyItemRemoved(trip.stops.size)
    }

}