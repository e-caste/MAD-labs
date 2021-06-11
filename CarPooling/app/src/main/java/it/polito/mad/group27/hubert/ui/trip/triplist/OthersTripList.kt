package it.polito.mad.group27.hubert.ui.trip.triplist

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.group27.hubert.*
import it.polito.mad.group27.hubert.entities.Profile
import it.polito.mad.group27.hubert.entities.TripFilter
import it.polito.mad.group27.hubert.entities.Option
import it.polito.mad.group27.hubert.entities.Trip
import it.polito.mad.group27.hubert.entities.TripDB
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OthersTripList(
    menu: Int = R.menu.others_trip_list_menu,
    title: Int = R.string.otherstriplist_title,
): BaseTripList(
    menu = menu,
    title = title,
) {

    private val query = queryBase
        .whereGreaterThanOrEqualTo("startDateTime", Timestamp.now())
    // .whereNotEqualTo("ownerUid", currentUserUid)  // 2 inequalities on 2 fields in 1 query are invalid. wut.
    override val options = FirestoreRecyclerOptions.Builder<TripDB>()
        .setQuery(query, TripDB::class.java)
        .build()
    private val usersColl = FirebaseFirestore.getInstance().collection("users")
    private lateinit var tripFilter: TripFilter
    private val defaultTripFilter = TripFilter()
    private lateinit var checkedChips: MutableMap<String, Boolean>
    private lateinit var chipGroup: ChipGroup
    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm")
    private val nf = NumberFormat.getCurrencyInstance(Locale.ITALY)
    override val warningMessageStringId: Int = R.string.warning_message_notrips_otherstriplist
    override val warningMessagePictureDrawableId: Int = R.drawable.woman_pushing_car
    private val optionToString = mapOf(
        Option.ANIMALS to R.string.animals,
        Option.LUGGAGE to R.string.luggage,
        Option.SMOKE to R.string.smokers,
    )

    override fun customizeCardView(tripViewHolder: BaseTripList.TripViewHolder, trip: Trip) {
        tripViewHolder.carImageView.setOnClickListener {
            findNavController().navigate(R.id.action_othersTripList_to_tripDetailsFragment, bundleOf("tripId" to trip.id))
        }
        // the trip is full and the current user has not booked it
        if (trip.acceptedUsersUids.size == trip.totalSeats &&
                !trip.interestedUsersUids.contains(currentUserUid) &&
                !trip.acceptedUsersUids.contains(currentUserUid)) {
            tripViewHolder.topRightButtonShadow.visibility = View.GONE
            tripViewHolder.topRightButton.visibility = View.GONE
            tripViewHolder.topRightSoldOutTextView.visibility = View.VISIBLE
            return
        }
        var icon: Int? = null
        if (trip.interestedUsersUids.contains(currentUserUid) || trip.acceptedUsersUids.contains(currentUserUid)) {
            icon = R.drawable.ic_baseline_done_24
            tripViewHolder.topRightButton.setOnClickListener {
                Toast.makeText(requireContext(), getString(R.string.warning_message_alreadybooked), Toast.LENGTH_LONG).show()
            }
        } else {
            icon = R.drawable.ic_baseline_add_24
            tripViewHolder.topRightButton.setOnClickListener {
                trip.interestedUsersUids.add(currentUserUid)
                coll.document(trip.id!!).set(trip.toTripDB())
                    .addOnSuccessListener {
//                        icon = R.drawable.ic_baseline_done_24
                        Snackbar.make(requireView(), getString(R.string.success_message_booked), Snackbar.LENGTH_LONG).show()
                        // try sending notification to trip owner
                        var tripOwner: Profile? = null
                        usersColl
                            .document(trip.ownerUid)
                            .get()
                            .addOnSuccessListener {
                                Log.d(getLogTag(), "reservation notification: tripOwner is $tripOwner")
                                if (it != null) {
                                    tripOwner = it.toObject(Profile::class.java)
                                    val me = ViewModelProvider(act).get(ProfileViewModel::class.java).profile.value
                                    if (tripOwner != null && me != null) {
                                        MainScope().launch {
                                            val result =  MessagingService.sendNotification(
                                                tripOwner!!.notificationToken,
                                                AndroidNotification(
                                                    getString(R.string.reservation_notification_title),
                                                    getString(R.string.reservation_notification_body,
                                                        me.fullName, trip.from,
                                                        trip.to,
                                                        sdf.format(
                                                            trip.startDateTime.time
                                                        )),
                                                    ViewModelProvider(act).get(ProfileViewModel::class.java)
                                                        .profile.value!!.profileImageUri.toString()
                                                )
                                            )
                                        }
                                        Log.d(
                                            getLogTag(), "reservation notification: sent " +
                                                "from ${me.fullName} (${me.uid}) " +
                                                "to ${tripOwner!!.fullName} (${tripOwner!!.uid})!")
                                }
                            }
                        }
                    }
                    .addOnFailureListener {
//                        icon = R.drawable.ic_baseline_add_24
                        Snackbar.make(requireView(), getString(R.string.warning_message_failedbooking), Snackbar.LENGTH_LONG).show()
                    }
                }
        }
        tripViewHolder.topRightButtonShadow.setImageResource(icon)
        tripViewHolder.topRightButton.setImageResource(icon)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.app_bar_search -> {
                val bundle = bundleOf("filter" to tripFilter)
                Log.d(getLogTag(), "opening trips filter fragment with bundle: $bundle")
                findNavController().navigate(R.id.action_othersTripList_to_tripFilterFragment, bundle)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        checkedChips = mutableMapOf(
            "from" to false,
            "to" to false,
            "priceMin" to false,
            "priceMax" to false,
            "dateTime" to false,
            "luggage" to false,
            "animals" to false,
            "smoke" to false,
        )
        tripFilter = arguments?.getParcelable("filter") ?: TripFilter()
        if (tripFilter.from == "") tripFilter.from = null
        if (tripFilter.to == "") tripFilter.to = null
        Log.d(getLogTag(), "OthersTripList trip filter: $tripFilter")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(
            true // default to enabled
        ) {
            override fun handleOnBackPressed() {
                if (tripFilter == TripFilter())
                    requireActivity().finish()
                else
                    findNavController().navigate(R.id.action_othersTripList_self)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this,callback)
    }

    override fun customizeTripList(view: View) {
        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        chipGroup = view.findViewById(R.id.trip_filter_chip_group)
        fab.visibility = View.GONE
        if (tripFilter != defaultTripFilter) {
            addChipsForEnabledFilters()
            Log.d(getLogTag(), "trip filter is applied, showing chips: ${chipGroup.checkedChipIds}")
            chipGroup.visibility = View.VISIBLE
        }
    }

    private fun addChipsForEnabledFilters() {

        fun addChip(text: String, k: String, logParam: Any) {
            val chip = layoutInflater.inflate(R.layout.chip_filter, null) as Chip
            chip.text = text
            checkedChips[k] = true
            chip.setOnClickListener {
                checkedChips[k] = !checkedChips[k]!!
                Log.d(getLogTag(), "chip from ($logParam) toggled: ${checkedChips[k]}")
                adapter!!.onDataChanged()
            }
            chipGroup.addView(chip)
        }

        var s: String

        if (tripFilter.from != defaultTripFilter.from) {
            s = "${getString(R.string.from)} ${tripFilter.from!!}"
            addChip(s, "from", s)
        }

        if (tripFilter.to != defaultTripFilter.to) {
            s = "${getString(R.string.to)} ${tripFilter.to!!}"
            addChip(s, "to", s)
        }

        if (tripFilter.priceMin != defaultTripFilter.priceMin) {
            s = "${getString(R.string.from)} ${nf.format(tripFilter.priceMin)}"
            addChip(s, "priceMin", s)
        }

        if (tripFilter.priceMax != defaultTripFilter.priceMax) {
            s = "${getString(R.string.upto)} ${nf.format(tripFilter.priceMax)}"
            addChip(s, "priceMax", s)
        }

        if (tripFilter.dateTime != defaultTripFilter.dateTime) {
            s = "${getString(R.string.since)} ${sdf.format(tripFilter.dateTime?.time!!)}"
            addChip(s, "dateTime", s)
        }

        for (opt in Option.values()) {
            if (tripFilter.options.contains(opt) && tripFilter.options[opt] != defaultTripFilter.options[opt]) {
                val optionName = opt.name.toLowerCase(Locale.ROOT)
                addChip(getString(optionToString[opt]!!), optionName, optionName)
            }
        }
    }

    override fun isFilteredOut(trip: Trip): Boolean {

        // returns true when a trip is not filtered out by any field of the trip filter
        // returns false when at least 1 filter does not correspond to one of the trip's fields
        fun isNotFilteredOutByTripFilter(): Boolean {
            var fromStopIndex: Int? = null
            var fromStop: String? = null
            val stops = trip.stops.map { it.place }.toMutableList()
            stops.add(0, trip.from)
            stops.add(trip.to)
            Log.d(getLogTag(), "stops list is: $stops")

            if (tripFilter.from != null && checkedChips["from"]!!) {
                val sublist = stops.subList(0, stops.size - 1)
//                Log.d(getLogTag(), "stops: $stops - " +
//                        "sublist: $sublist - " +
//                        "from: ${tripFilter.from!!} - " +
//                        "contains: ${sublist.any { it.contains(tripFilter.from!!, ignoreCase = true) }}")
                fromStop = sublist.find { it.contains(tripFilter.from!!, ignoreCase = true) }
                if (fromStop != null) {
                    fromStopIndex = sublist.indexOf(fromStop)
                } else return false
            }
            if (tripFilter.to != null && checkedChips["to"]!!) {
                val sublist = stops.subList(fromStopIndex ?: 1, stops.size)
                sublist.any {  it.contains(tripFilter.to!!, ignoreCase = true) } || return false
            }
            if (trip.price != null && checkedChips["priceMin"]!!) {
                trip.price!! >= tripFilter.priceMin || return false
            }
            if (trip.price != null && checkedChips["priceMax"]!!) {
                trip.price!! <= tripFilter.priceMax || return false
            }
            if (tripFilter.dateTime != null && checkedChips["dateTime"]!!) {
                trip.startDateTime >= tripFilter.dateTime!! || return false
            }
            for (opt in Option.values()) {
                if (tripFilter.options.contains(opt) &&
                    tripFilter.options[opt] == true &&
                    checkedChips[opt.name.toLowerCase(Locale.ROOT)]!!) {
                    trip.options.contains(opt) || return false
                }
            }
            return true
        }

        return !(trip.ownerUid != currentUserUid &&
                trip.advertised &&
                isNotFilteredOutByTripFilter())
    }
}