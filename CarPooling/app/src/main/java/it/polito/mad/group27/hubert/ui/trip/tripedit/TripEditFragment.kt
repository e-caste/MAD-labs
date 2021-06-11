package it.polito.mad.group27.hubert.ui.trip.tripedit

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.firebase.auth.FirebaseAuth
import it.polito.mad.group27.hubert.*
import it.polito.mad.group27.hubert.ui.EditFragment
import it.polito.mad.group27.hubert.entities.Hour
import it.polito.mad.group27.hubert.entities.Option
import it.polito.mad.group27.hubert.entities.Stop
import it.polito.mad.group27.hubert.entities.Trip
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class TripEditFragment : EditFragment(R.layout.trip_edit_fragment,
    R.menu.edit_menu,
    R.string.trip_edit_title) {

    private lateinit var tripEditViewModel: TripEditViewModel

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
    private var timePickerFrom: MaterialTimePicker? = null
    private var timePickerTo: MaterialTimePicker? = null
    private lateinit var estimated_time: TextView

    private val df: DateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ITALY)
    private val YYYYMMDD: DateFormat = SimpleDateFormat("yyyyMMdd")

    private lateinit var scrollView: NestedScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(getLogTag(), "On create trip edit")
        tripEditViewModel = ViewModelProvider(this).get(TripEditViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(getLogTag(), "TripEditFragment onViewCreated")
        // get trip from bundle
        if(!tripEditViewModel.isNewtripInitialized()) {
            Log.d(getLogTag(), "Initializing new Trip")
            if (arguments?.getParcelable<Trip>("trip") == null) {
                tripEditViewModel.newTrip = Trip()
            } else if (arguments?.getParcelable<Trip>("trip") != null) {
                tripEditViewModel.newTrip = arguments?.getParcelable("trip") ?: Trip()
            }
        }

        if(tripEditViewModel.newTrip.id==null)
            updateTitle(getString(R.string.add_trip))

        Log.d(getLogTag(), "got from bundle trip: ${tripEditViewModel.newTrip}")

        // add action to fab
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        registerForContextMenu(fab)
        fab.setOnClickListener {
            Log.d(getLogTag(), "image button clicked")
            act.openContextMenu(fab)
        }

        // init scrollView
        scrollView = view.findViewById(R.id.edit_trip_scrollview)


        // set image if present in trip object
        imageView= view.findViewById(R.id.car_image)
        if(editViewModel.image == null && !editViewModel.imageChanged) {
            //first render (and no update)
            setImage(tripEditViewModel.newTrip.carImageUri?.toString(), true)
        }else reloadImage()


        // from and to datetime check
        val dateTimeWatcher = Watcher(
            {
                Log.d(getLogTag(), YYYYMMDD.format(tripEditViewModel.newTrip.startDateTime.time).toString()+" "+YYYYMMDD.format(Calendar.getInstance().time).toString())
                YYYYMMDD.format(tripEditViewModel.newTrip.startDateTime.time) > YYYYMMDD.format(tripEditViewModel.newTrip.endDateTime.time)
                        || (YYYYMMDD.format(tripEditViewModel.newTrip.startDateTime.time) == YYYYMMDD.format(tripEditViewModel.newTrip.endDateTime.time)
                        && from_hour.editText?.text.toString() > to_hour.editText?.text.toString())
                        || tripEditViewModel.newTrip.startDateTime <= Calendar.getInstance()
            },
            {
                if( tripEditViewModel.newTrip.startDateTime <= Calendar.getInstance()  )
                    from_date.error = getString(R.string.start_date_not_in_past)
                else from_date.error = null
                if (YYYYMMDD.format(tripEditViewModel.newTrip.startDateTime.time) > YYYYMMDD.format(tripEditViewModel.newTrip.endDateTime.time)) {
                    to_date.error = getString(R.string.date_error)
                    to_hour.error = null
                } else if(YYYYMMDD.format(tripEditViewModel.newTrip.startDateTime.time) == YYYYMMDD.format(tripEditViewModel.newTrip.endDateTime.time)
                    && from_hour.editText?.text.toString() > to_hour.editText?.text.toString()){
                    to_hour.error = getString(R.string.edit_to_hour_error)
                    to_date.error = null
                }
                setEstimatedTime()
            },
            {
                to_date.error = null
                to_hour.error = null
                from_date.error = null
                setEstimatedTime()
            }
        )

        val from = view.findViewById<LinearLayout>(R.id.editFrom)
        from_place = from.findViewById<TextInputLayout>(R.id.stop_place)
        from_place.editText!!.isFocusable = false
        from_place.editText!!.isFocusableInTouchMode = false
        from_place.editText!!.isClickable = true
        from_date = from.findViewById<TextInputLayout>(R.id.stop_date)
        from_hour = from.findViewById<TextInputLayout>(R.id.stop_hour)
        from_date.editText?.setText(df.format(tripEditViewModel.newTrip.startDateTime.time))

        datePickerFrom = getDatePicker(tripEditViewModel.newTrip.startDateTime, from_date)
        from_date.editText?.setOnClickListener {
            if(!datePickerFrom.isVisible)
                datePickerFrom.show(requireActivity().supportFragmentManager, "datePickerTag")
        }

        val to = view.findViewById<LinearLayout>(R.id.editTo)
        to_place = to.findViewById<TextInputLayout>(R.id.stop_place)
        to_place.editText!!.isFocusable = false
        to_place.editText!!.isFocusableInTouchMode = false
        to_place.editText!!.isClickable = true
        to_hour = to.findViewById<TextInputLayout>(R.id.stop_hour)
        to_date = to.findViewById<TextInputLayout>(R.id.stop_date)

        datePickerTo = getDatePicker(tripEditViewModel.newTrip.endDateTime, to_date)
        to_date.editText?.setOnClickListener {
            if(!datePickerTo.isVisible)
                datePickerTo.show(requireActivity().supportFragmentManager, "datePickerTag")
        }

        from_place.hint = getString(R.string.select_location)
        from_place.editText?.setText(tripEditViewModel.newTrip.from)
        from_place.editText!!.setOnClickListener {
            setFragmentResultListener(SearchLocationFragment.REQUEST_KEY) { key, bundle ->
                // read from the bundle
                from_place.editText!!.setText(bundle.getString(SearchLocationFragment.location) ?: "")
                tripEditViewModel.newTrip.fromGeoPoint = bundle.getParcelable(SearchLocationFragment.geopoint)
            }
            findNavController().navigate(R.id.action_tripEditFragment_to_searchLocationFragment,
            bundleOf(SearchLocationFragment.location to tripEditViewModel.newTrip.from,
            SearchLocationFragment.geopoint to tripEditViewModel.newTrip.fromGeoPoint))
        }
        from_place.editText?.addTextChangedListener(Watcher(
            { from_place.editText?.text?.isEmpty() ?: true },
            { from_place.error = getString(R.string.edit_from_error)
                tripEditViewModel.newTrip.from = from_place.editText?.text.toString()
                 },
            { from_place.error = null
                tripEditViewModel.newTrip.from = from_place.editText?.text.toString()
                 }
        ))
        from_hour.editText?.setText(Hour(tripEditViewModel.newTrip.startDateTime).toString())
        from_hour.editText?.setOnClickListener {
            if(timePickerFrom==null || !timePickerFrom!!.isVisible) {
                timePickerFrom = getTimePicker(
                    from_hour.editText!!,
                    tripEditViewModel.newTrip.startDateTime,
                    this.requireContext()){
                    tripEditViewModel.newTrip.startDateTime.updateTime(it)
                }
                timePickerFrom!!.show(requireActivity().supportFragmentManager, "timePickerTag")
            }
        }
        from_date.editText?.setText(df.format(tripEditViewModel.newTrip.startDateTime.time))
        from_date.editText?.addTextChangedListener(dateTimeWatcher)
        from_hour.editText?.addTextChangedListener(dateTimeWatcher)

        to_place.hint = getString(R.string.select_location)
        to_place.editText?.setText(tripEditViewModel.newTrip.to)
        to_place.editText!!.setOnClickListener {
            setFragmentResultListener(SearchLocationFragment.REQUEST_KEY) { key, bundle ->
                // read from the bundle
                to_place.editText!!.setText(bundle.getString(SearchLocationFragment.location) ?: "")
                tripEditViewModel.newTrip.toGeoPoint = bundle.getParcelable(SearchLocationFragment.geopoint)
            }
            findNavController().navigate(R.id.action_tripEditFragment_to_searchLocationFragment,
                bundleOf(SearchLocationFragment.location to tripEditViewModel.newTrip.to,
                    SearchLocationFragment.geopoint to tripEditViewModel.newTrip.toGeoPoint))
        }
        to_place.editText?.addTextChangedListener(Watcher(
            { to_place.editText?.text?.isEmpty() ?: true || to_place.editText?.text == from_place.editText?.text},
            { to_place.error = getString(R.string.edit_to_error)
                tripEditViewModel.newTrip.to = to_place.editText?.text.toString()
                 },
            { to_place.error = null
                tripEditViewModel.newTrip.to = to_place.editText?.text.toString()
                 }
        ))
        to_hour.editText?.setText(Hour(tripEditViewModel.newTrip.endDateTime).toString())
        to_hour.editText?.setOnClickListener {
            if(timePickerTo==null || !timePickerTo!!.isVisible) {
                timePickerTo = getTimePicker(
                    to_hour.editText!!,
                    tripEditViewModel.newTrip.endDateTime,
                    this.requireContext()){
                    tripEditViewModel.newTrip.endDateTime.updateTime(it)
                }
                timePickerTo!!.show(requireActivity().supportFragmentManager, "timePickerTag")
            }
        }
        to_date.editText?.setText(df.format(tripEditViewModel.newTrip.endDateTime.time))
        to_hour.editText?.addTextChangedListener(dateTimeWatcher)
        to_date.editText?.addTextChangedListener(dateTimeWatcher)

        passengers = view.findViewById<TextInputLayout>(R.id.editPeopleText)
        tripEditViewModel.newTrip.totalSeats?.let {
            passengers.editText?.setText(it.toString())
            tripEditViewModel.totalSeats.value = it
        }
        passengers.editText?.addTextChangedListener(Watcher(
            { passengers.editText?.text?.isEmpty() ?: true
                    || passengers.editText?.text?.toString()?.toIntOrNull() ?: 0 <= 0
                    || passengers.editText?.text?.toString()?.toIntOrNull() ?: 0 < tripEditViewModel.newTrip.acceptedUsersUids.size },
            { if(passengers.editText?.text?.isEmpty() ?: true || passengers.editText?.text?.toString()?.toIntOrNull() ?: 0 <= 0) {
                passengers.error = getString(R.string.insert_passengers)
            }
                else{
                passengers.error = resources.getQuantityString(R.plurals.already_accepted_n_travelers,
                                                        tripEditViewModel.newTrip.acceptedUsersUids.size,
                                                        tripEditViewModel.newTrip.acceptedUsersUids.size)
            }
                tripEditViewModel.newTrip.totalSeats = -1
             },
            { passengers.error = null
                tripEditViewModel.newTrip.totalSeats = passengers.editText?.text?.toString()?.toIntOrNull()
                tripEditViewModel.totalSeats.value = passengers.editText?.text?.toString()?.toIntOrNull()
                 }
        ))

        price = view.findViewById<TextInputLayout>(R.id.editPriceText)
        tripEditViewModel.newTrip.price?.let { price.editText?.setText(it.toString()) }
        price.editText?.addTextChangedListener(Watcher(
            { price.editText?.text?.isEmpty() ?: true
                    || price.editText?.text?.trim()?.split("[,.]".toRegex())?.size ?: 3 > 2
                    || if(price.editText?.text?.trim()?.split("[,.]".toRegex())?.size ?: 0 == 2)
                            price.editText?.text?.trim()?.split("[,.]".toRegex())?.get(1)?.length ?: 3 > 2
                        else false},
            { price.error = getString(R.string.invalid_price)
                tripEditViewModel.newTrip.price = BigDecimal("-1.00").setScale(2)
                 },
            { price.error = null
                tripEditViewModel.newTrip.price = BigDecimal(price.editText!!.text!!.toString()).setScale(2)
                 }
        ))

        estimated_time =  view.findViewById<EditText>(R.id.estimated_time)
        setEstimatedTime()

        val stops_rv = view.findViewById<RecyclerView>(R.id.stop_list_rv)
        stops_rv.layoutManager = LinearLayoutManager(this.context)
        stops_rv.adapter = StopRecyclerViewAdapter(tripEditViewModel.newTrip, this.requireContext(), findNavController())


        val add_button = view.findViewById<Button>(R.id.add_button)
        add_button.setOnClickListener {
            var valid = validateFields(true)

            if(valid){
                val calendar_init = Calendar.getInstance()
                calendar_init.set(Calendar.HOUR_OF_DAY, 0)
                calendar_init.set(Calendar.MINUTE, 0)
                (stops_rv.adapter as StopRecyclerViewAdapter).add(Stop("", calendar_init ))
            }else{
                Snackbar.make(requireView(),  getString(R.string.toast_complete_previous_stop), Snackbar.LENGTH_LONG).show()
            }
        }

        val option_luggage = view.findViewById<SwitchMaterial>(R.id.luggage_switch)
        val option_animals = view.findViewById<SwitchMaterial>(R.id.animal_switch)
        val option_smokers = view.findViewById<SwitchMaterial>(R.id.smokers_switch)
        option_animals.isChecked = tripEditViewModel.newTrip.options.contains(Option.ANIMALS)
        option_luggage.isChecked = tripEditViewModel.newTrip.options.contains(Option.LUGGAGE)
        option_smokers.isChecked = tripEditViewModel.newTrip.options.contains(Option.SMOKE)

        val additional_info = view.findViewById<TextInputEditText>(R.id.additionalInfo)
        additional_info.setText(tripEditViewModel.newTrip.otherInformation)
        additional_info.addTextChangedListener(Watcher(
            { true },
            { tripEditViewModel.newTrip.otherInformation = additional_info.text.toString() },
            { tripEditViewModel.newTrip.otherInformation = additional_info.text.toString() }
        ))

        val accepted_rv = view.findViewById<RecyclerView>(R.id.accepted_rv)
        val accepted_button = view.findViewById<ImageView>(R.id.expand_accepted_button)
        val accepted_title = view.findViewById<TextView>(R.id.accepted_users)
        accepted_rv.layoutManager = MyLinearLayoutManager(this.requireContext(), scrollView)
        accepted_rv.adapter = PassengerRecyclerViewAdapter(tripEditViewModel, null, requireContext())

        val interested_rv = view.findViewById<RecyclerView>(R.id.interested_rv)
        val interested_button = view.findViewById<ImageView>(R.id.expand_interested_button)
        val interested_title = view.findViewById<TextView>(R.id.interested_users)
        interested_rv.layoutManager = LinearLayoutManager(this.context)
        interested_rv.adapter = PassengerRecyclerViewAdapter(tripEditViewModel,
            accepted_rv.adapter as PassengerRecyclerViewAdapter,
            requireContext()
        )

        accepted_rv.visibility = tripEditViewModel.acceptedVisibility
        if (accepted_rv.visibility == View.VISIBLE)
            accepted_button.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
        else
            accepted_button.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)

        interested_rv.visibility = tripEditViewModel.interestedVisibility
        if (interested_rv.visibility == View.VISIBLE)
            interested_button.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
        else
            interested_button.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)

        val passengersListWrapper = view.findViewById<LinearLayout>(R.id.passengers_list_wrapper)
        if (tripEditViewModel.newTrip.id == null)
            passengersListWrapper.visibility = View.GONE
        else
            passengersListWrapper.visibility = View.VISIBLE

        tripEditViewModel.acceptedExpandVisibility.observe(viewLifecycleOwner){
            if(tripEditViewModel.interestedExpandVisibility.value == View.GONE
                && it == View.GONE)
                    passengersListWrapper.visibility = View.GONE
            else
                passengersListWrapper.visibility = View.VISIBLE
            accepted_button.visibility = it
            accepted_title.visibility = it
        }
        tripEditViewModel.interestedExpandVisibility.observe(viewLifecycleOwner){
            if(tripEditViewModel.acceptedExpandVisibility.value == View.GONE
                && it == View.GONE)
                passengersListWrapper.visibility = View.GONE
            else
                passengersListWrapper.visibility = View.VISIBLE
            interested_button.visibility = it
            interested_title.visibility = it
        }

        val expandButtonClickListener: (RecyclerView, ImageView) -> Unit = {
            view, button ->
            if(view.visibility == View.VISIBLE){
                view.visibility = View.GONE
                button.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
            }
            else{
                view.visibility = View.VISIBLE
                button.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
                scrollView.post {
                    scrollView.fullScroll(View.FOCUS_DOWN)
                }
            }
        }
        accepted_button.setOnClickListener {
            expandButtonClickListener(accepted_rv, it as ImageView)
            tripEditViewModel.acceptedVisibility = accepted_rv.visibility

        }
        interested_button.setOnClickListener {
            expandButtonClickListener(interested_rv, it as ImageView)
            tripEditViewModel.interestedVisibility = interested_rv.visibility
        }

        val stopAdvertiseButton = view.findViewById<Button>(R.id.stop_advertising)
        stopAdvertiseButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.alert_title))
                .setMessage(resources.getString(R.string.alert_message))
                .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
                    // Respond to neutral button press
                }
                .setPositiveButton(resources.getString(R.string.yes)) { dialog, which ->
                    // Respond to positive button press
                    tripEditViewModel.newTrip.advertised = false
                    saveTrip()
                    findNavController().navigate(
                        R.id.action_tripEditFragment_to_tripDetailsFragment,
                        bundleOf("tripId" to tripEditViewModel.newTrip.id)
                    )
                }
                .show()
        }

    }

    private fun getEstimatedTime(start: Calendar, end: Calendar): Hour {
        val delta_minutes = (end.timeInMillis - start.timeInMillis) / (1000*60)
        val hours = ((delta_minutes)/60).toInt()
        val minutes = ((delta_minutes)%60).toInt()
        return Hour(hours, minutes)
    }

    private fun setEstimatedTime(){
        val time = getEstimatedTime(tripEditViewModel.newTrip.startDateTime, tripEditViewModel.newTrip.endDateTime)
        val hours = if (time.hour > 0) "${time.hour} h" else ""
        val minutes = if (time.minute > 0) "${time.minute} min" else ""
        estimated_time.setText( getString(R.string.estimated_time) + " : ${hours} ${minutes}" )
    }

    private fun Calendar.updateTime(timePicker: MaterialTimePicker): Calendar {
        this.set(Calendar.HOUR_OF_DAY, timePicker.hour)
        this.set(Calendar.MINUTE, timePicker.minute)
        Log.d(getLogTag(), "update time to : ${timePicker.hour} : ${timePicker.minute}")
        return this
    }

    private fun saveTrip(){

        var created =  false
        if(tripEditViewModel.newTrip.id == null) {
            created = true
        }

        val optionToSwitch = mapOf(
            Option.LUGGAGE to R.id.luggage_switch,
            Option.SMOKE to R.id.smokers_switch,
            Option.ANIMALS to R.id.animal_switch)
        tripEditViewModel.newTrip.options.removeAll { true }
        for ((option, switchId) in optionToSwitch.entries){
            val switch = requireView().findViewById<SwitchMaterial>(switchId)
            if(switch.isChecked)
                tripEditViewModel.newTrip.options.add(option)
        }

        val info = requireView().findViewById<TextInputEditText>(R.id.additionalInfo)
        info.setText(tripEditViewModel.newTrip.otherInformation ?: "")

        if(created) {
            tripEditViewModel.newTrip.id = tripEditViewModel.getNewId()
            tripEditViewModel.newTrip.ownerUid = FirebaseAuth.getInstance().currentUser!!.uid
        }

        val tripDB = tripEditViewModel.newTrip.toTripDB()

        saveImg("carImages",  tripDB.id!!){ uri:String?, changed:Boolean ->
            if(changed)
                tripDB.carImageUri = uri
            tripEditViewModel.updateTrip(tripDB)
            sendNotifications()
        }

    }

    private fun sendNotifications(){
        // send notifications to newly accepted users
        for (uid in tripEditViewModel.newAcceptedUsers){
            MainScope().launch {
                //TODO add feedback
                MessagingService.sendNotification(
                    tripEditViewModel.getProfileByUid(uid).notificationToken,
                    AndroidNotification(
                        getString(R.string.request_accepted_title),
                        getString(
                            R.string.request_accepted_message,
                            tripEditViewModel.newTrip.from,
                            tripEditViewModel.newTrip.to,
                            df.format(tripEditViewModel.newTrip.startDateTime.time)
                        ),
                        tripEditViewModel.newTrip.carImageUri.toString()
                    )
                )
            }
        }

        //send notifications to interested users if trip has finished places
        if (tripEditViewModel.newTrip.acceptedUsersUids.size >= tripEditViewModel.newTrip.totalSeats ?: 0
            && tripEditViewModel.newAcceptedUsers.size > 0){ // with the newly accepted users, seats are finished
            for (uid in tripEditViewModel.newTrip.interestedUsersUids){
                //TODO add feedback
                MainScope().launch {
                    MessagingService.sendNotification(
                        tripEditViewModel.getProfileByUid(uid).notificationToken,
                        AndroidNotification(
                            getString(R.string.seats_finished_title),
                            getString(
                                R.string.seats_finished_message,
                                tripEditViewModel.newTrip.from,
                                tripEditViewModel.newTrip.to,
                                df.format(tripEditViewModel.newTrip.startDateTime.time)
                            ),
                            tripEditViewModel.newTrip.carImageUri.toString()
                        )
                    )
                }
            }
        }

        // send notifications to accepted users if driver has stopped advertising the trip
        if (!tripEditViewModel.newTrip.advertised) {
            for (uid in tripEditViewModel.newTrip.acceptedUsersUids) {
                if (!tripEditViewModel.newAcceptedUsers.contains(uid)) {
                    //TODO add feedback
                    MainScope().launch {
                        MessagingService.sendNotification(
                            tripEditViewModel.getProfileByUid(uid).notificationToken,
                            AndroidNotification(
                                getString(R.string.trip_cancelled_title),
                                getString(
                                    R.string.trip_cancelled_message,
                                    tripEditViewModel.newTrip.from,
                                    tripEditViewModel.newTrip.to,
                                    df.format(tripEditViewModel.newTrip.startDateTime.time)
                                ),
                                tripEditViewModel.newTrip.carImageUri.toString()
                            )
                        )
                    }
                }
            }
        }
    }


    private fun validateFields(onlyRoute: Boolean = false): Boolean{

        var valid = true
        if(tripEditViewModel.newTrip.from.trim() =="") {
            from_place.error = getString(R.string.edit_from_error)
            valid = false
        }
        if(tripEditViewModel.newTrip.to.trim()=="" || tripEditViewModel.newTrip.to == tripEditViewModel.newTrip.from){
            to_place.error = getString(R.string.edit_to_error)
            valid= false
        }


        if(!onlyRoute && (tripEditViewModel.newTrip.price == null || tripEditViewModel.newTrip.price!! <= BigDecimal(0))){
            price.error =  getString(R.string.invalid_price)
            valid = false
        }

        if(!onlyRoute && (tripEditViewModel.newTrip.totalSeats == null || tripEditViewModel.newTrip.totalSeats!! < 0 )){
            passengers.error = getString(R.string.insert_passengers)
            valid= false
        }else if(passengers.editText?.text?.toString()?.toIntOrNull() ?: 0 < tripEditViewModel.newTrip.acceptedUsersUids.size) {
            passengers.error = resources.getQuantityString(
                R.plurals.already_accepted_n_travelers,
                tripEditViewModel.newTrip.acceptedUsersUids.size,
                tripEditViewModel.newTrip.acceptedUsersUids.size
            )
            valid=false
        }

        if( tripEditViewModel.newTrip.startDateTime <= Calendar.getInstance() ) {
            from_date.error = getString(R.string.start_date_not_in_past)
            valid = false
        }

        if(tripEditViewModel.newTrip.otherInformation?.length ?: 0 > 100)
            valid = false

        if (YYYYMMDD.format(tripEditViewModel.newTrip.startDateTime.time) > YYYYMMDD.format(tripEditViewModel.newTrip.endDateTime.time)) {
            to_date.error = getString(R.string.date_error)
            to_hour.error = null
            valid = false
        } else if(YYYYMMDD.format(tripEditViewModel.newTrip.startDateTime.time) == YYYYMMDD.format(tripEditViewModel.newTrip.endDateTime.time)
            && from_hour.editText?.text.toString() > to_hour.editText?.text.toString()) {
            to_hour.error = getString(R.string.edit_to_hour_error)
            to_date.error = null
            valid = false
        }

        val stops_rv = requireView().findViewById<RecyclerView>(R.id.stop_list_rv)

        for ((idx, stop) in tripEditViewModel.newTrip.stops.withIndex()){
            val (validStopDate, validStopTime) = tripEditViewModel.newTrip.checkDateTimeStop(idx)

            if(!validStopTime){
                valid = false
                stops_rv[idx].findViewById<TextInputLayout>(R.id.stop_hour).error = getString(R.string.stop_hour_error)
            }
            if(!validStopDate){
                valid = false
                stops_rv[idx].findViewById<TextInputLayout>(R.id.stop_date).error = getString(R.string.date_error)
            }

            if(stop.place.trim()==""){
                stops_rv[idx].findViewById<TextInputLayout>(R.id.stop_place).error = getString(R.string.stop_place_error)
                valid = false
            }
        }


        val places = tripEditViewModel.newTrip.stops.groupingBy{it.place }.eachCount()



        if(places.containsKey(tripEditViewModel.newTrip.from) || places.containsKey(tripEditViewModel.newTrip.to)){

            for ((idx, stop) in tripEditViewModel.newTrip.stops.withIndex()){
                if(stop.place.trim()!="" && (stop.place == tripEditViewModel.newTrip.from || stop.place == tripEditViewModel.newTrip.to))
                    stops_rv[idx].findViewById<TextInputLayout>(R.id.stop_place).error = getString(R.string.duplicated_place_error)
            }
            valid = false
        }
        val duplicatedStops = places.filterValues { it >1  }.keys
        if(duplicatedStops.isNotEmpty()){
            for ((idx, stop) in tripEditViewModel.newTrip.stops.withIndex()){
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
                        bundleOf("tripId" to tripEditViewModel.newTrip.id)
                    )
                }else{
                    Snackbar.make(requireView(),getString(R.string.fix_all_errors), Snackbar.LENGTH_LONG).show()
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private class MyLinearLayoutManager(val context: Context, private val scrollView: NestedScrollView) :
        LinearLayoutManager(context) {

        // Force new items appear at the top
        override fun onItemsAdded(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
            super.onItemsAdded(recyclerView, positionStart, itemCount)

            scrollView.post {
                scrollView.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

}

