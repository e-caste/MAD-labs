package it.polito.mad.group27.carpooling.ui.trip.triplist

import android.graphics.Color
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.entities.Trip
import it.polito.mad.group27.carpooling.entities.TripDB
import java.util.*


class TripList(
    title: Int = R.string.triplist_title,
): BaseTripList(
    title = title,
) {

    private val query = queryBase.whereEqualTo("ownerUid", currentUserUid)
    override val options = FirestoreRecyclerOptions.Builder<TripDB>()
        .setQuery(query, TripDB::class.java)
        .build()
    override val warningMessageStringId: Int = R.string.warning_message_notrips_triplist
    override val warningMessagePictureDrawableId: Int = R.drawable.man_entering_car

    override fun customizeCardView(tripViewHolder: TripViewHolder, trip: Trip) {
        tripViewHolder.carImageView.setOnClickListener {
            findNavController().navigate(R.id.action_tripList_to_tripDetailsFragment, bundleOf("tripId" to trip.id))
        }
        // do not allow users to edit trips in the past
        if ((trip.endDateTime <= Calendar.getInstance() && trip.advertised) || !trip.advertised) {
            tripViewHolder.topRightButtonShadow.visibility = View.INVISIBLE
            tripViewHolder.topRightButton.visibility = View.INVISIBLE
            tripViewHolder.topRightButton.setOnClickListener {}
            // show red border around card
            if (!trip.advertised) {
                val v = tripViewHolder.view as MaterialCardView
                v.strokeColor = Color.RED
                v.invalidate()
            }
            return
        }
        val icon = R.drawable.ic_baseline_edit_24
        tripViewHolder.topRightButtonShadow.setImageResource(icon)
        tripViewHolder.topRightButton.setImageResource(icon)
        tripViewHolder.topRightButton.setOnClickListener {
            findNavController().navigate(R.id.action_tripList_to_tripEditFragment, bundleOf("trip" to trip))
        }
    }

    override fun customizeTripList(view: View) {
        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.visibility = View.VISIBLE
        fab.setOnClickListener { findNavController().navigate(R.id.action_tripList_to_tripEditFragment) }
    }
}