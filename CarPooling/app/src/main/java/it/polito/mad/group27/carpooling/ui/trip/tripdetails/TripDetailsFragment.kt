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
import java.text.DateFormat
import java.util.*

class TripDetailsFragment : BaseFragmentWithToolbar(R.layout.trip_details_fragment,
        R.menu.show_menu, null) {
    private lateinit var viewModel: TripDetailsViewModel
    private lateinit var dropdownListButton : LinearLayout
    private lateinit var expandButton : ImageView

    private lateinit var trip : Trip
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
    private lateinit var carImageView: ImageView

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TripDetailsViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                adapter = TripStopsViewAdapter(trip.stops)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trip = arguments?.getParcelable<Trip>("trip") ?: Trip()

        Log.d(getLogTag(), "got from bundle: $trip")

        // Update title only in portrait orientation
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            updateTitle("${getString(R.string.trip_to)} ${trip.to}")
        else{
            val fragmentTitle : TextView = view.findViewById(R.id.trip_title_details)
            fragmentTitle.text = "${getString(R.string.trip_to)} ${trip.to}"
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
        if (trip.carImageUri == null) {
            carImageView.setColorFilter(Color.argb(34, 68, 68, 68))
            carImageView.setImageResource(R.drawable.ic_baseline_directions_car_24)
        } else {
            carImageView.setImageURI(trip.carImageUri)
        }


        seatsView.text = "${trip.availableSeats}/${trip.totalSeats}"
        dateView.text = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault()).format(trip.startDateTime.time)
        setEstimatedTime()
        priceView.text = trip.price.toString()
        departureDateTime.text = getDateTime(trip.startDateTime)
        departureLocation.text = trip.from
        destinationDateTime.text = getDateTime(trip.endDateTime)
        destinationLocation.text = trip.to

        // Additional stops visualization
        if (trip.stops.size > 0) {
            val recyclerView = view.findViewById<RecyclerView>(R.id.tripStopList)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = TripStopsViewAdapter(trip.stops)

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
        if (trip.options.size > 0 || (trip.otherInformation != null && trip.otherInformation!!.trim() != "")) {
            luggageView.visibility = if (trip.options.contains(Option.LUGGAGE)) View.VISIBLE else View.GONE
            animalsView.visibility = if (trip.options.contains(Option.ANIMALS)) View.VISIBLE else View.GONE
            smokersView.visibility = if (trip.options.contains(Option.SMOKE)) View.VISIBLE else View.GONE

            if (trip.otherInformation != null && trip.otherInformation!!.trim() != "") {
                infoText.text = trip.otherInformation
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
                Log.d(getLogTag(),"Passing bundle of $trip")
                findNavController().navigate(
                        R.id.action_tripDetailsFragment_to_tripEditFragment,
                        bundleOf("trip" to trip))
            }
            else-> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun getEstimatedTime(start: Calendar, end: Calendar): Hour {
        val deltaMinutes = (end.timeInMillis - start.timeInMillis) / (1000*60)
        val hours = ((deltaMinutes)/60).toInt()
        val minutes = ((deltaMinutes)%60).toInt()
        return Hour(hours, minutes)
    }

    private fun setEstimatedTime(){
        val time = getEstimatedTime(trip.startDateTime, trip.endDateTime)
        val hours = if (time.hour > 0) "${time.hour} h" else ""
        val minutes = if (time.minute > 0) "${time.minute} min" else ""
        estimatedTimeView.text = ("$hours $minutes")
    }

    private fun getDateTime(item: Calendar) :String {
        val date = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ITALY).format(item.timeInMillis).toString()
        val time = Hour(item[Calendar.HOUR], item[Calendar.MINUTE]).toString()
        return "$date, $time"
    }

}