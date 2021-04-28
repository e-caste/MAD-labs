package it.polito.mad.group27.carpooling.ui.trip.tripdetails

import android.hardware.SensorAdditionalInfo
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
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
    // TODO insert title customized (Trip to .... ) (?)
    private lateinit var viewModel: TripDetailsViewModel
    private lateinit var dropdownListButton : LinearLayout
    private lateinit var expandButton : ImageView

    private lateinit var trip : Trip
    private lateinit var seatsView : TextView
    private lateinit var dateView : TextView
    private lateinit var priceView : TextView
    private lateinit var departureHour : TextView
    private lateinit var departureLocation : TextView
    private lateinit var destinationHour : TextView
    private lateinit var destinationLocation : TextView
    private lateinit var luggageView : LinearLayout
    private lateinit var animalsView : LinearLayout
    private lateinit var smokersView : LinearLayout
    private lateinit var additionalInfo: LinearLayout
    private lateinit var optionsView : LinearLayout
    private lateinit var infoText : TextView

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

        Log.d(getLogTag(),"got from bundle: $trip")

        // Find views
        dropdownListButton = view.findViewById(R.id.startTripView)
        expandButton = view.findViewById(R.id.expandButton)
        seatsView = view.findViewById(R.id.showTripSeats)
        dateView = view.findViewById(R.id.showTripDate)
        priceView = view.findViewById(R.id.showTripPrice)
        departureHour = view.findViewById(R.id.departureTimeDetails)
        departureLocation = view.findViewById(R.id.departureNameDetails)
        destinationHour = view.findViewById(R.id.tripStopTime)
        destinationLocation = view.findViewById(R.id.tripStopName)
        luggageView = view.findViewById(R.id.luggage_details)
        animalsView = view.findViewById(R.id.animals_details)
        smokersView = view.findViewById(R.id.smokers_details)
        additionalInfo = view.findViewById(R.id.additional_info_details)
        optionsView = view.findViewById(R.id.trip_options_details)
        infoText = view.findViewById(R.id.extra_info_text_details)

        // Display basic info
        seatsView.text = "${trip.availableSeats}/${trip.totalSeats}"
        dateView.text =  DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault()).format(trip.startDateTime)
        priceView.text = trip.price.toString()
        departureHour.text = Hour(trip.startDateTime[Calendar.HOUR], trip.startDateTime[Calendar.MINUTE]).toString()
        departureLocation.text = trip.from
        destinationHour.text = Hour(trip.endDateTime[Calendar.HOUR], trip.endDateTime[Calendar.MINUTE]).toString()
        destinationLocation.text = trip.to

        // Additional stops visualization
        if(trip.stops.size > 0){
            val recyclerView = view.findViewById<RecyclerView>(R.id.tripStopList)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = TripStopsViewAdapter(trip.stops)

            dropdownListButton.setOnClickListener {
                val arrowImageView : ImageView = view.findViewById(R.id.expandButton)
                val recyclerView = view.findViewById<RecyclerView>(R.id.tripStopList)

                Log.d(getLogTag(),"Touched route list")

                recyclerView.visibility =
                    when(recyclerView.visibility) {
                        View.GONE -> {
                            arrowImageView.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
                            View.VISIBLE
                        }
                        else -> {
                            arrowImageView.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                            View.GONE
                        }
                    }
            }
        }

        // Display additional info
        if(trip.options.size > 0 || (trip.otherInformation != null && trip.otherInformation!!.trim() != "")){
            luggageView.visibility = if(trip.options.contains(Option.LUGGAGE)) View.VISIBLE else View.GONE
            animalsView.visibility = if(trip.options.contains(Option.ANIMALS)) View.VISIBLE else View.GONE
            smokersView.visibility = if(trip.options.contains(Option.SMOKE)) View.VISIBLE else View.GONE

            if(trip.otherInformation != null && trip.otherInformation!!.trim() != "" ) {
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

}