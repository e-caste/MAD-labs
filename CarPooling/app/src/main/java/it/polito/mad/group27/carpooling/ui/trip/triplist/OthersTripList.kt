package it.polito.mad.group27.carpooling.ui.trip.triplist

import android.os.Bundle
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.ui.trip.Trip
import it.polito.mad.group27.carpooling.ui.trip.TripDB

class OthersTripList: BaseTripList() {

    private val query = queryBase  // .whereNotEqualTo("ownerUid", currentUserUid)  // 2 inequalities on 2 fields in 1 query are invalid. wut.
    override val options = FirestoreRecyclerOptions.Builder<TripDB>()
        .setQuery(query, TripDB::class.java)
        .build()

    override fun setTopRightButtonIconAndOnClickListener(tripViewHolder: BaseTripList.TripViewHolder, bundle: Bundle) {
        val icon = R.drawable.ic_baseline_add_24
        tripViewHolder.topRightButtonShadow.setImageResource(icon)
        tripViewHolder.topRightButton.setImageResource(icon)
        tripViewHolder.topRightButton.setOnClickListener {
            // TODO: call Firestore API to add current user to interestedUsers and set icon to done check
        }
    }

    override fun filterOutTrip(trip: Trip): Boolean {
        return trip.ownerUid != currentUserUid
    }
}