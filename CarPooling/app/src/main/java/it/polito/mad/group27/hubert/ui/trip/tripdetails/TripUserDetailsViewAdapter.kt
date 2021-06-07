package it.polito.mad.group27.hubert.ui.trip.tripdetails

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import it.polito.mad.group27.hubert.entities.Profile
import it.polito.mad.group27.hubert.R

class TripUserDetailsViewAdapter (
    private val values: List<Profile>,
    private val context: Context
) : RecyclerView.Adapter<TripUserDetailsViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.passenger_item_details, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(context, holder, position)
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val viewProfile: LinearLayout  = view.findViewById(R.id.viewProfileLink)
        val nickname: TextView = view.findViewById(R.id.nickname_details)
        val image: ImageView = view.findViewById(R.id.passenger_image_details)

        fun bind(context: Context, holder: ViewHolder, position: Int) {
            val item = values[position]

            if(item.profileImageUri != null)
                Glide.with(context).load(item.profileImageUri).circleCrop().into(holder.image)

            holder.nickname.text = item.nickName
            if (item.uid == null){
                viewProfile.isEnabled = false
                viewProfile.isClickable = false
            }
            else {
                viewProfile.isClickable = true
                viewProfile.setOnClickListener {
                    it.findNavController().navigate(
                        R.id.action_tripDetailsFragment_to_nav_profile,
                        bundleOf("profile" to item)
                    )
                }
           }
        }

    }
}