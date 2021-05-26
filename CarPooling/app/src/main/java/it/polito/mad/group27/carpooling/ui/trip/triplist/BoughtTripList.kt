package it.polito.mad.group27.carpooling.ui.trip.triplist

import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.ui.trip.Trip
import it.polito.mad.group27.carpooling.ui.trip.TripDB

class BoughtTripList(
    title: Int = R.string.boughttriplist_title,
): BaseTripList(
    title = title,
) {
    private val query = queryBase
        .whereArrayContains("acceptedUsersUids", currentUserUid)
    override val options = FirestoreRecyclerOptions.Builder<TripDB>()
        .setQuery(query, TripDB::class.java)
        .build()
    override val warningMessageLayoutId: Int = R.id.warning_message_notrips_boughttriplist

    override fun customizeCardView(tripViewHolder: TripViewHolder, trip: Trip) {
        tripViewHolder.carImageView.setOnClickListener {
            findNavController().navigate(R.id.action_boughtTripList_to_tripDetailsFragment, bundleOf("tripId" to trip.id))
        }
        tripViewHolder.topRightButtonShadow.visibility = View.GONE
        tripViewHolder.topRightButton.visibility = View.GONE
        tripViewHolder.topRightButton.setOnClickListener { }
    }

    override fun customizeTripList(view: View) {
        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.visibility = View.GONE
    }
}