package it.polito.mad.group27.carpooling.ui.trip.triplist

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
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


abstract class BaseTripList(
    private val layout: Int = R.layout.fragment_trip_list,
    menu: Int = R.menu.base_trip_list_menu,
    title: Int = R.string.app_name,
    ): BaseFragmentWithToolbar(
    layout,
    menu,
    title,
){

    protected val coll = FirebaseFirestore.getInstance().collection("trips")
    protected val queryBase = coll
        .orderBy("startDateTime", Query.Direction.ASCENDING)
    protected val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: "UNAVAILABLE"
    protected open val options = FirestoreRecyclerOptions.Builder<TripDB>()
        .setQuery(queryBase, TripDB::class.java)
        .build()
    protected var adapter: TripFirestoreRecyclerAdapter? = null
    abstract val warningMessageStringId: Int
    abstract val warningMessagePictureDrawableId: Int


    inner class TripViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
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
            view.layoutParams =  LinearLayout.LayoutParams(0,
                0)
        }

        fun _setCardVisible() {
            view.visibility = View.VISIBLE
            // float should be same value as in fragment_trip.xml - this converts dp->px
            view.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200f, resources.displayMetrics).toInt()
            )
        }
    }

    protected inner class TripFirestoreRecyclerAdapter(
        options: FirestoreRecyclerOptions<TripDB>,
        val showWarningMessage: () -> Unit,
    ) : FirestoreRecyclerAdapter<TripDB, TripViewHolder>(options) {

        var recyclerView: RecyclerView? =null
        var verticalScroll:Int = 0
        override fun onBindViewHolder(tripViewHolder: TripViewHolder, position: Int, tripDB: TripDB) {
            val trip = tripDB.toTrip()

            if (isFilteredOut(trip)) {
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

            customizeCardView(tripViewHolder, trip)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_trip, parent, false)
            return TripViewHolder(view)
        }

        override fun onDataChanged() {
            super.onDataChanged()
            showWarningMessage()

            //bringing back scrollview to correct position
            val tmpScroll = verticalScroll
            recyclerView?.scrollBy(0,tmpScroll)
            if (recyclerView != null)
                verticalScroll -= tmpScroll
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            this.recyclerView = recyclerView
           recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
               override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                   super.onScrolled(recyclerView, dx, dy)
                   verticalScroll += dy
               }
           })
        }

        override fun getItemCount(): Int {
            return this.snapshots.size
        }

        fun getShownItemNumber(): Int {
            return this.snapshots.filter { !isFilteredOut(it.toTrip()) }.size
        }
    }

    // set top right button and its onClickListener, then customize the card if needed
    abstract fun customizeCardView(tripViewHolder: TripViewHolder, trip: Trip)

    protected open fun isFilteredOut(trip: Trip): Boolean {
        return false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        val warningMessageView = view.findViewById<LinearLayout>(R.id.warning_message_notrips)
        val warningMessageTextView = warningMessageView.findViewById<TextView>(R.id.warning_message_notrips_textview)
        val warningMessageImageView = warningMessageView.findViewById<ImageView>(R.id.warning_message_notrips_imageview)
        warningMessageTextView.text = getString(warningMessageStringId)
        warningMessageImageView.setImageResource(warningMessagePictureDrawableId)

        adapter = TripFirestoreRecyclerAdapter(options) {
            // all trips are hidden -> show warning message
            Log.d(getLogTag(), "item count: ${adapter!!.itemCount}")
            if (adapter!!.getShownItemNumber() == 0) {
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
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    }
                    else -> {
                        Log.d(getLogTag(), "orientation is portrait, using linear layout...")
                        LinearLayoutManager(context)
                    }
                }
                recyclerView.adapter = adapter
            }
        }
        customizeTripList(view)
    }

    // set FAB, show Chips...
    abstract fun customizeTripList(view: View)

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