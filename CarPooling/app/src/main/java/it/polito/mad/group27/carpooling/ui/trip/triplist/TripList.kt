package it.polito.mad.group27.carpooling.ui.trip.triplist

import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.ui.trip.Trip
import it.polito.mad.group27.carpooling.ui.trip.TripDB
import java.util.*


class TripList: BaseTripList() {

    private val query = queryBase.whereEqualTo("ownerUid", currentUserUid)
    override val options = FirestoreRecyclerOptions.Builder<TripDB>()
        .setQuery(query, TripDB::class.java)
        .build()

    override fun setTopRightButtonIconAndOnClickListener(tripViewHolder: TripViewHolder, trip: Trip) {
        // do not allow users to edit trips in the past
        if (trip.endDateTime <= Calendar.getInstance()) {
            tripViewHolder.topRightButtonShadow.visibility = View.INVISIBLE
            tripViewHolder.topRightButton.visibility = View.INVISIBLE
            tripViewHolder.topRightButton.setOnClickListener {}
            return
        }
        val icon = R.drawable.ic_baseline_edit_24
        tripViewHolder.topRightButtonShadow.setImageResource(icon)
        tripViewHolder.topRightButton.setImageResource(icon)
        tripViewHolder.topRightButton.setOnClickListener {
            findNavController().navigate(R.id.action_tripList_to_tripEditFragment, bundleOf("trip" to trip))
        }
    }

    override fun setFab(view: View) {
        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener { findNavController().navigate(R.id.action_tripList_to_tripEditFragment) }
    }
}