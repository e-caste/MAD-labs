package it.polito.mad.group27.carpooling.ui.trip.triplist

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.ui.trip.TripDB

class OthersTripList: BaseTripList() {

    private val query = queryBase.whereEqualTo("ownerUid", currentUserUid)
    private val options = FirestoreRecyclerOptions.Builder<TripDB>()
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

    override fun setAdapter(recyclerView: RecyclerView) {
        adapter = TripFirestoreRecyclerAdapter(options)
        recyclerView.adapter = adapter
    }
}