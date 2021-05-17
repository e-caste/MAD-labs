package it.polito.mad.group27.carpooling.ui.profile.showprofile

import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import it.polito.mad.group27.carpooling.*
import it.polito.mad.group27.carpooling.ui.BaseFragmentWithToolbar
import it.polito.mad.group27.carpooling.ui.trip.Hour
import it.polito.mad.group27.carpooling.ui.trip.Trip
import it.polito.mad.group27.carpooling.ui.trip.TripDB
import it.polito.mad.group27.carpooling.ui.trip.triplist.BaseTripList
import java.math.BigDecimal
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


class ShowProfileFragment : BaseFragmentWithToolbar(
    R.layout.show_profile_fragment,
    R.menu.show_menu, null
) {

    private lateinit var profileImageView: ImageView
    private lateinit var nickNameView: TextView
    private lateinit var emailView: TextView
    private lateinit var locationView: TextView
    private lateinit var registrationDateView: TextView
    private lateinit var reputationBar: RatingBar
    private var fullNameView : TextView? = null
    private var privateMode = false




    private var adapter: TripsRecyclerAdapter? = null

    private val dateFormatter = SimpleDateFormat.getDateInstance()

    private lateinit var profileViewModel: ProfileBaseViewModel

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if(!privateMode){
            val inflater: MenuInflater = inflater
            inflater.inflate(optionsMenuId, menu)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profile = arguments?.getParcelable<Profile>("profile")

        val recyclerView = view.findViewById<RecyclerView>(R.id.list)


        profileImageView = view.findViewById(R.id.imageProfileView)
        nickNameView = view.findViewById(R.id.nicknameView)
        emailView = view.findViewById(R.id.emailView)
        locationView = view.findViewById(R.id.locationView)
        registrationDateView = view.findViewById(R.id.registrationDateView)
        reputationBar = view.findViewById(R.id.ratingBar)
        fullNameView = view.findViewById(R.id.nameView)

        if(profile!=null){
            privateMode = true
            profileViewModel = ViewModelProvider(this).get(ProfileBaseViewModel::class.java)
            profileViewModel.profile.value= profile
            updateFields(profile)
        }else{
            profileViewModel = ViewModelProvider(act).get(ProfileViewModel::class.java)
            profileViewModel.profile.observe(viewLifecycleOwner) { updateFields(it) }
        }


        if(privateMode){

            val coll = FirebaseFirestore.getInstance().collection("trips")
            val queryBase = coll
                .whereEqualTo("ownerUid", FirebaseAuth.getInstance().currentUser.uid)
                .whereLessThan("startDateTime", Timestamp.now())
                .whereArrayContains("acceptedUsersUids", profileViewModel.profile.value!!.uid!!)
                .orderBy("startDateTime", Query.Direction.ASCENDING)

            val options = FirestoreRecyclerOptions.Builder<TripDB>()
                .setQuery(queryBase, TripDB::class.java)
                .build()

            adapter = TripsRecyclerAdapter(options)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(context)

            view.findViewById<ViewGroup>(R.id.sensible_information).visibility=View.GONE
        }else{
            view.findViewById<ViewGroup>(R.id.travelled_with).visibility=View.GONE
        }

    }

    private fun updateFields(profile: Profile?) {
        if(profile!=null) {


            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                updateTitle(profile.fullName)
            else {
                fullNameView?.text = profile.fullName
            }
            if (profile.profileImageUri != null) {
                val circularProgressDrawable = CircularProgressDrawable(requireContext())
                circularProgressDrawable.strokeWidth = 5f
                circularProgressDrawable.centerRadius = 30f
                circularProgressDrawable.start()

                Glide.with(this).load(profile.profileImageUri)
                    .placeholder(circularProgressDrawable)
                    .into(profileImageView)

            }else
                profileImageView.setImageResource(R.drawable.ic_baseline_person_24)
            nickNameView.text = profile.nickName
            if(!privateMode) {
                emailView.text = profile.email
                locationView.text = profile.location
            }
            registrationDateView.text = dateFormatter.format(profile.registrationDate.toDate()) // TODO fix format
            reputationBar.rating = profile.rating
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.edit_menu_button ->
                findNavController().navigate(R.id.action_showProfileFragment_to_editProfileFragment)
            else -> {
               return super.onOptionsItemSelected(item)
            }

        }
        return true
    }

    private inner class TripsRecyclerAdapter(
        options: FirestoreRecyclerOptions<TripDB>
    ) : FirestoreRecyclerAdapter<TripDB, TripViewHolder>(options) {

        override fun onBindViewHolder(tripViewHolder: TripViewHolder, position: Int, tripDB: TripDB) {
            val trip = tripDB.toTrip()
            Log.d("MAD-group27", "adding trip to TripList: $trip")
            tripViewHolder.bind(trip)

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_trip, parent, false)
            return TripViewHolder(view)
        }

        override fun getItemCount(): Int {
            return this.snapshots.size
        }
    }

    private inner class TripViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy")
        private val priceFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.ITALY)

        val topRightButton: ImageButton = view.findViewById(R.id.topright_button)
        val topRightButtonShadow: ImageView = view.findViewById(R.id.topright_button_shadow)
        val carImageView: ImageView = view.findViewById(R.id.car_image)

        private fun setPrice(price: BigDecimal?) {
            val textView = view.findViewById<TextView>(R.id.price_text)
            textView.text = priceFormat.format(price)
        }

        private fun setCarImageUri(carImageUri: Uri?) {
            if (carImageUri == null) {
                carImageView.setColorFilter(Color.argb(34, 68, 68, 68))
                carImageView.setImageResource(R.drawable.ic_baseline_directions_car_24)
            } else {
                Glide.with(this@ShowProfileFragment).load(carImageUri).into(carImageView)
                carImageView.colorFilter = null
            }
        }

        private fun setFrom(from: String?) {
            val fromTextView = view.findViewById<TextView>(R.id.departure_text)
            fromTextView.text = from
        }

        private fun setTo(to: String?) {
            val toTextView = view.findViewById<TextView>(R.id.destination_text)
            toTextView.text = to
        }

        private fun setStartDateTime(startDateTime: Calendar?) {
            val hourDepartureTextView = view.findViewById<TextView>(R.id.hour_departure_text)
            val dateDepartureTextView = view.findViewById<TextView>(R.id.date_departure_text)
            if (startDateTime != null) {
                hourDepartureTextView.text = Hour(startDateTime.get(Calendar.HOUR_OF_DAY), startDateTime[Calendar.MINUTE]).toString()
                dateDepartureTextView.text = dateFormat.format(startDateTime.time)
            }
        }

        fun bind(trip: Trip) {
            this.setPrice(trip.price)
            this.setCarImageUri(trip.carImageUri)
            this.setFrom(trip.from)
            this.setTo(trip.to)
            this.setStartDateTime(trip.startDateTime)
            topRightButton.visibility =View.GONE
            topRightButtonShadow.visibility =View.GONE
        }

    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }
}

