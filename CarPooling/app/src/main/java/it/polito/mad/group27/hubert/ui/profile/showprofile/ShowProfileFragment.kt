package it.polito.mad.group27.hubert.ui.profile.showprofile

import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
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
import it.polito.mad.group27.hubert.*
import it.polito.mad.group27.hubert.entities.Profile
import it.polito.mad.group27.hubert.ui.BaseFragmentWithToolbar
import it.polito.mad.group27.hubert.entities.Hour
import it.polito.mad.group27.hubert.entities.Trip
import it.polito.mad.group27.hubert.entities.TripDB
import java.math.BigDecimal
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.format.FormatStyle
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
    private lateinit var reputationBarPassenger: RatingBar
    private lateinit var reputationBarDriver: RatingBar
    private var fullNameView : TextView? = null
    private var privateMode = false




    private var adapterTravelledWithMe: TripsRecyclerAdapter? = null
    private var adapterITravelledWith: TripsRecyclerAdapter? = null

    private val dateFormatter = if(Build.VERSION.SDK_INT >= 26) SimpleDateFormat.getDateInstance(FormatStyle.LONG.ordinal)
                else SimpleDateFormat.getDateInstance()

    private lateinit var profileViewModel: ProfileBaseViewModel

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if(!privateMode){
            val myInflater: MenuInflater = inflater
            myInflater.inflate(optionsMenuId, menu)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profile = arguments?.getParcelable<Profile>("profile")

        val recyclerViewTravelledWithMe = view.findViewById<RecyclerView>(R.id.listTravelledWithMe)
        val recyclerViewITravelledWith = view.findViewById<RecyclerView>(R.id.listITravelledWith)


        profileImageView = view.findViewById(R.id.imageProfileView)
        nickNameView = view.findViewById(R.id.nicknameView)
        emailView = view.findViewById(R.id.emailView)
        locationView = view.findViewById(R.id.locationView)
        registrationDateView = view.findViewById(R.id.registrationDateView)
        reputationBarPassenger = view.findViewById(R.id.ratingBarPassenger)
        reputationBarDriver = view.findViewById(R.id.ratingBarDriver)
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

            act.supportActionBar?.setHomeAsUpIndicator(null)
            act.supportActionBar?.setDisplayHomeAsUpEnabled(true)

            val coll = FirebaseFirestore.getInstance().collection("trips")
            val queryBaseTravelledWithMe = coll
                .whereEqualTo("ownerUid", FirebaseAuth.getInstance().currentUser.uid)
                .whereLessThan("startDateTime", Timestamp.now())
                .whereArrayContains("acceptedUsersUids", profileViewModel.profile.value!!.uid!!)
                .orderBy("startDateTime", Query.Direction.ASCENDING)

            val optionsTravelledWithMe = FirestoreRecyclerOptions.Builder<TripDB>()
                .setQuery(queryBaseTravelledWithMe, TripDB::class.java)
                .build()

            adapterTravelledWithMe = TripsRecyclerAdapter(optionsTravelledWithMe){
                if(adapterTravelledWithMe!!.itemCount == 0)
                    view.findViewById<ViewGroup>(R.id.travelled_with_me).visibility=View.GONE
                else
                    view.findViewById<ViewGroup>(R.id.travelled_with_me).visibility=View.VISIBLE
            }
            recyclerViewTravelledWithMe.adapter = adapterTravelledWithMe
            recyclerViewTravelledWithMe.layoutManager = LinearLayoutManager(context)

            val queryBaseITravelledWith = coll
                .whereEqualTo("ownerUid", profileViewModel.profile.value!!.uid!!)
                .whereLessThan("startDateTime", Timestamp.now())
                .whereArrayContains("acceptedUsersUids",FirebaseAuth.getInstance().currentUser.uid )
                .orderBy("startDateTime", Query.Direction.ASCENDING)


            val optionsITravelledWith = FirestoreRecyclerOptions.Builder<TripDB>()
                .setQuery(queryBaseITravelledWith, TripDB::class.java)
                .build()

            adapterITravelledWith = TripsRecyclerAdapter(optionsITravelledWith){
                if(adapterITravelledWith!!.itemCount == 0)
                    view.findViewById<ViewGroup>(R.id.i_travelled_with).visibility=View.GONE
                else
                    view.findViewById<ViewGroup>(R.id.i_travelled_with).visibility=View.VISIBLE
            }
            recyclerViewITravelledWith.adapter = adapterITravelledWith
            recyclerViewITravelledWith.layoutManager = LinearLayoutManager(context)

            view.findViewById<ViewGroup>(R.id.sensible_information).visibility=View.GONE
        }else{
            view.findViewById<ViewGroup>(R.id.travelled_with_me).visibility=View.GONE
            view.findViewById<ViewGroup>(R.id.i_travelled_with).visibility=View.GONE
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

                loadImage(profile.profileImageUri!!,profileImageView, Size.HUGE)
//                val circularProgressDrawable = CircularProgressDrawable(requireContext())
//                circularProgressDrawable.strokeWidth = 5f
//                circularProgressDrawable.centerRadius = 30f
//                circularProgressDrawable.start()
//
//                Glide.with(this).load(profile.profileImageUri)
//                    .placeholder(circularProgressDrawable)
//                    .into(profileImageView)

            }else
                profileImageView.setImageResource(R.drawable.ic_baseline_person_24)
            nickNameView.text = profile.nickName
            if(!privateMode) {
                emailView.text = profile.email
                locationView.text = profile.location
            }
            registrationDateView.text = dateFormatter.format(profile.registrationDate.toDate())
            if (profile.countRatingsDriver == 0L){
                reputationBarDriver.visibility = View.GONE
                requireView().findViewById<TextView>(R.id.driverRating).visibility = View.GONE

            }else {
                reputationBarDriver.visibility = View.VISIBLE
                requireView().findViewById<TextView>(R.id.driverRating).visibility = View.VISIBLE

                reputationBarDriver.rating = profile.sumRatingsDriver.toFloat()/ profile.countRatingsDriver
            }


            if (profile.countRatingsPassenger == 0L){
                reputationBarPassenger.visibility = View.GONE
                requireView().findViewById<TextView>(R.id.passengerRating).visibility = View.GONE

            }else {
                reputationBarPassenger.visibility = View.VISIBLE
                requireView().findViewById<TextView>(R.id.passengerRating).visibility = View.VISIBLE
                reputationBarPassenger.rating = profile.sumRatingsPassenger.toFloat()/ profile.countRatingsPassenger
            }

            if(profile.countRatingsPassenger == 0L && profile.countRatingsDriver ==0L){
                requireView().findViewById<TextView>(R.id.rating_label).visibility = View.GONE
                requireView().findViewById<View>(R.id.rating_bar).visibility = View.GONE
            }else{
                requireView().findViewById<TextView>(R.id.rating_label).visibility = View.VISIBLE
                requireView().findViewById<View>(R.id.rating_bar).visibility = View.VISIBLE
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.edit_menu_button ->
                findNavController().navigate(R.id.action_showProfileFragment_to_editProfileFragment)
            else -> {
                //Menu button
                Log.d(getLogTag(), "Home pressed")
                if(privateMode)
                    findNavController().navigateUp()
                else return super.onOptionsItemSelected(item)
            }

        }
        return true
    }

    private inner class TripsRecyclerAdapter(
        options: FirestoreRecyclerOptions<TripDB>, val updateCallback: ()->Unit
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

        override fun onDataChanged() {
            super.onDataChanged()
            updateCallback()
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
        adapterITravelledWith?.startListening()
        adapterTravelledWithMe?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterITravelledWith?.stopListening()
        adapterTravelledWithMe?.stopListening()
    }
}

