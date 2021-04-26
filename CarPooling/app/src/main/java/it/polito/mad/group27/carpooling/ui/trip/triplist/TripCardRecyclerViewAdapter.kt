package it.polito.mad.group27.carpooling.ui.trip.triplist

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.ui.trip.Trip

import it.polito.mad.group27.carpooling.ui.trip.triplist.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [Trip].
 */
class TripCardRecyclerViewAdapter(
    private val values: MutableList<Trip>,
    private val navController: NavController,
): RecyclerView.Adapter<TripCardRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_trip, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.carImageView.setOnClickListener { navController.navigate(R.id.action_tripList_to_tripDetailsFragment) }
        holder.carImageView.setImageURI(item.carImageUri)
        holder.priceTextView.text = item.price.toString()
        holder.editButton.setOnClickListener { navController.navigate(R.id.action_tripList_to_tripEditFragment) }
        holder.departureTextView.text = item.from
        holder.destinationTextView.text = item.to
        holder.hourDepartureTextView.text = item.startHour.toString()
        holder.dateDepartureTextView.text = item.date.toString()
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val carImageView: ImageView = view.findViewById(R.id.car_image)
        val priceTextView: TextView = view.findViewById(R.id.price_text)
        val editButton: ImageButton = view.findViewById(R.id.edit_button)
        val departureTextView: TextView = view.findViewById(R.id.departure_text)
        val destinationTextView: TextView = view.findViewById(R.id.destination_text)
        val hourDepartureTextView: TextView = view.findViewById(R.id.hour_departure_text)
        val dateDepartureTextView: TextView = view.findViewById(R.id.date_departure_text)

        override fun toString(): String {
            return super.toString()
        }
    }
}