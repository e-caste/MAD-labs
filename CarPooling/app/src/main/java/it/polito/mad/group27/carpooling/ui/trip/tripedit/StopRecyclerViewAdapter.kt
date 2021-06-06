package it.polito.mad.group27.carpooling.ui.trip.tripedit

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.Watcher
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.entities.Hour
import it.polito.mad.group27.carpooling.entities.Stop
import it.polito.mad.group27.carpooling.entities.Trip

class StopRecyclerViewAdapter(val trip: Trip, private val context: Context, private val navController: NavController) :
    RecyclerView.Adapter<StopRecyclerViewAdapter.ItemViewHolder>() {
    class ItemViewHolder(v: View, val context: Context, val trip: Trip, val parent: StopRecyclerViewAdapter) : RecyclerView.ViewHolder(v) {
        private val placeView = v.findViewById<TextInputLayout>(R.id.stop_place)
        private val hourView = v.findViewById<TextInputLayout>(R.id.stop_hour)
        private val dateView  = v.findViewById<TextInputLayout>(R.id.stop_date)
        private val removeButton = v.findViewById<ImageView>(R.id.remove_stop_button)
        private var timePicker: MaterialTimePicker ? = null
        private lateinit var datePicker: MaterialDatePicker<Long>

        private var dateTimeWatcher: Watcher?= null
        private var placeViewWatcher: Watcher?= null

        fun bind(stop: Stop, position: Int, navController: NavController) {
            datePicker = getDatePicker(stop.dateTime, dateView)
            removeButton.visibility = View.VISIBLE
            if (dateTimeWatcher!=null){
                dateView.editText!!.removeTextChangedListener(dateTimeWatcher)
                hourView.editText!!.removeTextChangedListener(dateTimeWatcher)
            }

            if(placeViewWatcher!=null){
                placeView.editText!!.removeTextChangedListener(placeViewWatcher)
            }

            dateView.error = null
            hourView.error = null
            placeView.error = null


            dateTimeWatcher = Watcher(
                { val (validStopDate, validStopTime) = trip.checkDateTimeStop(position)
                    !validStopDate || !validStopTime},
                {
                    val (validStopDate, _) = trip.checkDateTimeStop(position)
                    if(!validStopDate){
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

            placeView.editText!!.isFocusable = false
            placeView.editText!!.isFocusableInTouchMode = false
            placeView.editText!!.isClickable = true

            placeView.editText!!.setOnClickListener {

                (context as AppCompatActivity).supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment)
                    ?.childFragmentManager?.fragments?.get(0)
                    ?.setFragmentResultListener(SearchLocationFragment.REQUEST_KEY) { key, bundle ->
                        placeView.editText!!.setText(bundle.getString(SearchLocationFragment.location) ?: "")
                        stop.geoPoint = bundle.getParcelable(SearchLocationFragment.geopoint)
                    }
                Log.d(getLogTag(), "from stop to search location")
                navController.navigate(R.id.action_tripEditFragment_to_searchLocationFragment,
                    bundleOf(SearchLocationFragment.location to stop.place,
                        SearchLocationFragment.geopoint to stop.geoPoint)
                )
            }

            placeViewWatcher = Watcher(
                { placeView.editText?.text?.isEmpty() ?: true },
                { placeView.error = context.getString(R.string.stop_place_error)
                    stop.place = placeView.editText?.text.toString()
                    (context as AppCompatActivity).invalidateOptionsMenu() },
                { placeView.error = null
                    stop.place = placeView.editText?.text.toString()
                    (context as AppCompatActivity).invalidateOptionsMenu() }
            )
            placeView.editText?.setText(stop.place)
            placeView.hint = context.getString(R.string.select_location)
            placeView?.editText?.addTextChangedListener(placeViewWatcher)

            removeButton.setOnClickListener {
                parent.remove(position)
            }

            hourView.editText?.setText(Hour(stop.dateTime).toString())
            hourView.editText?.setOnClickListener {
                if (timePicker ==null || !timePicker!!.isVisible) {
                    timePicker = getTimePicker(
                        hourView.editText!!,
                        stop.dateTime,
                        context
                    ) {
                        stop.dateTime.updateTime(it)
                    }
                    timePicker!!.show(
                        (context as AppCompatActivity).supportFragmentManager,
                        "timePickerTag"
                    )
                }
            }
            hourView.editText?.addTextChangedListener(dateTimeWatcher)

            dateView.editText?.setText(df.format(stop.dateTime.time))
            dateView.editText?.setOnClickListener {
                if(!datePicker.isVisible)
                    datePicker.show((context as AppCompatActivity).supportFragmentManager, "datePickerTag")
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
        holder.bind(trip.stops[position], position, navController)
    }

    fun add(stop: Stop) {
        trip.stops.add(stop)
        this.notifyItemInserted(trip.stops.size - 1)
    }

    fun remove(position : Int) {
        trip.stops.removeAt(position)
        this.notifyItemRemoved(position)
        this.notifyItemRangeChanged(position, trip.stops.size-position)
    }

}