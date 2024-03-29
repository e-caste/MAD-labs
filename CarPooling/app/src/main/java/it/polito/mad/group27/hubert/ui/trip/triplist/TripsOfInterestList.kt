package it.polito.mad.group27.hubert.ui.trip.triplist

import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.mad.group27.hubert.R
import it.polito.mad.group27.hubert.entities.Trip
import it.polito.mad.group27.hubert.entities.TripDB

class TripsOfInterestList(
    title: Int = R.string.tripsofinterestlist_title,
): BaseTripList(
    title = title,
){
    private val query = queryBase
        .whereArrayContains("interestedUsersUids", currentUserUid)
        .whereEqualTo("advertised", true)
    override val options = FirestoreRecyclerOptions.Builder<TripDB>()
        .setQuery(query, TripDB::class.java)
        .build()
    override val warningMessageStringId: Int = R.string.warning_message_notrips_tripsofinterestlist
    override val warningMessagePictureDrawableId: Int = R.drawable.woman_running_behind_car

    override fun customizeCardView(tripViewHolder: TripViewHolder, trip: Trip) {
        tripViewHolder.carImageView.setOnClickListener {
            findNavController().navigate(R.id.action_tripsOfInterestList_to_tripDetailsFragment, bundleOf("tripId" to trip.id))
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