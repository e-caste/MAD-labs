package it.polito.mad.group27.carpooling.ui.trip.triplist

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.createSampleDataIfNotPresent
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.BaseFragmentWithToolbar
import it.polito.mad.group27.carpooling.ui.trip.Trip
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
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

    private inner class TripViewHolder internal constructor(private val view: View) : RecyclerView.ViewHolder(view) {
        private val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy")
        private val priceFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.ITALY)

        internal fun setPrice(price: BigDecimal?) {
            val textView = view.findViewById<TextView>(R.id.price_text)
            textView.text = priceFormat.format(price)
        }
        // TODO: add all other fields, carefully respect names to allow automatic (de)serialization
    }

    private inner class TripFirestoreRecyclerAdapter internal constructor(options: FirestoreRecyclerOptions<Trip>) : FirestoreRecyclerAdapter<Trip, TripViewHolder>(options) {
        override fun onBindViewHolder(tripViewHolder: TripViewHolder, position: Int, trip: Trip) {
            tripViewHolder.setPrice(trip.price)
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
        adapter = TripFirestoreRecyclerAdapter(options)
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