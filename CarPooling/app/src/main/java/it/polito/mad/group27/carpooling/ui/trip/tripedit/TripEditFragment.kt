package it.polito.mad.group27.carpooling.ui.trip.tripedit

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputLayout.END_ICON_NONE
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.Watcher
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.BaseFragmentWithToolbar
import it.polito.mad.group27.carpooling.ui.EditFragment
import it.polito.mad.group27.carpooling.ui.trip.Hour
import it.polito.mad.group27.carpooling.ui.trip.Stop
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

    var price: TextInputLayout? = null
    var to_place: TextInputLayout? = null
    var from_place: TextInputLayout? = null
    var passengers: TextInputLayout? = null

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
        from_place = from.findViewById<TextInputLayout>(R.id.stop_place)
        val from_hour = from.findViewById<TextInputLayout>(R.id.stop_hour)
        from_place?.hint = "From"
        from_place?.editText?.setText(newTrip.from)
        from_place?.editText?.addTextChangedListener(Watcher(
            { from_place?.editText?.text?.isEmpty() ?: true },
            { from_place?.error = "Departure can not be empty"
                act.invalidateOptionsMenu() },
            { from_place?.error = null
                act.invalidateOptionsMenu() }
        ))
        from_hour.editText?.setText(newTrip.startHour.toString())
        from_hour.editText?.setOnClickListener {
            if(timePickerFrom == null || !timePickerFrom?.isVisible!!) {
                timePickerFrom = getTimePicker(
                    from_hour.editText!!,
                    newTrip.startHour,
                    this.requireContext()){
                    newTrip.startHour.updateTime(it)
                }
                timePickerFrom!!.show(requireActivity().supportFragmentManager, "timePickerTag")
            }

        }

        val to = view.findViewById<LinearLayout>(R.id.editTo)
        to_place = to.findViewById<TextInputLayout>(R.id.stop_place)
        val to_hour = to.findViewById<TextInputLayout>(R.id.stop_hour)
        to_place?.hint = "To"
        to_place?.editText?.setText(newTrip.to)
        to_place?.editText?.addTextChangedListener(Watcher(
            { to_place?.editText?.text?.isEmpty() ?: true },
            { to_place?.error = "Destination can not be empty"
                act.invalidateOptionsMenu() },
            { to_place?.error = null
                act.invalidateOptionsMenu() }
        ))
        to_hour.editText?.setText(newTrip.endHour.toString())
        to_hour.editText?.setOnClickListener {
            if(timePickerTo == null || !timePickerTo?.isVisible!!) {
                timePickerTo = getTimePicker(
                    to_hour.editText!!,
                    newTrip.endHour,
                    this.requireContext()){
                    newTrip.endHour.updateTime(it)
                }
                timePickerTo!!.show(requireActivity().supportFragmentManager, "timePickerTag")
            }
        }
        to_hour.editText?.addTextChangedListener(Watcher(
            { to_hour.editText?.text.toString() <= from_hour?.editText?.text.toString() },
            { to_hour.error = "Invalid arrival time"
                act.invalidateOptionsMenu() },
            { to_hour.error = null
                act.invalidateOptionsMenu() }
        ))

        passengers = view.findViewById<TextInputLayout>(R.id.editPeopleText)
        trip.tot_places?.let { passengers?.editText?.setText(it) }

        price = view.findViewById<TextInputLayout>(R.id.editPriceText)
        val price_format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        trip.price?.let { price?.editText?.setText(price_format.format(it)) }
        price?.editText?.addTextChangedListener(Watcher(
            { price?.editText?.text?.isEmpty() ?: true
                    || price?.editText?.text?.trim()?.split("[,.]".toRegex())?.size ?: 3 > 2
                    || if(price?.editText?.text?.trim()?.split("[,.]".toRegex())?.size ?: 0 == 2)
                            price?.editText?.text?.trim()?.split("[,.]".toRegex())?.get(1)?.length ?: 3 > 2
                        else false},
            { price?.error = "Invalid price"
                act.invalidateOptionsMenu() },
            { price?.error = null
                act.invalidateOptionsMenu() }
        ))

        estimated_time =  view.findViewById<EditText>(R.id.estimated_time)
        setEstimatedTime()

        val stops_rv = view.findViewById<RecyclerView>(R.id.stop_list_rv)
        stops_rv.layoutManager = LinearLayoutManager(this.context)
        stops_rv.adapter = StopRecyclerViewAdapter(newTrip.stops, this.requireContext())

        val remove_button = view.findViewById<Button>(R.id.remove_button)
        remove_button.visibility = View.INVISIBLE
        remove_button.setOnClickListener {
            (stops_rv.adapter as StopRecyclerViewAdapter).remove()
            if (newTrip.stops.size == 0)
                remove_button.visibility = View.INVISIBLE
        }

        val add_button = view.findViewById<Button>(R.id.add_button)
        add_button.setOnClickListener {
            (stops_rv.adapter as StopRecyclerViewAdapter).add(Stop("", Hour(0,0)))
            if (newTrip.stops.size > 0)
                remove_button.visibility = View.VISIBLE
        }

        //TODO swappare add e remove buttons
        // TODO aggiungere colori sensati ai bottoni
        //TODO check prima di aggiungere una fermata che le altre abbiano i campi e mettere errore sotto il bottone

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

    private fun getEstimatedTime(start: Hour, end: Hour): Hour {
        val start_minutes = start.minute + start.hour*60
        val end_minutes = end.minute + end.hour*60
        val hours = ((end_minutes-start_minutes)/60)
        val minutes = (end_minutes-start_minutes)%60
        return Hour(hours, minutes)
    }

    private fun setEstimatedTime(){
        val time = getEstimatedTime(newTrip.startHour, newTrip.endHour)
        val hours = if (time.hour > 0) "${time.hour} h" else ""
        val minutes = if (time.minute > 0) "${time.minute} min" else ""
        estimated_time.setText( "Estimated travel time : ${hours} ${minutes}" )
    }

    private fun Hour.updateTime(timePicker: MaterialTimePicker): Hour {
        this.hour = timePicker.hour
        this.minute = timePicker.minute
        setEstimatedTime()
        return this
    }

    fun saveTrip(){
        // check id, if -1 take counter and increment
    }
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
//        menu.findItem(R.id.save_menu_button).isEnabled = validateFields()
    // TODO
    }

    private fun validateFields(){

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