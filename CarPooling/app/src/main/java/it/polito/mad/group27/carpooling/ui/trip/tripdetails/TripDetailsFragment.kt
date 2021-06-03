package it.polito.mad.group27.carpooling.ui.trip.tripdetails

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.os.bundleOf
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import it.polito.mad.group27.carpooling.*
import it.polito.mad.group27.carpooling.entities.Profile
import it.polito.mad.group27.carpooling.entities.Review
import it.polito.mad.group27.carpooling.ui.BaseFragmentWithToolbar
import it.polito.mad.group27.carpooling.ui.trip.Hour
import it.polito.mad.group27.carpooling.ui.trip.Option
import it.polito.mad.group27.carpooling.ui.trip.Trip
import it.polito.mad.group27.carpooling.ui.trip.TripDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class TripDetailsFragment : BaseFragmentWithToolbar(R.layout.trip_details_fragment,
        R.menu.show_menu, null) {

    private var privateMode = false
    private var tripIsAdvertised = true
    private val currentUserUid = FirebaseAuth.getInstance().currentUser.uid
    private val db = FirebaseFirestore.getInstance()
    private var reviewAdapter: ReviewFirestoreRecyclerAdapter? = null
    private var showReviewForm: Boolean = true
    private val dropdownPassengers: MutableList<Profile> = mutableListOf()
    private lateinit var driver: Profile
    private lateinit var selectedDropdownPassenger: Profile

    private lateinit var fragmentTitle: TextView
    private lateinit var tripDetailsViewModel: TripDetailsViewModel
    private lateinit var dropdownListButton: LinearLayout
    private lateinit var expandButton: ImageView
    private lateinit var interestedExpandButton: ImageView
    private lateinit var acceptedExpandButton: ImageView

    private lateinit var driverInfo: LinearLayout
    private lateinit var driverLink: LinearLayout
    private lateinit var driverImage: ImageView
    private lateinit var driverNickname: TextView
    private lateinit var driverRating: RatingBar
    private lateinit var seatsView: TextView
    private lateinit var dateView: TextView
    private lateinit var estimatedTimeView: TextView
    private lateinit var priceView: TextView
    private lateinit var departureDateTime: TextView
    private lateinit var departureLocation: TextView
    private lateinit var destinationDateTime: TextView
    private lateinit var destinationLocation: TextView
    private lateinit var luggageView: LinearLayout
    private lateinit var animalsView: LinearLayout
    private lateinit var smokersView: LinearLayout
    private lateinit var additionalInfo: LinearLayout
    private lateinit var optionsView: LinearLayout
    private lateinit var travellersDetails: LinearLayout
    private lateinit var interestedUsers: LinearLayout
    private lateinit var acceptedUsers: LinearLayout
    private lateinit var infoText: TextView
    private lateinit var carImageView: ImageView
    private lateinit var stopsRecyclerView: RecyclerView
    private lateinit var interestedUsersRecyclerView: RecyclerView
    private lateinit var acceptedUsersRecyclerView: RecyclerView
    private lateinit var noTravellerInfoMessage: TextView
    private lateinit var unadvertisedTripMessage: TextView
    private lateinit var bookingFAB: FloatingActionButton
    private lateinit var map: MyMapView


    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var warningMessageNoReviews: TextView
    private lateinit var reviewForm: LinearLayout
    private lateinit var reviewFormTitle: TextView
    private lateinit var reviewFormDropdownEnclosure: TextInputLayout
    private lateinit var reviewFormDropdown: AutoCompleteTextView
    private lateinit var reviewFormRating: RatingBar
    private lateinit var reviewFormTextfieldLayout: TextInputLayout
    private lateinit var reviewFormTextfield: TextInputEditText
    private lateinit var reviewFormSendButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tripDetailsViewModel = ViewModelProvider(this).get(TripDetailsViewModel::class.java)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.trip_details_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tripId = arguments?.getString("tripId") ?: ""

        Log.d(getLogTag(), "got tripId from bundle: $tripId")

        if (tripId.isEmpty()) {
            throw Exception("Trip id was null")
        } else {
            tripDetailsViewModel.trip.value = tripDetailsViewModel.loadTrip(tripId)
            Log.d(getLogTag(),"int: ${tripDetailsViewModel.interestedExpanded == View.VISIBLE}")
            Log.d(getLogTag(),"acc: ${tripDetailsViewModel.acceptedExpanded == View.VISIBLE}")
        }

        // Find views
        dropdownListButton = view.findViewById(R.id.startTripView)
        expandButton = view.findViewById(R.id.expandButton)
        carImageView = view.findViewById(R.id.image_details_view)
        estimatedTimeView = view.findViewById(R.id.estimated_time_details)
        seatsView = view.findViewById(R.id.showTripSeats)
        dateView = view.findViewById(R.id.showTripDate)
        priceView = view.findViewById(R.id.showTripPrice)
        departureDateTime = view.findViewById(R.id.departureTimeDetails)
        departureLocation = view.findViewById(R.id.departureNameDetails)
        destinationDateTime = view.findViewById(R.id.tripStopDateTime)
        destinationLocation = view.findViewById(R.id.tripStopName)
        luggageView = view.findViewById(R.id.luggage_details)
        animalsView = view.findViewById(R.id.animals_details)
        smokersView = view.findViewById(R.id.smokers_details)
        additionalInfo = view.findViewById(R.id.additional_info_details)
        optionsView = view.findViewById(R.id.trip_options_details)
        infoText = view.findViewById(R.id.extra_info_text_details)
        stopsRecyclerView = view.findViewById(R.id.tripStopList)
        travellersDetails = view.findViewById(R.id.traveller_details_view)
        interestedUsers = view.findViewById(R.id.interested_users_expand_view)
        acceptedUsers = view.findViewById(R.id.accepted_users_expand_view)
        interestedUsersRecyclerView = view.findViewById(R.id.interested_list)
        acceptedUsersRecyclerView = view.findViewById(R.id.accepted_list)
        interestedExpandButton = view.findViewById(R.id.expand_interested_button)
        acceptedExpandButton = view.findViewById(R.id.expand_accepted_button)
        noTravellerInfoMessage = view.findViewById(R.id.no_traveller_info_message)
        unadvertisedTripMessage = view.findViewById(R.id.unadvertised_trip_message)
        bookingFAB = view.findViewById(R.id.sign_as_interested_fab)
        driverInfo = view.findViewById(R.id.view_driver_profile_trip_details)
        driverLink = view.findViewById(R.id.view_driver_profile_link)
        driverImage = view.findViewById(R.id.driver_image_trip_details)
        driverNickname = view.findViewById(R.id.driver_nickname_trip_details)
        driverRating = view.findViewById(R.id.driver_rating_trip_details)
        map = view.findViewById(R.id.map_trip_details)

        reviewsRecyclerView = view.findViewById(R.id.trip_reviews_list)
        warningMessageNoReviews = view.findViewById(R.id.warning_message_noreviews)
        reviewForm = view.findViewById(R.id.review_form)
        reviewFormTitle = view.findViewById(R.id.review_form_title)
        reviewFormDropdownEnclosure = view.findViewById(R.id.review_form_dropdown_enclosure)
        reviewFormDropdown = view.findViewById(R.id.review_form_dropdown)
        reviewFormRating = view.findViewById(R.id.review_form_rating)
        reviewFormTextfieldLayout = view.findViewById(R.id.review_form_textfield_layout)
        reviewFormTextfield = view.findViewById(R.id.review_form_textfield)
        reviewFormSendButton = view.findViewById(R.id.review_form_button_send)

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setViewParent(view.findViewById<NestedScrollView>(R.id.show_trip_details))
        map.isVerticalMapRepetitionEnabled = false
        map.setScrollableAreaLimitLatitude(MapView.getTileSystem().maxLatitude, MapView.getTileSystem().minLatitude+5.0, 10)
        map.controller.setCenter(GeoPoint(49.8, 6.12))
        map.minZoomLevel = 3.3
        map.controller.setZoom(5.5)

        val rotationGestureOverlay = RotationGestureOverlay(map)
        rotationGestureOverlay.isEnabled = true
        map.setMultiTouchControls(true)
        map.overlays.add(rotationGestureOverlay)


        checkPrivateMode()
        checkAdvertised()

        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT)
            fragmentTitle = view.findViewById(R.id.trip_title_details)

        updateFields(tripDetailsViewModel.trip.value ?: Trip())
        tripDetailsViewModel.trip.observe(viewLifecycleOwner) {
            if (it != null) {
                updateFields(it)
                if(it.acceptedUsersUids.size == it.totalSeats){
                    bookingFAB.visibility = View.GONE
                }
            }
        }

        val dropdownClickListener : (View, ImageView) -> Unit = {
                dropdownView, button ->
            if(dropdownView.visibility == View.VISIBLE){
                dropdownView.visibility = View.GONE
                button.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
            }
            else{
                dropdownView.visibility = View.VISIBLE
                button.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
            }
        }

        val bookingFABListenerBooked : (View) -> Unit = {
            if (currentUserUid in tripDetailsViewModel.trip.value!!.acceptedUsersUids ||
                currentUserUid in tripDetailsViewModel.trip.value!!.interestedUsersUids
            ) {
                Log.d(getLogTag(), "already booked on view created")
                bookingFAB.setImageResource(R.drawable.ic_baseline_done_24)
                bookingFAB.setOnClickListener {
                    Toast.makeText(requireContext(), getString(R.string.warning_message_alreadybooked), Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

        val bookingFABListenerNotBooked : (View) -> Unit = {
            tripDetailsViewModel.trip.value!!.interestedUsersUids.add(currentUserUid)
            FirebaseFirestore.getInstance().collection("trips")
                .document(tripDetailsViewModel.trip.value!!.id!!)
                .set(tripDetailsViewModel.trip.value!!.toTripDB())
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), getString(R.string.success_message_booked), Toast.LENGTH_LONG).show()
                    var tripOwner: Profile?
                    FirebaseFirestore.getInstance().collection("users")
                        .document(tripDetailsViewModel.trip.value!!.ownerUid).get()
                        .addOnSuccessListener {
                            if (it != null) {
                                tripOwner = it.toObject(Profile::class.java)
                                val me = ViewModelProvider(act).get(ProfileViewModel::class.java).profile.value
                                if (tripOwner != null && me != null) {
                                    MessagingService.sendNotification(
                                        tripOwner!!.notificationToken,
                                        AndroidNotification(
                                            "New trip reservation!",
                                            "User ${me.fullName} has just booked your trip from " +
                                                    "${tripDetailsViewModel.trip.value!!.from} " +
                                                    "to ${tripDetailsViewModel.trip.value!!.to} " +
                                                    "on ${
                                                        SimpleDateFormat("dd/MM/yyyy HH:mm").format(
                                                            tripDetailsViewModel.trip.value!!.startDateTime.time
                                                        )
                                                    }",
                                            tripDetailsViewModel.trip.value!!.carImageUri.toString()
                                        )
                                    )
                                    Log.d(
                                        getLogTag(), "reservation notification: sent " +
                                            "from ${me.fullName} (${me.uid}) " +
                                            "to ${tripOwner!!.fullName} (${tripOwner!!.uid})!")
                                    tripDetailsViewModel.userIsBooked.value = true
                                }
                            }
                            bookingFAB.setImageResource(R.drawable.ic_baseline_done_24)
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.warning_message_failedbooking),
                        Toast.LENGTH_LONG
                    ).show()
                }

        }

        dropdownListButton.setOnClickListener{
            dropdownClickListener(stopsRecyclerView,expandButton)
            tripDetailsViewModel.stopsExpanded = stopsRecyclerView.visibility
        }

        interestedUsers.setOnClickListener{
            dropdownClickListener(interestedUsersRecyclerView, interestedExpandButton)
            tripDetailsViewModel.interestedExpanded = interestedUsersRecyclerView.visibility
        }

        acceptedUsers.setOnClickListener{
            dropdownClickListener(acceptedUsersRecyclerView, acceptedExpandButton)
            tripDetailsViewModel.acceptedExpanded = acceptedUsersRecyclerView.visibility
        }

        tripDetailsViewModel.interestedList.observe(viewLifecycleOwner){
            interestedUsers.visibility = if(it.isNotEmpty()) View.VISIBLE else View.GONE
        }

        tripDetailsViewModel.acceptedList.observe(viewLifecycleOwner){
            acceptedUsers.visibility = if(it.isNotEmpty()) View.VISIBLE else View.GONE
        }

        tripDetailsViewModel.userIsBooked.observe(viewLifecycleOwner){
            bookingFAB.setImageResource(
                if(it) R.drawable.ic_baseline_done_24 else R.drawable.ic_baseline_add_24)
            bookingFAB.setOnClickListener(
                if(it) bookingFABListenerBooked else bookingFABListenerNotBooked
            )
        }

        tripDetailsViewModel.driverProfile.observe(viewLifecycleOwner){
            if(it != null){
                if(it.profileImageUri != null) {
                    Glide.with(requireContext()).load(it.profileImageUri).circleCrop()
                        .into(driverImage)
                } else {
                    driverImage.setImageResource(R.drawable.ic_baseline_person_24)
                }
                driverNickname.text = it.nickName
                if(it.countRatingsDriver > 0) {
                    driverRating.rating = (it.sumRatingsDriver / it.countRatingsDriver).toFloat()
                } else {
                    driverRating.visibility = View.GONE
                }
                driverLink.isClickable = true
                driverLink.setOnClickListener { profile ->
                    profile.findNavController().navigate(
                        R.id.action_tripDetailsFragment_to_nav_profile,
                        bundleOf("profile" to it)
                    )
                }
            }
        }

        tripDetailsViewModel.stopList.observe(viewLifecycleOwner){
            if(it != null && it.size > 1) {
                while (map.overlays.size > 1)
                    map.overlays.removeLast()
                for (stop in it) {
                    val marker = Marker(map)
                    marker.position = stop.geoPoint
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = stop.place
                    if (map.overlays.size > map.overlays.size + 1) {
                        map.overlays.removeAt(map.overlays.size - 1)
                    }
                    map.overlays.add(map.overlays.size, marker)
                }
                map.controller.animateTo(it.first().geoPoint)

                if(tripDetailsViewModel.activeRoadSearchJob!= null && tripDetailsViewModel.activeRoadSearchJob!!.isActive){
                    tripDetailsViewModel.activeRoadSearchJob!!.cancel()
                }
                tripDetailsViewModel.activeRoadSearchJob = MainScope().launch {
                    withContext(Dispatchers.IO) {
                        val roadManager: RoadManager = OSRMRoadManager(context, "")
                        val road: Road = roadManager.getRoad(it.map { stop -> stop.geoPoint } as ArrayList<GeoPoint>)
                        val roadOverlay: Polyline = RoadManager.buildRoadOverlay(road, Color.BLUE, 10.0f)
                        map.overlays.add(roadOverlay)
                        map.invalidate()
                    }
                }
            }
        }

            if(!checkPrivateMode()) {
                tripDetailsViewModel.checkBookedUser(currentUserUid)
            }

        val tripDocRef = db
            .collection("trips")
            .document(tripId)
        val query = db
            .collection("reviews")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .whereEqualTo("tripId", tripDocRef)

        val options = FirestoreRecyclerOptions.Builder<Review>()
            .setQuery(query, Review::class.java)
            .build()
        reviewAdapter = ReviewFirestoreRecyclerAdapter(options) {
            if (reviewAdapter!!.getShownItemCount() == 0) {
                reviewsRecyclerView.visibility = View.GONE
                warningMessageNoReviews.visibility = View.VISIBLE
            } else {
                reviewsRecyclerView.visibility = View.VISIBLE
                warningMessageNoReviews.visibility = View.GONE
            }
        }
        reviewsRecyclerView.adapter = reviewAdapter
        reviewsRecyclerView.layoutManager = LinearLayoutManager(context)

        // initialize review form

        Log.d(getLogTag(), "private mode: $privateMode - check: ${checkPrivateMode()}")
        query.get()
            .addOnCompleteListener { query ->
                if (query.isSuccessful) {
                    val reviews = query.result?.toObjects(Review::class.java)!!
                    Log.d(getLogTag(), "reviews are $reviews")
                    // detect if current user can send a review
                    if (privateMode) { // driver
                        val dropdownPassengerUids = mutableListOf<String>()
                        for (passengerUid in tripDetailsViewModel.trip.value!!.acceptedUsersUids) {
                            if (!reviews.any { it.passengerUid?.id == passengerUid && !it.isForDriver }) {
                                dropdownPassengerUids.add(passengerUid)
                            }
                        }
                        Log.d(getLogTag(), "accepted uids: ${tripDetailsViewModel.trip.value!!.acceptedUsersUids} - dropdown uids: $dropdownPassengerUids")
                        // is there any passenger the driver has not reviewed yet?
                        showReviewForm = dropdownPassengerUids.size > 0 &&
                                Calendar.getInstance() > tripDetailsViewModel.trip.value!!.startDateTime
                        if (showReviewForm) {
                            db.collection("users")
                                .whereIn("uid", dropdownPassengerUids)
                                .get()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        task.result?.toObjects(Profile::class.java)?.let {
                                                it1 -> dropdownPassengers.addAll(it1)
                                        }
                                        Log.d(getLogTag(), "dropdownPassengers are $dropdownPassengers")
                                        reviewFormDropdown.setText(dropdownPassengers[0].fullName)
                                        reviewFormDropdown.setAdapter(ArrayAdapter(requireContext(), R.layout.dropdown_element, dropdownPassengers.map { it.fullName }))
                                        reviewFormTitle.text = getString(R.string.review_form_title, dropdownPassengers[0].fullName)
                                        selectedDropdownPassenger = dropdownPassengers[0]
                                        reviewFormDropdown.setOnItemClickListener { _, _, i, _ ->
                                            reviewFormTitle.text = getString(R.string.review_form_title, dropdownPassengers[i].fullName)
                                            selectedDropdownPassenger = dropdownPassengers[i]
                                        }
                                    } else {
                                        Log.d(getLogTag(), "dropdownPassengers query failed")
                                    }
                                }
                            reviewFormTextfield.hint = getString(R.string.review_form_textfield, getString(R.string.passenger))
                            reviewFormSendButton.setOnClickListener {
                                if (reviewFormRating.rating == 0F) {
                                    Toast.makeText(context, getString(R.string.warning_message_mustrate), Toast.LENGTH_LONG).show()
                                } else {
                                    db.collection("reviews")
                                        .add(
                                            Review(
                                                tripId = tripDocRef,
                                                passengerUid = selectedDropdownPassenger.uid?.let { it1 -> db.collection("users").document(it1) },
                                                rating = reviewFormRating.rating.toLong(),
                                                comment = if (reviewFormTextfield.text.isNullOrBlank()) null else reviewFormTextfield.text.toString(),
                                                isForDriver = false,
                                                timestamp = Timestamp.now(),
                                            )
                                    ).addOnSuccessListener {
                                        selectedDropdownPassenger.uid?.let { uid ->
                                            db.collection("users")
                                                .document(uid)
                                                .set(
                                                    Profile(
                                                        uid = uid,
                                                        profileImageUri = selectedDropdownPassenger.profileImageUri,
                                                        fullName = selectedDropdownPassenger.fullName,
                                                        nickName = selectedDropdownPassenger.nickName,
                                                        email = selectedDropdownPassenger.email,
                                                        location = selectedDropdownPassenger.location,
                                                        registrationDate = selectedDropdownPassenger.registrationDate,
                                                        notificationToken = selectedDropdownPassenger.notificationToken,
                                                        sumRatingsPassenger = selectedDropdownPassenger.sumRatingsPassenger + reviewFormRating.rating.toLong(),
                                                        countRatingsPassenger = selectedDropdownPassenger.countRatingsPassenger + 1,
                                                        sumRatingsDriver = selectedDropdownPassenger.sumRatingsDriver,
                                                        countRatingsDriver = selectedDropdownPassenger.countRatingsDriver,
                                                    )
                                                )
                                                .addOnSuccessListener {
                                                    dropdownPassengers.remove(selectedDropdownPassenger)
                                                    if (dropdownPassengers.size == 0) {
                                                        showReviewForm = false
                                                        reviewForm.visibility = View.GONE
                                                    } else {
                                                        reviewFormDropdown.setText(dropdownPassengers[0].fullName)
                                                        reviewFormDropdown.setAdapter(ArrayAdapter(requireContext(), R.layout.dropdown_element, dropdownPassengers.map { it.fullName }))
                                                        reviewFormTitle.text = getString(R.string.review_form_title, dropdownPassengers[0].fullName)
                                                        selectedDropdownPassenger = dropdownPassengers[0]
                                                    }
                                                    Toast.makeText(context, getString(R.string.success_ratingsubmitted), Toast.LENGTH_LONG).show()
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(context, getString(R.string.warning_message_failedrating), Toast.LENGTH_LONG).show()
                                                }
                                        }
                                    }.addOnFailureListener {
                                        Toast.makeText(context, getString(R.string.warning_message_failedrating), Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    } else {  // passenger
                        showReviewForm = !reviews.any { it.passengerUid?.id == currentUserUid && it.isForDriver } &&
                                Calendar.getInstance() > tripDetailsViewModel.trip.value!!.startDateTime &&
                                tripDetailsViewModel.trip.value!!.acceptedUsersUids.contains(currentUserUid)
                        reviewFormDropdownEnclosure.visibility = View.GONE
                        if (showReviewForm) {
                            reviewFormTextfield.hint = getString(R.string.review_form_textfield, getString(R.string.driver))
                            db.collection("users")
                                .whereEqualTo("uid", tripDetailsViewModel.trip.value!!.ownerUid)
                                .get()
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        driver = it.result?.toObjects(Profile::class.java)!![0]
                                        Log.d(getLogTag(), "driver is $driver")
                                        reviewFormTitle.text = getString(R.string.review_form_title, driver.fullName)
                                    } else {
                                        Log.d(getLogTag(), "driver query failed")
                                    }
                                }
                            reviewFormSendButton.setOnClickListener {
                                if (reviewFormRating.rating == 0F) {
                                    Toast.makeText(context, getString(R.string.warning_message_mustrate), Toast.LENGTH_LONG).show()
                                } else {
                                    db.collection("reviews").add(
                                        Review(
                                            tripId = tripDocRef,
                                            passengerUid = db.collection("users").document(currentUserUid),
                                            rating = reviewFormRating.rating.toLong(),
                                            comment = if (reviewFormTextfield.text.isNullOrBlank()) null else reviewFormTextfield.text.toString(),
                                            isForDriver = true,
                                            timestamp = Timestamp.now(),
                                        )
                                    ).addOnSuccessListener {
                                        driver.uid?.let { uid ->
                                            db.collection("users")
                                                .document(uid)
                                                .set(
                                                    Profile(
                                                        uid = uid,
                                                        profileImageUri = driver.profileImageUri,
                                                        fullName = driver.fullName,
                                                        nickName = driver.nickName,
                                                        email = driver.email,
                                                        location = driver.location,
                                                        registrationDate = driver.registrationDate,
                                                        notificationToken = driver.notificationToken,
                                                        sumRatingsPassenger = driver.sumRatingsPassenger,
                                                        countRatingsPassenger = driver.countRatingsPassenger,
                                                        sumRatingsDriver = driver.sumRatingsDriver + reviewFormRating.rating.toLong(),
                                                        countRatingsDriver = driver.countRatingsDriver + 1,
                                                    )
                                                )
                                                .addOnSuccessListener {
                                                    showReviewForm = false
                                                    reviewForm.visibility = View.GONE
                                                    Toast.makeText(context, getString(R.string.success_ratingsubmitted), Toast.LENGTH_LONG).show()
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(context, getString(R.string.warning_message_failedrating), Toast.LENGTH_LONG).show()
                                                }
                                        }
                                    }.addOnFailureListener {
                                        Toast.makeText(context, getString(R.string.warning_message_failedrating), Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    }
                    reviewForm.visibility = if (showReviewForm) View.VISIBLE else View.GONE
                } else {
                    Log.d(getLogTag(), "reviews query failed")
                }
            }
    }

    private fun checkAdvertised(): Boolean {
        tripIsAdvertised = tripDetailsViewModel.trip.value!!.advertised
        return tripIsAdvertised
    }

    private fun checkPrivateMode(): Boolean {
        privateMode = tripDetailsViewModel.trip.value!!.ownerUid == FirebaseAuth.getInstance().currentUser!!.uid
        return privateMode
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if(privateMode && tripIsAdvertised){
            menu.clear()
            inflater.inflate(optionsMenuId, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_menu_button -> {
                Log.d(getLogTag(), "Passing bundle of ${tripDetailsViewModel.trip.value}")
                findNavController().navigate(
                    R.id.action_tripDetailsFragment_to_tripEditFragment,
                    bundleOf("trip" to tripDetailsViewModel.trip.value)
                )
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun updateFields(trip: Trip) {
        // Update title only in portrait orientation
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            updateTitle("${getString(R.string.trip_to)} ${trip.to}")
        else {
            "${getString(R.string.trip_to)} ${trip.to}".also { fragmentTitle.text = it }
        }

        if(checkPrivateMode() || !checkAdvertised())
            requireActivity().invalidateOptionsMenu()

        if(!checkAdvertised())
            unadvertisedTripMessage.visibility = if(tripIsAdvertised) View.GONE else View.VISIBLE

        // Display basic info
        if (trip.carImageUri == null) {
            carImageView.setColorFilter(Color.argb(34, 68, 68, 68))
            carImageView.setImageResource(R.drawable.ic_baseline_directions_car_24)
        } else {
            Glide.with(this).load(trip.carImageUri).into(carImageView)
        }

        (trip.acceptedUsersUids.size.toString() + "/" + trip.totalSeats).also {
            seatsView.text = it
        }
        dateView.text = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())
            .format(tripDetailsViewModel.trip.value!!.startDateTime.time)
        setEstimatedTime(trip)
        priceView.text = trip.price.toString()
        departureDateTime.text = getDateTime(tripDetailsViewModel.trip.value!!.startDateTime)
        departureLocation.text = trip.from
        destinationDateTime.text = getDateTime(tripDetailsViewModel.trip.value!!.endDateTime)
        destinationLocation.text = trip.to

        // Additional stops visualization
        if (trip.stops.size > 0) {
            stopsRecyclerView.visibility = tripDetailsViewModel.stopsExpanded
            stopsRecyclerView.layoutManager = LinearLayoutManager(context)
            stopsRecyclerView.adapter =
                TripStopsViewAdapter(tripDetailsViewModel.trip.value!!.stops)

            expandButton.visibility = View.VISIBLE
        } else expandButton.visibility = View.INVISIBLE

        // Display additional info
        if (trip.options.size > 0 || (trip.otherInformation != null && trip.otherInformation!!.trim() != "")) {
            optionsView.visibility = View.VISIBLE
            luggageView.visibility =
                if (trip.options.contains(Option.LUGGAGE)) View.VISIBLE else View.GONE
            animalsView.visibility =
                if (trip.options.contains(Option.ANIMALS)) View.VISIBLE else View.GONE
            smokersView.visibility =
                if (trip.options.contains(Option.SMOKE)) View.VISIBLE else View.GONE

            if (trip.otherInformation != null && trip.otherInformation!!.trim() != "") {
                infoText.text = trip.otherInformation
                additionalInfo.visibility = View.VISIBLE
            } else {
                additionalInfo.visibility = View.GONE
            }
        } else {
            optionsView.visibility = View.GONE
        }

        if(checkPrivateMode()){
            driverInfo.visibility = View.GONE
            bookingFAB.visibility = View.GONE
            travellersDetails.visibility = View.VISIBLE

            if (trip.acceptedUsersUids.size > 0 || trip.interestedUsersUids.size > 0) {
                noTravellerInfoMessage.visibility = View.GONE

                if (trip.interestedUsersUids.size > 0) {
                    interestedUsers.visibility = View.VISIBLE
                    interestedUsersRecyclerView.visibility = tripDetailsViewModel.interestedExpanded
                    interestedUsersRecyclerView.layoutManager = LinearLayoutManager(context)
                    tripDetailsViewModel.loadInterestedUsers {
                        interestedUsersRecyclerView.adapter =
                            TripUserDetailsViewAdapter(tripDetailsViewModel.interestedList.value!!, requireContext())
                    }
                } else interestedUsers.visibility = View.GONE

                if (trip.acceptedUsersUids.size > 0) {
                    acceptedUsers.visibility = View.VISIBLE
                    acceptedUsersRecyclerView.visibility = tripDetailsViewModel.acceptedExpanded
                    acceptedUsersRecyclerView.layoutManager = LinearLayoutManager(context)
                    tripDetailsViewModel.loadAcceptedUsers {
                        acceptedUsersRecyclerView.adapter =
                            TripUserDetailsViewAdapter(tripDetailsViewModel.acceptedList.value!!, requireContext())
                    }
                } else acceptedUsers.visibility = View.GONE

            } else {
                acceptedUsers.visibility = View.GONE
                interestedUsers.visibility = View.GONE
                noTravellerInfoMessage.visibility = View.VISIBLE
            }
        } else {
            driverInfo.visibility = View.VISIBLE
            travellersDetails.visibility = View.GONE
            bookingFAB.visibility = View.VISIBLE
            tripDetailsViewModel.checkBookedUser(currentUserUid)
        }
    }

    private fun getEstimatedTime(start: Calendar, end: Calendar): Hour {
        val deltaMinutes = (end.timeInMillis - start.timeInMillis) / (1000*60)
        val hours = ((deltaMinutes)/60).toInt()
        val minutes = ((deltaMinutes)%60).toInt()
        return Hour(hours, minutes)
    }

    private fun setEstimatedTime(trip: Trip){
        val time = getEstimatedTime(trip.startDateTime, trip.endDateTime)
        val hours = if (time.hour > 0) "${time.hour} h" else ""
        val minutes = if (time.minute > 0) "${time.minute} min" else ""
        estimatedTimeView.text = ("$hours $minutes")
    }

    private fun getDateTime(item: Calendar) :String {
        val date = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ITALY).format(item.timeInMillis).toString()
        val time = Hour(item[Calendar.HOUR_OF_DAY], item[Calendar.MINUTE]).toString()
        return "$date, $time"
    }

    // TRIP REVIEWS MANAGEMENT SECTION

    private inner class ReviewFirestoreRecyclerAdapter(
        options: FirestoreRecyclerOptions<Review>,
        val updateCallback: () -> Unit,
    ) : FirestoreRecyclerAdapter<Review, ReviewViewHolder>(options) {

        override fun onBindViewHolder(reviewViewHolder: ReviewViewHolder, position: Int, review: Review) {
            val reviewIsMine = (privateMode && !review.isForDriver) || (!privateMode && review.isForDriver && review.passengerUid?.id == currentUserUid)
            Log.d(getLogTag(), "adding review (is mine: $reviewIsMine) to list: $review")
            reviewViewHolder.bind(review, reviewIsMine)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.trip_review, parent, false)
            return ReviewViewHolder(view)
        }

        override fun onDataChanged() {
            super.onDataChanged()
            updateCallback()
        }

        override fun getItemCount(): Int {
            return this.snapshots.size
        }

        fun getShownItemCount(): Int {
            return this.snapshots.filter { !(it.comment == null || it.comment.isBlank()) }.size
        }
    }

    private inner class ReviewViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private val mineLayout = view.findViewById<LinearLayout>(R.id.trip_review_mine)
        private val theirsLayout = view.findViewById<LinearLayout>(R.id.trip_review_theirs)
        private var driver: Profile? = null
        private var passenger: Profile? = null

        private fun setComment(comment: String?, body: TextView) {
            body.text = comment
        }

        private fun bindMine(review: Review) {
            mineLayout.visibility = View.VISIBLE
            theirsLayout.visibility = View.GONE
            val title = view.findViewById<TextView>(R.id.review_title_mine)
            val body = view.findViewById<TextView>(R.id.review_body_mine)
            val reviewOf = if (privateMode) passenger?.fullName else driver?.fullName
            title.text = getString(R.string.review_title_mine, reviewOf)
            setComment(review.comment, body)
        }

        private fun bindTheirs(review: Review) {
            mineLayout.visibility = View.GONE
            theirsLayout.visibility = View.VISIBLE
            val avatar = view.findViewById<ImageView>(R.id.review_avatar_theirs)
            val name = view.findViewById<TextView>(R.id.name)
            val body = view.findViewById<TextView>(R.id.review_body_theirs)
            if (privateMode ||  // you are the driver and the review is for you
                (!privateMode && review.isForDriver)  // you are a passenger and the review is for the driver
            ) {
                if (passenger?.profileImageUri != null) {
                    Glide.with(this@TripDetailsFragment).load(passenger?.profileImageUri).into(avatar)
                }
                name.text = passenger?.fullName
            } else {  // you are a passenger || you are the driver and the review is for a passenger
                if (driver?.profileImageUri != null) {
                    Glide.with(this@TripDetailsFragment).load(driver?.profileImageUri).into(avatar)
                }
                name.text = getString(R.string.review_title_theirs_driver, driver?.fullName, passenger?.fullName)
            }
            setComment(review.comment, body)
        }

        fun bind(review: Review, reviewIsMine: Boolean) {
            // don't show reviews that only contain a rating
            if (review.comment == null || review.comment.isBlank()) {
                mineLayout.visibility = View.GONE
                theirsLayout.visibility = View.GONE
                return
            }
            // give it up for callback hell! - could be implemented by awaiting coroutines
            review.tripId?.get()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val tripDB = task.result?.toObject(TripDB::class.java)!!
                        Log.d(getLogTag(), "tripDB is $tripDB")
                        db.collection("users")
                            .whereEqualTo("uid", tripDB.ownerUid)
                            .get()
                            .addOnCompleteListener { task1 ->
                                if (task1.isSuccessful) {
                                    driver = task1.result?.toObjects(Profile::class.java)!![0]
                                    Log.d(getLogTag(), "driver is $driver")
                                    review.passengerUid?.get()
                                        ?.addOnCompleteListener { task2 ->
                                            if (task2.isSuccessful) {
                                                passenger = task2.result?.toObject(Profile::class.java)!!
                                                Log.d(getLogTag(), "passenger is $passenger")
                                                if (reviewIsMine) {
                                                    bindMine(review)
                                                } else {
                                                    bindTheirs(review)
                                                }
                                            }
                                        }
                                }
                            }
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()
        reviewAdapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        reviewAdapter!!.stopListening()
    }
}


class MyMapView(ctx:Context, attrs : AttributeSet?, defStyle :Int , defStyleRes:Int) :
    MapView(ctx , attrs) {

    constructor (ctx : Context ) : this( ctx , null , 0, 0)
    constructor( ctx : Context , attrs : AttributeSet ):
    this(ctx , attrs , 0, 0)
    constructor (ctx : Context , attrs : AttributeSet , defStyle : Int):
    this (ctx , attrs , defStyle ,0)
    private var mViewParent: ViewParent? = null
    //add constructors here
    fun setViewParent(viewParent: ViewParent) { //any ViewGroup
        mViewParent = viewParent
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> if (null == mViewParent) {
                parent.requestDisallowInterceptTouchEvent(true)
            } else {
                mViewParent!!.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_UP -> if (null == mViewParent) {
                parent.requestDisallowInterceptTouchEvent(false)
            } else {
                mViewParent!!.requestDisallowInterceptTouchEvent(false)
            }
            else -> {
            }
        }
        return super.onInterceptTouchEvent(event)
    }
}