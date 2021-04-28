package it.polito.mad.group27.carpooling.ui.trip.tripedit

import android.content.Context
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.Watcher
import it.polito.mad.group27.carpooling.ui.trip.Hour
import it.polito.mad.group27.carpooling.ui.trip.Stop
import it.polito.mad.group27.carpooling.ui.trip.Trip

class StopRecyclerViewAdapter(val trip: Trip, val context: Context) :
    RecyclerView.Adapter<StopRecyclerViewAdapter.ItemViewHolder>() {
    class ItemViewHolder(v: View, val context: Context, val trip: Trip, val parent: StopRecyclerViewAdapter) : RecyclerView.ViewHolder(v) {
        private val placeView = v.findViewById<TextInputLayout>(R.id.stop_place)
        private val hourView = v.findViewById<TextInputLayout>(R.id.stop_hour)
        private val dateView  = v.findViewById<TextInputLayout>(R.id.stop_date)
        private val remove_button = v.findViewById<ImageView>(R.id.remove_stop_button)
        private lateinit var timePicker: MaterialTimePicker
        private lateinit var datePicker: MaterialDatePicker<Long>

        fun bind(stop: Stop, position: Int) {
            datePicker = getDatePicker(stop.dateTime, dateView)
            remove_button.visibility = View.VISIBLE

            val dateTimeWatcher = Watcher(
                { val (validStopDate, validStopTime) = trip.checkDateTimeStop(position)
                    validStopDate || validStopTime},
                {
                    val (validStopDate, validStopTime) = trip.checkDateTimeStop(position)
                    if(validStopDate){
                        dateView.error = context.getString(R.string.date_error)
                        hourView.error = null
                    }
                    else{
                        hourView.error = context.getString(R.string.stop_hour_error)
                        dateView.error = null
                    }
                },
                {
                    dateView.error = null
                    hourView.error = null
                }
            )

            placeView.editText?.setText(stop.place)
            placeView.hint = context.getString(R.string.stop) + " ${position+1}"
            placeView?.editText?.addTextChangedListener(Watcher(
                { placeView.editText?.text?.isEmpty() ?: true },
                { placeView.error = context.getString(R.string.stop_place_error)
                    stop.place = placeView.editText?.text.toString()
                    (context as AppCompatActivity).invalidateOptionsMenu() },
                { placeView.error = null
                    stop.place = placeView.editText?.text.toString()
                    (context as AppCompatActivity).invalidateOptionsMenu() }
            ))

            remove_button.setOnClickListener {
                parent.remove(position)
            }

            hourView.editText?.setText(Hour(stop.dateTime).toString())
            hourView.editText?.setOnClickListener {
                if (!timePicker.isVisible) {
                    timePicker = getTimePicker(
                        hourView.editText!!,
                        stop.dateTime,
                        context
                    ) {
                        stop.dateTime.updateTime(it)
                    }
                    timePicker.show(
                        (context as AppCompatActivity).getSupportFragmentManager(),
                        "timePickerTag"
                    )
                }
            }
            hourView.editText?.addTextChangedListener(dateTimeWatcher)

            dateView.editText?.setText(df.format(stop.dateTime.time))
            dateView.editText?.setOnClickListener {
                if(!datePicker.isVisible)
                    datePicker.show((context as AppCompatActivity).getSupportFragmentManager(), "datePickerTag")
            }
            dateView.editText?.addTextChangedListener(dateTimeWatcher)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.place_datetime, parent, false)
        return ItemViewHolder(layout, context, trip, this)
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

    fun remove(position : Int) {
        trip.stops.removeAt(position)
        this.notifyItemRemoved(position)
    }

}