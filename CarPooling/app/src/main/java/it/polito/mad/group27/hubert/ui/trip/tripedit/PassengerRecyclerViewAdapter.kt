package it.polito.mad.group27.hubert.ui.trip.tripedit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import it.polito.mad.group27.hubert.entities.Profile
import it.polito.mad.group27.hubert.R
import it.polito.mad.group27.hubert.Size
import it.polito.mad.group27.hubert.dpTopixel
import it.polito.mad.group27.hubert.loadImage

class PassengerRecyclerViewAdapter(
    private val viewModel: TripEditViewModel,
    private val acceptedAdapter: PassengerRecyclerViewAdapter?,
    private val context: Context
) :
    RecyclerView.Adapter<PassengerRecyclerViewAdapter.ItemViewHolder>() {
    class ItemViewHolder(
        v: View,
        val this_rv: PassengerRecyclerViewAdapter,
        val other_rv: PassengerRecyclerViewAdapter?
    ) : RecyclerView.ViewHolder(v) {


        private val nicknameView: TextView = v.findViewById(R.id.nicknameView)
        private val viewProfile: LinearLayout = v.findViewById(R.id.viewProfileLink)
        private val button: Button = v.findViewById(R.id.accept_button)
        private val image: ImageView = v.findViewById(R.id.passenger_image)

        fun bind(
            context: Context,
            viewModel: TripEditViewModel,
            passenger: Profile,
            position: Int,
            this_rv: PassengerRecyclerViewAdapter,
            other_rv: PassengerRecyclerViewAdapter?
        ) {

            if (passenger.profileImageUri != null)
                (context as AppCompatActivity).supportFragmentManager
                    .findFragmentById(R.id.nav_host_fragment)
                    ?.childFragmentManager?.fragments?.get(0)
                    ?.loadImage(passenger.profileImageUri!!, image, Size.SMALL, true)

            nicknameView.text = passenger.nickName
            if (other_rv != null) {
                button.visibility = View.VISIBLE

                if (passenger.uid == null){
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

                    viewModel.totalSeats.observe(context as LifecycleOwner){ it ->
                        if(viewModel.newTrip.acceptedUsersUids.size < it ?: 0) {

                            button.isEnabled = true
                            button.setOnClickListener {
                                this_rv.remove(position)
                                other_rv.add(passenger)
                            }
                        }
                        else{
                            button.isEnabled = true
                            button.setOnClickListener {
                                Snackbar.make(it, context.getString(R.string.seats_finished), Snackbar.LENGTH_LONG).show()
                            }
                        }
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

    private val passengers: MutableList<Profile> = mutableListOf()

    init {
        // download users data from db
        viewModel.downloadUsers{
            passengers.addAll(
            if(acceptedAdapter == null)
                viewModel.newTrip.acceptedUsersUids.map {
                    viewModel.getProfileByUid(it)
                } as MutableList<Profile>
            else viewModel.newTrip.interestedUsersUids.map {
                viewModel.getProfileByUid(it)
            } as MutableList<Profile> )
            this.notifyDataSetChanged()

            if (acceptedAdapter == null){
                viewModel.acceptedExpandVisibility.value = if (passengers.size == 0) View.GONE else View.VISIBLE
            }
            else{
                viewModel.interestedExpandVisibility.value = if (passengers.size == 0) View.GONE else View.VISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.passenger_item, parent, false)
        return ItemViewHolder(layout, this, acceptedAdapter)
    }

    override fun getItemCount(): Int {
        return passengers.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(context, viewModel, passengers[position], position, this, acceptedAdapter)
    }

    fun add(passenger: Profile) {
        passengers.add(passenger)
        this.notifyItemInserted(passengers.size - 1)
        viewModel.putToAccepted(passenger.uid!!)

        if (acceptedAdapter == null){
            viewModel.acceptedExpandVisibility.value = if (passengers.size == 0) View.GONE else View.VISIBLE
        }
        else{
            viewModel.interestedExpandVisibility.value = if (passengers.size == 0) View.GONE else View.VISIBLE
        }
    }

    fun remove(position: Int) {
        passengers.removeAt(position)
        this.notifyItemRemoved(position)
        this.notifyItemRangeChanged(position, passengers.size - position)

        if (acceptedAdapter == null){
            viewModel.acceptedExpandVisibility.value = if (passengers.size == 0) View.GONE else View.VISIBLE
        }
        else{
            viewModel.interestedExpandVisibility.value = if (passengers.size == 0) View.GONE else View.VISIBLE
        }
    }

}
