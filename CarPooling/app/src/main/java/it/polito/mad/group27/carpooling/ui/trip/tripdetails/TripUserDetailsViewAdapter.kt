package it.polito.mad.group27.carpooling.ui.trip.tripdetails

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import it.polito.mad.group27.carpooling.Profile
import it.polito.mad.group27.carpooling.R

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
        val item = values[position]

        if(item.profileImageUri != null)
            Glide.with(context).load(item.profileImageUri).circleCrop().into(holder.image)

        holder.nickname.text = item.nickName
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nickname: TextView = view.findViewById(R.id.nickname_details)
        val image: ImageView = view.findViewById(R.id.passenger_image_details)
    }
}