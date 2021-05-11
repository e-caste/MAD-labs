package it.polito.mad.group27.carpooling.ui.trip.tripdetails

import android.content.res.Configuration
import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.BaseFragmentWithToolbar
import it.polito.mad.group27.carpooling.ui.trip.Hour
import it.polito.mad.group27.carpooling.ui.trip.Option
import it.polito.mad.group27.carpooling.ui.trip.Trip
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.DateFormat
import java.util.*

class TripDetailsFragment : BaseFragmentWithToolbar(R.layout.trip_details_fragment,
        R.menu.show_menu, null) {
    private lateinit var tripDetailsViewModel: TripDetailsViewModel
    private lateinit var dropdownListButton : LinearLayout
    private lateinit var expandButton : ImageView

    private lateinit var seatsView : TextView
    private lateinit var dateView : TextView
    private lateinit var estimatedTimeView: TextView
    private lateinit var priceView : TextView
    private lateinit var departureDateTime : TextView
    private lateinit var departureLocation : TextView
    private lateinit var destinationDateTime : TextView
    private lateinit var destinationLocation : TextView
    private lateinit var luggageView : LinearLayout
    private lateinit var animalsView : LinearLayout
    private lateinit var smokersView : LinearLayout
    private lateinit var additionalInfo: LinearLayout
    private lateinit var optionsView : LinearLayout
    private lateinit var infoText : TextView
    private lateinit var carImageView : ImageView
    private lateinit var recyclerView : RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tripDetailsViewModel = ViewModelProvider(this).get(TripDetailsViewModel::class.java)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.trip_details_fragment, container, false)
        if (view is RecyclerView) {
            with(view) {
                adapter = TripStopsViewAdapter(tripDetailsViewModel.trip.value!!.stops)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val visibility = savedInstanceState?.getInt("${getString(R.string.app_name)}.stateView")
        tripDetailsViewModel.trip.value = arguments?.getParcelable("trip") ?: Trip()

        Log.d(getLogTag(), "got from bundle: ${tripDetailsViewModel.trip}")

        // Update title only in portrait orientation
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            updateTitle("${getString(R.string.trip_to)} ${tripDetailsViewModel.trip.value?.to}")
        else{
            val fragmentTitle : TextView = view.findViewById(R.id.trip_title_details)
            "${getString(R.string.trip_to)} ${tripDetailsViewModel.trip.value?.to}".also { fragmentTitle.text = it }
        }

        // Find views
        dropdownListButton = view.findViewById(R.id.startTripView)
        expandButton = view.findViewById(R.id.expandButton)
        carImageView = view.findViewById(R.id.image_details_view)
        estimatedTimeView = view.findViewById(R.id.estimated_time_details)
        seatsView = view.findViewById(R.id.showTripSeats)
        dateView = view.findViewById(R.id.showTripDate)
        priceView = view.findViewById(R.id.showTripPrice)
        departureDateTime = view.findViewById(R.id.departureTimeDetails)
        departureLocation = view.findViewById(R.id.departureNameDetails)
        destinationDateTime = view.findViewById(R.id.tripStopDateTime)
        destinationLocation = view.findViewById(R.id.tripStopName)
        luggageView = view.findViewById(R.id.luggage_details)
        animalsView = view.findViewById(R.id.animals_details)
        smokersView = view.findViewById(R.id.smokers_details)
        additionalInfo = view.findViewById(R.id.additional_info_details)
        optionsView = view.findViewById(R.id.trip_options_details)
        infoText = view.findViewById(R.id.extra_info_text_details)

        // Display basic info
        if (tripDetailsViewModel.trip.value?.carImageUri == null) {
            carImageView.setColorFilter(Color.argb(34, 68, 68, 68))
            carImageView.setImageResource(R.drawable.ic_baseline_directions_car_24)
        } else {
            carImageView.setImageURI(tripDetailsViewModel.trip.value?.carImageUri)
        }


        (tripDetailsViewModel.trip.value!!.availableSeats.toString() + "/" + tripDetailsViewModel.trip.value?.totalSeats).also { seatsView.text = it }
        dateView.text = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault()).format(tripDetailsViewModel.trip.value!!.startDateTime.time)
        setEstimatedTime()
        priceView.text = tripDetailsViewModel.trip.value?.price.toString()
        departureDateTime.text = getDateTime(tripDetailsViewModel.trip.value!!.startDateTime)
        departureLocation.text = tripDetailsViewModel.trip.value?.from
        destinationDateTime.text = getDateTime(tripDetailsViewModel.trip.value!!.endDateTime)
        destinationLocation.text = tripDetailsViewModel.trip.value?.to

        // Additional stops visualization
        if (tripDetailsViewModel.trip.value!!.stops.size > 0) {
            recyclerView = view.findViewById(R.id.tripStopList)
            recyclerView.visibility = visibility ?: View.GONE
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = TripStopsViewAdapter(tripDetailsViewModel.trip.value!!.stops)

            dropdownListButton.setOnClickListener {
                Log.d(getLogTag(), "Touched route list")
                recyclerView.visibility =
                    when (recyclerView.visibility) {
                        View.GONE -> {
                            expandButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
                            View.VISIBLE
                        }
                        else -> {
                            expandButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                            View.GONE
                        }
                    }
            }
        } else expandButton.visibility = View.INVISIBLE

        // Display additional info
        if (tripDetailsViewModel.trip.value?.options!!.size > 0 || (tripDetailsViewModel.trip.value?.otherInformation != null && tripDetailsViewModel.trip.value?.otherInformation!!.trim() != "")) {
            luggageView.visibility = if (tripDetailsViewModel.trip.value?.options!!.contains(Option.LUGGAGE)) View.VISIBLE else View.GONE
            animalsView.visibility = if (tripDetailsViewModel.trip.value?.options!!.contains(Option.ANIMALS)) View.VISIBLE else View.GONE
            smokersView.visibility = if (tripDetailsViewModel.trip.value?.options!!.contains(Option.SMOKE)) View.VISIBLE else View.GONE

            if (tripDetailsViewModel.trip.value?.otherInformation != null && tripDetailsViewModel.trip.value?.otherInformation!!.trim() != "") {
                infoText.text = tripDetailsViewModel.trip.value?.otherInformation
                additionalInfo.visibility = View.VISIBLE
            } else {
                additionalInfo.visibility = View.GONE
            }
        } else {
            optionsView.visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.edit_menu_button -> {
                Log.d(getLogTag(),"Passing bundle of ${tripDetailsViewModel.trip.value}")
                findNavController().navigate(
                        R.id.action_tripDetailsFragment_to_tripEditFragment,
                        bundleOf("trip" to Json.encodeToString(tripDetailsViewModel.trip.value)))
            }
            else-> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("${getString(R.string.app_name)}.stateView", recyclerView.visibility)
    }


    private fun getEstimatedTime(start: Calendar, end: Calendar): Hour {
        val deltaMinutes = (end.timeInMillis - start.timeInMillis) / (1000*60)
        val hours = ((deltaMinutes)/60).toInt()
        val minutes = ((deltaMinutes)%60).toInt()
        return Hour(hours, minutes)
    }

    private fun setEstimatedTime(){
        val time = getEstimatedTime(tripDetailsViewModel.trip.value!!.startDateTime, tripDetailsViewModel.trip.value!!.endDateTime)
        val hours = if (time.hour > 0) "${time.hour} h" else ""
        val minutes = if (time.minute > 0) "${time.minute} min" else ""
        estimatedTimeView.text = ("$hours $minutes")
    }

    private fun getDateTime(item: Calendar) :String {
        val date = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ITALY).format(item.timeInMillis).toString()
        val time = Hour(item[Calendar.HOUR_OF_DAY], item[Calendar.MINUTE]).toString()
        return "$date, $time"
    }

}