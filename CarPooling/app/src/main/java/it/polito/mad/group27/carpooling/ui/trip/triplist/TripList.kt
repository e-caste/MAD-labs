package it.polito.mad.group27.carpooling.ui.trip.triplist

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.mad.group27.carpooling.Profile
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.createSampleDataIfNotPresent
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.BaseFragmentWithToolbar
import it.polito.mad.group27.carpooling.ui.trip.Trip
import it.polito.mad.group27.carpooling.ui.trip.triplist.dummy.DummyContent
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


/**
 * A fragment representing a list of Items.
 */
class TripList: BaseFragmentWithToolbar(
    R.layout.fragment_trip_list,
    R.menu.trip_list_menu,
    R.string.app_name
){

    private lateinit var trips: MutableList<Trip>
    val counterName = "group27.lab2.trips.id_counter"
    val tripPrefix = "group27.lab2.trips."
    val carImagePrefix = "group27.lab2.car_img."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createSampleDataIfNotPresent()
        loadTrips()
    }

    private fun loadTrips() {
        trips = mutableListOf()
        val prefs = activity?.getPreferences(Context.MODE_PRIVATE)
        val savedTripsCounter = prefs?.getString(counterName, null)?.toInt()
        if (savedTripsCounter != null) {
            for (i in 0 until savedTripsCounter) {
                try {
                    val savedTripJson = prefs.getString("$tripPrefix$i", null)
                    if (savedTripJson != null) {
                        val tmpTrip: Trip = Json.decodeFromString(savedTripJson)
                        trips.add(tmpTrip)
                        Log.d(getLogTag(), "decoded $tripPrefix$i from JSON with data: $tmpTrip")
                    } else {
                        Log.d(getLogTag(), "$tripPrefix$i contains null, not decoding from JSON")
                    }
                } catch (e: SerializationException) {
                    Log.d(getLogTag(), "cannot parse saved preference $tripPrefix$i")
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trip_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                // TODO: when is this called?
                layoutManager = when (resources.configuration.orientation) {
                    Configuration.ORIENTATION_LANDSCAPE -> {
                        Log.d(getLogTag(), "orientation is landscape, using grid layout...")
                        GridLayoutManager(context, 2)
                    }
                    else -> {
                        Log.d(getLogTag(), "orientation is portrait, using linear layout...")
                        LinearLayoutManager(context)
                    }
                }
                adapter = TripCardRecyclerViewAdapter(trips, findNavController())
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        recyclerView.layoutManager = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                Log.d(getLogTag(), "orientation is landscape, using grid layout...")
                // TODO: select number of columns based on screen width
                GridLayoutManager(context, 2)
            }
            else -> {
                Log.d(getLogTag(), "orientation is portrait, using linear layout...")
                LinearLayoutManager(context)
            }
        }
        recyclerView.adapter = TripCardRecyclerViewAdapter(trips, findNavController())

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
            findNavController().navigate(R.id.action_tripList_to_tripEditFragment)
        }


    }
}