package it.polito.mad.group27.carpooling.ui.trip.triplist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.TripFilter
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.trip.Option
import it.polito.mad.group27.carpooling.ui.trip.Trip
import it.polito.mad.group27.carpooling.ui.trip.TripDB

class OthersTripList: BaseTripList(
    layout = R.layout.fragment_trip_list,
    menu = R.menu.others_trip_list_menu,
) {

    private val query = queryBase  // .whereNotEqualTo("ownerUid", currentUserUid)  // 2 inequalities on 2 fields in 1 query are invalid. wut.
    override val options = FirestoreRecyclerOptions.Builder<TripDB>()
        .setQuery(query, TripDB::class.java)
        .build()
    private lateinit var tripFilter: TripFilter

    override fun customizeCardView(tripViewHolder: BaseTripList.TripViewHolder, trip: Trip) {
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
                        icon = R.drawable.ic_baseline_done_24
                        Toast.makeText(requireContext(), getString(R.string.success_message_booked), Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener {
                        icon = R.drawable.ic_baseline_add_24
                        Toast.makeText(requireContext(), getString(R.string.warning_message_failedbooking), Toast.LENGTH_LONG).show()
                    }
            }
        }
        tripViewHolder.topRightButtonShadow.setImageResource(icon!!)
        tripViewHolder.topRightButton.setImageResource(icon!!)
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
        tripFilter = arguments?.getParcelable("filter") ?: TripFilter()
        if (tripFilter.from == "") tripFilter.from = null
        if (tripFilter.to == "") tripFilter.to = null
        Log.d(getLogTag(), "OthersTripList trip filter: $tripFilter")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun setFab(view: View) {
        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.visibility = View.GONE
    }

    override fun filterOutTrip(trip: Trip): Boolean {

        fun applyTripFilter(): Boolean {
            var res = true
            if (tripFilter.from != null)
                res = res && trip.from.contains(tripFilter.from!!, ignoreCase = true)
            if (tripFilter.to != null)
                res = res && trip.to.contains(tripFilter.to!!, ignoreCase = true)
            if (trip.price != null)
                res = res && trip.price!! >= tripFilter.priceMin
                res = res && trip.price!! <= tripFilter.priceMax
            if (tripFilter.dateTime != null)
                res = res && trip.startDateTime >= tripFilter.dateTime!!
            for (opt in Option.values()) {
                if (tripFilter.options.contains(opt) && tripFilter.options[opt] == true) {
                    res = res && trip.options.contains(opt)
                }
            }
            return res
        }

        return !(trip.ownerUid != currentUserUid &&
                trip.advertised &&
                applyTripFilter())
    }
}