package it.polito.mad.group27.carpooling.ui.trip.tripedit

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.BaseFragmentWithToolbar
import it.polito.mad.group27.carpooling.ui.trip.Hour
import it.polito.mad.group27.carpooling.ui.trip.Trip
import java.text.DateFormat
import java.util.*

class TripEditFragment : BaseFragmentWithToolbar(R.layout.trip_edit_fragment,
    R.menu.edit_menu,
    R.string.trip_edit_title) {
    //TODO change title to add (?)

    private lateinit var viewModel: TripEditViewModel

    private val trip = arguments?.getParcelable<Trip>("trip") ?: Trip()
    private val newTrip = trip.copy()
    private var datePicker: MaterialDatePicker<Long>

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

        val date = view.findViewById<TextView>(R.id.editDateText)
        date.text = df.format(newTrip.date)
        date.setOnClickListener {
            if(!datePicker.isVisible)
                datePicker.show(requireActivity().supportFragmentManager, "datePickerTag")
        }

        val from = view.findViewById<LinearLayout>(R.id.editFrom)
        (from.children.filter { it is TextInputLayout }.first() as TextInputLayout).hint = "From"
        val timePickerFrom = getTimePicker(
            from.findViewWithTag<TextInputEditText>("editHour"),
            newTrip.startHour){
            newTrip.startHour = Hour(it.hour, it.minute)
            newTrip.startHour
        }
        from.findViewWithTag<TextInputEditText>("editPlace").setText(newTrip.from)
        from.findViewWithTag<TextInputEditText>("editHour").setText(newTrip.startHour.toString())
        from.findViewWithTag<TextInputEditText>("editHour").setOnClickListener {
            if(!timePickerFrom.isVisible)
                timePickerFrom.show(requireActivity().supportFragmentManager, "timePickerTag")
        }

        val to = view.findViewById<LinearLayout>(R.id.editTo)
        (to.children.filter { it is TextInputLayout }.first() as TextInputLayout).hint = "To"
        to.findViewWithTag<TextInputEditText>("editPlace").setText(newTrip.to)
        to.findViewWithTag<TextInputEditText>("editHour").setText(newTrip.endHour.toString())

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
        val timePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour.hour)
                .setMinute(hour.minute)
                .build()
        timePicker.addOnPositiveButtonClickListener {
            val newHour: Hour = update(timePicker)
            view.text = newHour.toString()
        }
        return timePicker
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