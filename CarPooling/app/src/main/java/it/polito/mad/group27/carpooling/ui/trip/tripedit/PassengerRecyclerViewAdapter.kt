package it.polito.mad.group27.carpooling.ui.trip.tripedit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.group27.carpooling.Profile
import it.polito.mad.group27.carpooling.R

class PassengerRecyclerViewAdapter(
    val viewModel: TripEditViewModel,
    val acceptedAdapter: PassengerRecyclerViewAdapter?
) :
    RecyclerView.Adapter<PassengerRecyclerViewAdapter.ItemViewHolder>() {
    class ItemViewHolder(
        v: View,
        val this_rv: PassengerRecyclerViewAdapter,
        val other_rv: PassengerRecyclerViewAdapter?
    ) : RecyclerView.ViewHolder(v) {

        private val nicknameView: TextView = v.findViewById(R.id.nicknameView)
        private val viewProfile: TextView = v.findViewById(R.id.viewProfileLink)
        private val button: Button = v.findViewById(R.id.accept_button)

        fun bind(
            viewModel: TripEditViewModel,
            passenger: Profile,
            position: Int,
            this_rv: PassengerRecyclerViewAdapter,
            other_rv: PassengerRecyclerViewAdapter?
        ) {

            nicknameView.text = passenger.nickName
            if (other_rv != null) {
                button.visibility = View.VISIBLE
                if (passenger.uid == null){
                    button.isClickable = false
                    button.isEnabled = false
                    viewProfile.isClickable = false
                }
                else {
                    viewProfile.isClickable = true
                    viewProfile.setOnClickListener {
                        it.findNavController().navigate(
                            R.id.action_tripEditFragment_to_nav_profile,
                            bundleOf("profile" to passenger)
                        )
                    }

                    if(viewModel.newTrip.acceptedUsersUids.size < viewModel.newTrip.totalSeats ?: 0) {

                        button.isClickable = true
                        button.isEnabled = true
                        button.setOnClickListener {
                            this_rv.remove(position)
                            other_rv.add(passenger)
                        }
                    }
                    else{
                        button.isClickable = false
                        button.isEnabled = false
                    }


                }
            } else {
                button.visibility = View.GONE
                viewProfile.isClickable = true
                viewProfile.setOnClickListener {
                    it.findNavController().navigate(
                        R.id.action_tripEditFragment_to_nav_profile,
                        bundleOf("profile" to passenger)
                    )
                }
            }

        }
    }
    init {
        // download users data from db
        viewModel.downloadUsers{
            passengers.addAll(
            if(acceptedAdapter == null)
                viewModel.newTrip.interestedUsersUids.map {
                    viewModel.getProfileByUid(it)
                } as MutableList<Profile>
            else viewModel.newTrip.acceptedUsersUids.map {
                viewModel.getProfileByUid(it)
            } as MutableList<Profile> )
            this.notifyDataSetChanged()
        }
    }

    val passengers: MutableList<Profile> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.passenger_item, parent, false)
        return ItemViewHolder(layout, this, acceptedAdapter)
    }

    override fun getItemCount(): Int {
        return passengers.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(viewModel, passengers[position], position, this, acceptedAdapter)
    }

    fun add(passenger: Profile) {
        passengers.add(passenger)
        this.notifyItemInserted(passengers.size - 1)
        viewModel.putToAccepted(passenger.uid!!)
    }

    fun remove(position: Int) {
        passengers.removeAt(position)
        this.notifyItemRemoved(position)
        this.notifyItemRangeChanged(position, passengers.size - position)
    }


}