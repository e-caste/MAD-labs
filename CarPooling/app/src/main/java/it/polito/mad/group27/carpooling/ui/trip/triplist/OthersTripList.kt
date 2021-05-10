package it.polito.mad.group27.carpooling.ui.trip.triplist

import android.os.Bundle
import android.widget.Toast
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.ui.trip.Trip
import it.polito.mad.group27.carpooling.ui.trip.TripDB
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class OthersTripList: BaseTripList() {

    private val query = queryBase  // .whereNotEqualTo("ownerUid", currentUserUid)  // 2 inequalities on 2 fields in 1 query are invalid. wut.
    override val options = FirestoreRecyclerOptions.Builder<TripDB>()
        .setQuery(query, TripDB::class.java)
        .build()

    override fun setTopRightButtonIconAndOnClickListener(tripViewHolder: BaseTripList.TripViewHolder, bundle: Bundle) {
        val trip = Json.decodeFromString<Trip>(requireArguments().getString("trip")!!)
        var icon: Int? = null
        if (trip.interestedUsersUids.contains(currentUserUid) || trip.acceptedUsersUids.contains(currentUserUid)) {
            icon = R.drawable.ic_baseline_done_24
            tripViewHolder.topRightButton.setOnClickListener {
                Toast.makeText(requireContext(), "You have already booked this trip!", Toast.LENGTH_LONG).show()
            }
        } else {
            icon = R.drawable.ic_baseline_add_24
            tripViewHolder.topRightButton.setOnClickListener {
                // TODO: call Firestore API to add current user to interestedUsers and set icon to done check
            }
        }
        tripViewHolder.topRightButtonShadow.setImageResource(icon)
        tripViewHolder.topRightButton.setImageResource(icon)
    }

    override fun filterOutTrip(trip: Trip): Boolean {
        return trip.ownerUid != currentUserUid
    }
}