package it.polito.mad.group27.carpooling.ui.profile.showprofile

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import it.polito.mad.group27.carpooling.*
import it.polito.mad.group27.carpooling.ui.BaseFragmentWithToolbar
import java.text.SimpleDateFormat


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
            profileViewModel.profile.value= Profile()
            updateFields(profile)
        }else{
            profileViewModel = ViewModelProvider(act).get(ProfileViewModel::class.java)
            profileViewModel.profile.observe(viewLifecycleOwner) { updateFields(it) }
        }


        if(privateMode){
            view.findViewById<ViewGroup>(R.id.sensible_information).visibility=View.GONE
        }

    }

    private fun updateFields(profile: Profile?) {
        if(profile!=null) {


            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                updateTitle(profile.fullName)
            else {
                fullNameView?.text = profile.fullName
            }
            if (profile.profileImageUri != null)
                Glide.with(this).load(profile.profileImageUri ).into(profileImageView)
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



}