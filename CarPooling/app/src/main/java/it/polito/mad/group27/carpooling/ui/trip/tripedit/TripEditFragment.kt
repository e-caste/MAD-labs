package it.polito.mad.group27.carpooling.ui.trip.tripedit

import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.Watcher
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.EditFragment
import it.polito.mad.group27.carpooling.ui.trip.Hour
import it.polito.mad.group27.carpooling.ui.trip.Option
import it.polito.mad.group27.carpooling.ui.trip.Stop
import it.polito.mad.group27.carpooling.ui.trip.Trip
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.math.BigDecimal
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class TripEditFragment : EditFragment(R.layout.trip_edit_fragment,
    R.menu.edit_menu,
    R.string.trip_edit_title) {
    //TODO change title to add (?)

    private lateinit var viewModel: TripEditViewModel

    private lateinit var trip : Trip
    private lateinit var newTrip : Trip

    lateinit var price: TextInputLayout
    lateinit var to_place: TextInputLayout
    lateinit var to_date: TextInputLayout
    lateinit var to_hour: TextInputLayout
    lateinit var from_place: TextInputLayout
    lateinit var from_date: TextInputLayout
    lateinit var from_hour: TextInputLayout
    lateinit var passengers: TextInputLayout

    private lateinit var datePickerFrom: MaterialDatePicker<Long>
    private lateinit var datePickerTo: MaterialDatePicker<Long>
    private lateinit var timePickerFrom: MaterialTimePicker
    private lateinit var timePickerTo: MaterialTimePicker
    private lateinit var estimated_time: TextView

    val df: DateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())
    val YYYYMMDD: DateFormat = SimpleDateFormat("yyyyddMM")

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TripEditViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get trip from bundle
        trip = arguments?.getParcelable<Trip>("trip") ?: Trip()
        newTrip = trip.copy()
        Log.d(getLogTag(), "got from bundle trip: $trip")

        // add action to fab
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        registerForContextMenu(fab)
        fab.setOnClickListener {
            Log.d(getLogTag(), "image button clicked")
            act.openContextMenu(fab)
        }

        // set image if present in trip object
        imageView= view.findViewById(R.id.car_image)
        if(newTrip.carImageUri != null){
            image = MediaStore.Images.Media.getBitmap(act.contentResolver, newTrip.carImageUri)
            if(image != null)
                imageView.setImageBitmap(image)
        }

        // from and to datetime check
        val dateTimeWatcher = Watcher(
            {
                YYYYMMDD.format(newTrip.startDateTime) > YYYYMMDD.format(newTrip.endDateTime)
                        || (YYYYMMDD.format(newTrip.startDateTime) == YYYYMMDD.format(newTrip.endDateTime)
                        && from_hour.editText?.text.toString() > to_hour.editText?.text.toString())
            },
            {
                if (YYYYMMDD.format(newTrip.startDateTime) > YYYYMMDD.format(newTrip.endDateTime)) {
                    to_date.error = getString(R.string.to_date_error)
                    to_hour.error = null
                } else {
                    to_hour.error = getString(R.string.edit_to_hour_error)
                    to_date.error = null
                }
            },
            {
                from_date.error = null
                to_hour.error = null
            }
        )

        val from = view.findViewById<LinearLayout>(R.id.editFrom)
        from_place = from.findViewById<TextInputLayout>(R.id.stop_place)
        from_date = from.findViewById<TextInputLayout>(R.id.stop_date)
        from_hour = from.findViewById<TextInputLayout>(R.id.stop_hour)
        from_date.editText?.setText(df.format(newTrip.startDateTime))

        datePickerFrom = getDatePicker(newTrip.startDateTime, from_date)
        from_date.editText?.setOnClickListener {
            if(!datePickerFrom.isVisible)
                datePickerFrom.show(requireActivity().supportFragmentManager, "datePickerTag")
        }

        val to = view.findViewById<LinearLayout>(R.id.editTo)
        to_place = to.findViewById<TextInputLayout>(R.id.stop_place)
        to_hour = to.findViewById<TextInputLayout>(R.id.stop_hour)
        to_date = to.findViewById<TextInputLayout>(R.id.stop_date)

        datePickerTo = getDatePicker(newTrip.endDateTime, to_date)
        to_date.editText?.setOnClickListener {
            if(!datePickerTo.isVisible)
                datePickerTo.show(requireActivity().supportFragmentManager, "datePickerTag")
        }

        from_place.hint = getString(R.string.from)
        from_place.editText?.setText(newTrip.from)
        from_place.editText?.addTextChangedListener(Watcher(
            { from_place.editText?.text?.isEmpty() ?: true },
            { from_place.error = getString(R.string.edit_from_error)
                newTrip.from = from_place.editText?.text.toString()
                 },
            { from_place.error = null
                newTrip.from = from_place.editText?.text.toString()
                 }
        ))
        from_hour.editText?.setText(Hour(newTrip.startDateTime).toString())
        from_hour.editText?.setOnClickListener {
            if(!timePickerFrom.isVisible) {
                timePickerFrom = getTimePicker(
                    from_hour.editText!!,
                    newTrip.startDateTime,
                    this.requireContext()){
                    newTrip.startDateTime.updateTime(it)
                }
                timePickerFrom!!.show(requireActivity().supportFragmentManager, "timePickerTag")
            }
        }
        from_date.editText?.setText(df.format(newTrip.startDateTime))
        from_date.editText?.addTextChangedListener(dateTimeWatcher)
        from_hour.editText?.addTextChangedListener(dateTimeWatcher)

        to_place.hint = getString(R.string.to)
        to_place.editText?.setText(newTrip.to)
        to_place.editText?.addTextChangedListener(Watcher(
            { to_place.editText?.text?.isEmpty() ?: true || to_place.editText?.text == from_place.editText?.text},
            { to_place.error = getString(R.string.edit_to_error)
                newTrip.to = to_place.editText?.text.toString()
                 },
            { to_place.error = null
                newTrip.to = to_place.editText?.text.toString()
                 }
        ))
        to_hour.editText?.setText(Hour(newTrip.endDateTime).toString())
        to_hour.editText?.setOnClickListener {
            if(!timePickerTo.isVisible) {
                timePickerTo = getTimePicker(
                    to_hour.editText!!,
                    newTrip.endDateTime,
                    this.requireContext()){
                    newTrip.endDateTime.updateTime(it)
                }
                timePickerTo.show(requireActivity().supportFragmentManager, "timePickerTag")
            }
        }
        to_date.editText?.setText(df.format(newTrip.endDateTime))
        to_hour.editText?.addTextChangedListener(dateTimeWatcher)
        to_date.editText?.addTextChangedListener(dateTimeWatcher)

        passengers = view.findViewById<TextInputLayout>(R.id.editPeopleText)
        trip.totalSeats?.let { passengers.editText?.setText(it.toString()) }
        passengers.editText?.addTextChangedListener(Watcher(
            { passengers.editText?.text?.isEmpty() ?: true },
            { passengers.error = getString(R.string.insert_passengers)
                newTrip.totalSeats = -1
                // TODO make not stubbed when adding some logic
                newTrip.availableSeats = -1
             },
            { passengers.error = null
                newTrip.totalSeats = passengers.editText?.text?.toString()?.toInt()
                // TODO make not stubbed when adding some logic
                newTrip.availableSeats = (0 .. newTrip.totalSeats!!).random()
                 }
        ))

        price = view.findViewById<TextInputLayout>(R.id.editPriceText)
        trip.price?.let { price.editText?.setText(it.toString()) }
        price.editText?.addTextChangedListener(Watcher(
            { price.editText?.text?.isEmpty() ?: true
                    || price.editText?.text?.trim()?.split("[,.]".toRegex())?.size ?: 3 > 2
                    || if(price.editText?.text?.trim()?.split("[,.]".toRegex())?.size ?: 0 == 2)
                            price.editText?.text?.trim()?.split("[,.]".toRegex())?.get(1)?.length ?: 3 > 2
                        else false},
            { price.error = getString(R.string.invalid_price)
                newTrip.price = BigDecimal("-1.00").setScale(2)
                 },
            { price.error = null
                newTrip.price = BigDecimal(price.editText!!.text!!.toString()).setScale(2)
                 }
        ))

        estimated_time =  view.findViewById<EditText>(R.id.estimated_time)
        setEstimatedTime()

        val stops_rv = view.findViewById<RecyclerView>(R.id.stop_list_rv)
        stops_rv.layoutManager = LinearLayoutManager(this.context)
        stops_rv.adapter = StopRecyclerViewAdapter(newTrip, this.requireContext())

        val remove_button = view.findViewById<Button>(R.id.remove_button)
        remove_button.visibility = View.INVISIBLE if(newTrip.stops.size == 0) else View.VISIBLE
        remove_button.setOnClickListener {
            (stops_rv.adapter as StopRecyclerViewAdapter).remove()
            if (newTrip.stops.size == 0)
                remove_button.visibility = View.GONE
        }

        val add_button = view.findViewById<Button>(R.id.add_button)
        add_button.setOnClickListener {
            val lastStop = newTrip.stops.size -1
            if (lastStop < 0
                || ( newTrip.stops[lastStop].place.trim() != ""
                && (if (lastStop > 0) {
                    newTrip.stops[lastStop].dateTime > newTrip.stops[lastStop - 1].dateTime
                }
                    else true)
                        &&  newTrip.stops[lastStop].dateTime < newTrip.endDateTime
                        && newTrip.stops[lastStop].dateTime > newTrip.startDateTime) ) {
                            val calendar_init = Calendar.getInstance()
                            calendar_init.set(Calendar.HOUR, 0)
                            calendar_init.set(Calendar.MINUTE, 0)
                (stops_rv.adapter as StopRecyclerViewAdapter).add(Stop("", calendar_init ))
                if (newTrip.stops.size > 0)
                    remove_button.visibility = View.VISIBLE
            }else{
                Snackbar.make(requireView(),  getString(R.string.toast_complete_previous_stop), Snackbar.LENGTH_LONG).show()
            }
        }

        val option_luggage = view.findViewById<SwitchMaterial>(R.id.luggage_switch)
        val option_animals = view.findViewById<SwitchMaterial>(R.id.animal_switch)
        val option_smokers = view.findViewById<SwitchMaterial>(R.id.smokers_switch)
        option_animals.isChecked = newTrip.options.contains(Option.ANIMALS)
        option_luggage.isChecked = newTrip.options.contains(Option.LUGGAGE)
        option_smokers.isChecked = newTrip.options.contains(Option.SMOKE)

        val additional_info = view.findViewById<TextInputEditText>(R.id.additionalInfo)
        additional_info.setText(newTrip.otherInformation)
        additional_info.addTextChangedListener(Watcher(
            { true },
            { newTrip.otherInformation = additional_info.text.toString() },
            { newTrip.otherInformation = additional_info.text.toString() }
        ))

    }

    private fun getDatePicker(calendar: Calendar, view: TextInputLayout) : MaterialDatePicker<Long> {
        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraintsBuilder.build())
                .build()
        datePicker.addOnPositiveButtonClickListener {
            calendar.timeInMillis = datePicker.selection!!
            view.editText?.setText(df.format(newTrip.startDateTime))
            Log.d(getLogTag(), newTrip.startDateTime.toString())
        }
        return datePicker
    }

    private fun getEstimatedTime(start: Calendar, end: Calendar): Hour {
        val delta_minutes = (start.getTimeInMillis() - end.getTimeInMillis()) / (1000*60)
        val hours = ((delta_minutes)/60).toInt()
        val minutes = ((delta_minutes)%60).toInt()
        return Hour(hours, minutes)
    }

    private fun setEstimatedTime(){
        val time = getEstimatedTime(newTrip.startDateTime, newTrip.endDateTime)
        val hours = if (time.hour > 0) "${time.hour} h" else ""
        val minutes = if (time.minute > 0) "${time.minute} min" else ""
        estimated_time.setText( getString(R.string.estimated_time) + " : ${hours} ${minutes}" )
    }

    private fun Calendar.updateTime(timePicker: MaterialTimePicker): Calendar {
        this.set(Calendar.HOUR, timePicker.hour)
        this.set(Calendar.MINUTE, timePicker.minute)
        setEstimatedTime()
        return this
    }

    private fun saveTrip(){

        val sharedPref = act.getPreferences(Context.MODE_PRIVATE)!!

        // check id, if -1 take counter and increment
        if(newTrip.id == null) {
            val counterKey = getString(R.string.trip_counter)
            val counter = sharedPref.getLong(counterKey, 0)
            newTrip.id = counter + 1
            with(sharedPref.edit()) {
                putString(counterKey, (counter+1).toString())
                apply()
            }
        }
        val imageName = "${getString(R.string.car_image_prefix)}${newTrip.id}"
        if (newTrip.carImageUri == null && image!=null){
            val f = File(act.filesDir, imageName)
            newTrip.carImageUri = f.toUri()
        }
        if(image==null) {
            if(newTrip.carImageUri != null){
                //delete old image
                File(newTrip.carImageUri!!.path!!).delete()
            }
            newTrip.carImageUri = null
        }

        val optionToSwitch = mapOf(Option.LUGGAGE to R.id.luggage_switch,
            Option.SMOKE to R.id.smokers_switch,
            Option.ANIMALS to R.id.animal_switch)
        for ((option, switchId) in optionToSwitch.entries){
            val switch = requireView().findViewById<SwitchMaterial>(switchId)
            if(switch.isChecked)
                newTrip.options.add(option)
        }

        val info = requireView().findViewById<TextInputEditText>(R.id.additionalInfo)
        newTrip.otherInformation = info.text?.toString() ?: null

        Log.d(getLogTag(), Json.encodeToString(newTrip))
        writeParcelable(newTrip, "${getString(R.string.trip_prefix)}${newTrip.id}")
        saveImg(imageName)
    }


    private fun validateFields(): Boolean{
        var valid = true
        if(newTrip.from.trim() =="") {
            from_place.error = getString(R.string.edit_from_error)
            valid = false
        }
        if(newTrip.to.trim()=="" || newTrip.to == newTrip.from){
            to_place.error = getString(R.string.edit_to_error)
            valid= false
        }


        if(newTrip.price == null || newTrip.price!! <= BigDecimal(0)){
            price.error =  getString(R.string.invalid_price)
            valid = false
        }

        if(newTrip.totalSeats == null || newTrip.totalSeats!! < 0 ){
            passengers.error = getString(R.string.insert_passengers)
            valid= false
        }

        if (YYYYMMDD.format(newTrip.startDateTime) > YYYYMMDD.format(newTrip.endDateTime)) {
            to_date.error = getString(R.string.to_date_error)
            to_hour.error = null
            valid = false
        } else {
            to_hour.error = getString(R.string.edit_to_hour_error)
            to_date.error = null
            valid = false
        }

        // TODO set as field
        val stops_rv = requireView().findViewById<RecyclerView>(R.id.stop_list_rv)

        for ((idx, stop) in newTrip.stops.withIndex()){
            var validStopTime = true
            if(idx == 0){
                if(stop.hour.toString() <= newTrip.startHour.toString()){
                    validStopTime = false
                }
            }else if(idx== newTrip.stops.size -1){
                if(stop.hour.toString() >= newTrip.endHour.toString()){
                    validStopTime = false
                }
            }else{
                if(stop.hour.toString() <= newTrip.stops[idx-1].hour.toString()){
                    validStopTime = false
                }
            }

            if(!validStopTime){
                valid = false
                stops_rv[idx].findViewById<TextInputLayout>(R.id.stop_hour).error = getString(R.string.stop_hour_error)
            }

            if(stop.place.trim()==""){
                stops_rv[idx].findViewById<TextInputLayout>(R.id.stop_place).error = getString(R.string.stop_place_error)
                valid = false
            }
        }

        val places = newTrip.stops.groupingBy{it.place }.eachCount()



        if(places.containsKey(newTrip.from) || places.containsKey(newTrip.to)){

            for ((idx, stop) in newTrip.stops.withIndex()){
                if(stop.place.trim()!="" && (stop.place == newTrip.from || stop.place == newTrip.to))
                    stops_rv[idx].findViewById<TextInputLayout>(R.id.stop_place).error = getString(R.string.duplicated_place_error)
            }
            valid = false
        }
        val duplicatedStops = places.filterValues { it >1  }.keys
        if(duplicatedStops.isNotEmpty()){
            for ((idx, stop) in newTrip.stops.withIndex()){
                if(stop.place.trim()!="" && stop.place in duplicatedStops)
                    stops_rv[idx].findViewById<TextInputLayout>(R.id.stop_place).error = getString(R.string.duplicated_place_error)
            }
            valid = false
        }



        return valid
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save_menu_button->{
                if(validateFields()) {
                    saveTrip()
                    // detect where to go if added or modified
                    findNavController().navigate(
                        R.id.action_tripEditFragment_to_tripDetailsFragment,
                        bundleOf("trip" to newTrip)
                    )
                }else{
                    Snackbar.make(requireView(),getString(R.string.fix_all_errors), Snackbar.LENGTH_LONG).show()
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

}