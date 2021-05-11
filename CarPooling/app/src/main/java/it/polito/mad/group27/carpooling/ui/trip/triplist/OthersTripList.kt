package it.polito.mad.group27.carpooling.ui.trip.triplist

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

    override fun setTopRightButtonIconAndOnClickListener(tripViewHolder: BaseTripList.TripViewHolder, trip: Trip) {
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
                coll.document(trip.id!!).set(trip)
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

    override fun setFab(view: View) {
        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.visibility = View.GONE
    }

    override fun filterOutTrip(trip: Trip): Boolean {
        return !(trip.ownerUid != currentUserUid &&
                trip.advertised)
    }
}