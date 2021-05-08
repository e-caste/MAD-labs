package it.polito.mad.group27.carpooling.ui.trip.triplist

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.BaseFragmentWithToolbar
import it.polito.mad.group27.carpooling.ui.trip.Hour
import it.polito.mad.group27.carpooling.ui.trip.Trip
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


/**
 * A fragment representing a list of Items.
 */
class TripList: BaseFragmentWithToolbar(
    R.layout.fragment_trip_list,
    R.menu.trip_list_menu,
    R.string.app_name
){

    private val db = FirebaseFirestore.getInstance()
    private val queryBase = db.collection(coll)
        .whereGreaterThan("startDateTime", Calendar.getInstance())  // TODO: check if this works
        .orderBy("startDateTime", Query.Direction.ASCENDING)
    private val options = FirestoreRecyclerOptions.Builder<Trip>()
        .setQuery(queryBase, Trip::class.java)
        .build()
    private var adapter: TripFirestoreRecyclerAdapter? = null

    companion object {
        private const val coll = "trips"
    }

    private inner class TripViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy")
        private val priceFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.ITALY)

        val editButton: ImageButton = view.findViewById(R.id.edit_button)
        val carImageView: ImageView = view.findViewById(R.id.car_image)

        fun setPrice(price: BigDecimal?) {
            val textView = view.findViewById<TextView>(R.id.price_text)
            textView.text = priceFormat.format(price)
        }

        fun setCarImageUri(carImageUri: Uri?) {
            val carImageView = view.findViewById<ImageView>(R.id.car_image)
            if (carImageUri == null) {
                carImageView.setColorFilter(Color.argb(34, 68, 68, 68))
                carImageView.setImageResource(R.drawable.ic_baseline_directions_car_24)
            } else {
                carImageView.setImageURI(carImageUri)
                carImageView.colorFilter = null
            }
        }

        fun setFrom(from: String?) {
            val fromTextView = view.findViewById<TextView>(R.id.departure_text)
            fromTextView.text = from
        }

        fun setTo(to: String?) {
            val toTextView = view.findViewById<TextView>(R.id.destination_text)
            toTextView.text = to
        }

        fun setStartDateTime(startDateTime: Calendar?) {
            val hourDepartureTextView = view.findViewById<TextView>(R.id.hour_departure_text)
            val dateDepartureTextView = view.findViewById<TextView>(R.id.date_departure_text)
            if (startDateTime != null) {
                hourDepartureTextView.text = Hour(startDateTime.get(Calendar.HOUR_OF_DAY), startDateTime[Calendar.MINUTE]).toString()
                dateDepartureTextView.text = dateFormat.format(startDateTime.time)
            }
        }
    }

    private inner class TripFirestoreRecyclerAdapter(
        options: FirestoreRecyclerOptions<Trip>,
        private val navController: NavController,
    ) : FirestoreRecyclerAdapter<Trip, TripViewHolder>(options) {

        override fun onBindViewHolder(tripViewHolder: TripViewHolder, position: Int, trip: Trip) {
            val bundle = bundleOf("trip" to Json.encodeToString(trip))
            val bundleParcelable = bundleOf("trip" to trip)

            tripViewHolder.setPrice(trip.price)
            tripViewHolder.setCarImageUri(trip.carImageUri)
            tripViewHolder.setFrom(trip.from)
            tripViewHolder.setTo(trip.to)
            tripViewHolder.setStartDateTime(trip.startDateTime)

            tripViewHolder.carImageView.setOnClickListener { navController.navigate(R.id.action_tripList_to_tripDetailsFragment, bundleParcelable) }
            tripViewHolder.editButton.setOnClickListener { navController.navigate(R.id.action_tripList_to_tripEditFragment, bundle) }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_trip, parent, false)
            return TripViewHolder(view)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_trip_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        recyclerView.layoutManager = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                Log.d(getLogTag(), "orientation is landscape, using grid layout with 2 columns...")
                GridLayoutManager(context, 2)
            }
            else -> {
                Log.d(getLogTag(), "orientation is portrait, using linear layout...")
                LinearLayoutManager(context)
            }
        }
        adapter = TripFirestoreRecyclerAdapter(options, findNavController())
        recyclerView.adapter = adapter

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener { findNavController().navigate(R.id.action_tripList_to_tripEditFragment) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // TODO: do we still need to save and restore the instance state?
    }

    override fun onStart() {
        super.onStart()
        adapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()

        if (adapter != null) {
            adapter!!.stopListening()
        }
    }
}