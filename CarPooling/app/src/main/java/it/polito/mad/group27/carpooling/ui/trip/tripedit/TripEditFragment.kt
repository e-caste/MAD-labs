package it.polito.mad.group27.carpooling.ui.trip.tripedit

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.BaseFragmentWithToolbar
import it.polito.mad.group27.carpooling.ui.EditFragment
import it.polito.mad.group27.carpooling.ui.trip.Hour
import it.polito.mad.group27.carpooling.ui.trip.Trip
import java.text.DateFormat
import java.text.NumberFormat
import java.util.*

class TripEditFragment : EditFragment(R.layout.trip_edit_fragment,
    R.menu.edit_menu,
    R.string.trip_edit_title) {
    //TODO change title to add (?)

    private lateinit var viewModel: TripEditViewModel

    private val trip = arguments?.getParcelable<Trip>("trip") ?: Trip()
    private val newTrip = trip.copy()
    private var datePicker: MaterialDatePicker<Long>
    private var timePickerFrom: MaterialTimePicker? = null
    private var timePickerTo: MaterialTimePicker? = null
    lateinit var estimated_time: TextView

    val df: DateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())

    init {
        datePicker = getDatePicker()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TripEditViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fab = view.findViewById<FloatingActionButton>(R.id.fab)

        registerForContextMenu(fab)
        fab.setOnClickListener {
            Log.d(getLogTag(), "image button clicked")
            act.openContextMenu(fab)
        }

        imageView= view.findViewById(R.id.car_image)

        val date = view.findViewById<TextView>(R.id.editDateText)
        date.text = df.format(newTrip.date)
        date.setOnClickListener {
            if(!datePicker.isVisible)
                datePicker.show(requireActivity().supportFragmentManager, "datePickerTag")
        }

        val from = view.findViewById<LinearLayout>(R.id.editFrom)
        (from.children.filter { it is TextInputLayout }.first() as TextInputLayout).hint = "From"
        from.findViewWithTag<TextInputEditText>("editPlace").setText(newTrip.from)
        from.findViewWithTag<TextInputEditText>("editHour").setText(newTrip.startHour.toString())
        from.findViewWithTag<TextInputEditText>("editHour").setOnClickListener {
            if(timePickerFrom == null || !timePickerFrom?.isVisible!!) {
                timePickerFrom = getTimePicker(
                    from.findViewWithTag<TextInputEditText>("editHour"),
                    newTrip.startHour){
                    newTrip.startHour.updateTime(it)
                }
                timePickerFrom!!.show(requireActivity().supportFragmentManager, "timePickerTag")
            }

        }

        val to = view.findViewById<LinearLayout>(R.id.editTo)
        (to.children.filter { it is TextInputLayout }.first() as TextInputLayout).hint = "To"
        to.findViewWithTag<TextInputEditText>("editPlace").setText(newTrip.to)
        to.findViewWithTag<TextInputEditText>("editHour").setText(newTrip.endHour.toString())
        to.findViewWithTag<TextInputEditText>("editHour").setOnClickListener {
            if(timePickerTo == null || !timePickerTo?.isVisible!!) {
                timePickerTo = getTimePicker(
                    to.findViewWithTag<TextInputEditText>("editHour"),
                    newTrip.endHour){
                    newTrip.endHour.updateTime(it)
                }
                timePickerTo!!.show(requireActivity().supportFragmentManager, "timePickerTag")
            }

        }

        val passengers = view.findViewById<TextInputEditText>(R.id.editPeopleText)
        trip.tot_places?.let { passengers.setText(it) }

        val price = view.findViewById<TextInputEditText>(R.id.editPriceText)
        val price_format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        trip.price?.let { price.setText(price_format.format(it)) }

        estimated_time =  view.findViewById<EditText>(R.id.estimated_time)
        setEstimatedTime()
    }

    private fun getDatePicker() : MaterialDatePicker<Long> {
        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraintsBuilder.build())
                .build()
        datePicker.addOnPositiveButtonClickListener {
            newTrip.date = Date(datePicker.selection!!)
            this.view?.findViewById<TextView>(R.id.editDateText)?.text = df.format(newTrip.date)
            Log.d(getLogTag(), newTrip.date.toString())
        }
        return datePicker
    }

    private fun getTimePicker(view: TextView, hour: Hour, update: (MaterialTimePicker) -> Hour): MaterialTimePicker{
        // TODO select 12H or 24H basing on Locale.getDefault()
        val timePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(hour.hour)
                .setMinute(hour.minute)
                .build()
        timePicker.addOnPositiveButtonClickListener {
            val newHour: Hour = update(timePicker)
            Log.d(getLogTag(), "updated hour with $newHour")
            view.text = newHour.toString()
            setEstimatedTime()
        }
        timePicker.addOnDismissListener {
            timePicker.dismiss()
        }
        return timePicker
    }

    private fun Hour.updateTime(timePicker: MaterialTimePicker): Hour{
        Log.d(getLogTag(), newTrip.startHour.toString())
        this.hour = timePicker.hour
        this.minute = timePicker.minute
        return this
    }

    private fun getEstimatedTime(start: Hour, end: Hour): Hour {
        val start_minutes = start.minute + start.hour*60
        val end_minutes = end.minute + end.hour*60
        val hours = ((end_minutes-start_minutes)/60)
        val minutes = (end_minutes-start_minutes)%60
        return Hour(hours, minutes)
    }

    private fun setEstimatedTime(){
        val time = getEstimatedTime(newTrip.startHour, newTrip.endHour)
        val hours = if (time.hour != 0) "${time.hour} h" else ""
        val minutes = if (time.minute != 0) "${time.minute} min" else ""
        estimated_time.setText( "Estimated travel time : ${hours} ${minutes}" )
    }

    fun saveTrip(){
        // check id, if -1 take counter and increment
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save_menu_button->{
                //TODO
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

}