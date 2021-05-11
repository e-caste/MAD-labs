package it.polito.mad.group27.carpooling.ui.trip.triplist

import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.BaseFragmentWithToolbar
import it.polito.mad.group27.carpooling.ui.trip.Hour
import it.polito.mad.group27.carpooling.ui.trip.Trip
import it.polito.mad.group27.carpooling.ui.trip.TripDB
import java.math.BigDecimal
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


open class BaseTripList: BaseFragmentWithToolbar(
    R.layout.fragment_trip_list,
    R.menu.trip_list_menu,
    R.string.app_name
){

    protected val coll = FirebaseFirestore.getInstance().collection("trips")
    protected val queryBase = coll
        .whereGreaterThanOrEqualTo("startDateTime", Timestamp.now())
        .orderBy("startDateTime", Query.Direction.ASCENDING)
    protected val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: "UNAVAILABLE"
    protected open val options = FirestoreRecyclerOptions.Builder<TripDB>()
        .setQuery(queryBase, TripDB::class.java)
        .build()
    protected var adapter: TripFirestoreRecyclerAdapter? = null


    protected inner class TripViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy")
        private val priceFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.ITALY)

        val topRightButton: ImageButton = view.findViewById(R.id.topright_button)
        val topRightButtonShadow: ImageView = view.findViewById(R.id.topright_button_shadow)
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
                Glide.with(this@BaseTripList).load(carImageUri).into(carImageView)
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

        fun _setCardInvisible() {
            view.visibility = View.GONE
            view.layoutParams.height = 0
        }

        fun _setCardVisible() {
            view.visibility = View.VISIBLE
            // float should be same value as in fragment_trip.xml - this converts dp->px
            view.layoutParams.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200f, resources.displayMetrics).toInt()
        }
    }

    protected inner class TripFirestoreRecyclerAdapter(
        options: FirestoreRecyclerOptions<TripDB>,
        val showWarningMessage: () -> Unit,
    ) : FirestoreRecyclerAdapter<TripDB, TripViewHolder>(options) {

        override fun onBindViewHolder(tripViewHolder: TripViewHolder, position: Int, tripDB: TripDB) {
            val trip = tripDB.toTrip()

            if (filterOutTrip(trip)) {
                Log.d(getLogTag(), "filtering out trip: $trip")
                tripViewHolder._setCardInvisible()
                return
            }
            // prevent quirks where RV recycles gone cards
            tripViewHolder._setCardVisible()
            Log.d(getLogTag(), "adding trip to TripList: $trip")

            tripViewHolder.setPrice(trip.price)
            tripViewHolder.setCarImageUri(trip.carImageUri)
            tripViewHolder.setFrom(trip.from)
            tripViewHolder.setTo(trip.to)
            tripViewHolder.setStartDateTime(trip.startDateTime)

            tripViewHolder.carImageView.setOnClickListener {
                findNavController().navigate(R.id.action_tripList_to_tripDetailsFragment, bundleOf("tripId" to trip.id))
            }
            setTopRightButtonIconAndOnClickListener(tripViewHolder, trip)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_trip, parent, false)
            return TripViewHolder(view)
        }

        override fun onDataChanged() {
            super.onDataChanged()
            showWarningMessage()
        }

        override fun getItemCount(): Int {
            return this.snapshots.filter { !filterOutTrip(it.toTrip()) }.size
        }
    }

    protected open fun setTopRightButtonIconAndOnClickListener(tripViewHolder: TripViewHolder, trip: Trip) {
        // to implement in subclasses
    }

    protected open fun filterOutTrip(trip: Trip): Boolean {
        return false
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
        val warningMessageView = view.findViewById<LinearLayout>(R.id.warning_message_notrips)

        adapter = TripFirestoreRecyclerAdapter(options) {
            // all trips are hidden -> show warning message
            Log.d(getLogTag(), "item count: ${adapter!!.itemCount}")
            if (adapter!!.itemCount == 0) {
                Log.d(getLogTag(), "TripList is empty, showing warning message to user")
                recyclerView.visibility = View.GONE
                warningMessageView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                warningMessageView.visibility = View.GONE

                recyclerView.layoutManager = when (resources.configuration.orientation) {
                    Configuration.ORIENTATION_LANDSCAPE -> {
                        Log.d(
                            getLogTag(),
                            "orientation is landscape, using grid layout with 2 columns..."
                        )
                        GridLayoutManager(context, 2)
                    }
                    else -> {
                        Log.d(getLogTag(), "orientation is portrait, using linear layout...")
                        LinearLayoutManager(context)
                    }
                }
                recyclerView.adapter = adapter
            }
        }
        setFab(view)
    }

    protected open fun setFab(view: View) {
        // overridden in TripList
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