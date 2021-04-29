package it.polito.mad.group27.carpooling.ui.profile.showprofile

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import it.polito.mad.group27.carpooling.MainActivity
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.ui.BaseFragmentWithToolbar


class ShowProfileFragment : BaseFragmentWithToolbar(
    R.layout.show_profile_fragment,
    R.menu.show_menu, null
) {

    private lateinit var viewModel: ShowProfileViewModel

    private lateinit var profileImageView: ImageView
    private lateinit var nickNameView: TextView
    private lateinit var emailView: TextView
    private lateinit var locationView: TextView
    private lateinit var registrationDateView: TextView
    private lateinit var reputationBar: RatingBar
    private var fullNameView : TextView? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ShowProfileViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        profileImageView = view.findViewById(R.id.imageProfileView)
        nickNameView = view.findViewById(R.id.nicknameView)
        emailView = view.findViewById(R.id.emailView)
        locationView = view.findViewById(R.id.locationView)
        registrationDateView = view.findViewById(R.id.registrationDateView)
        reputationBar = view.findViewById(R.id.ratingBar)

        fullNameView = view.findViewById(R.id.nameView)

        updateFields()
    }

    private fun updateFields() {
        val act = activity as MainActivity
        val profile = act.profile
        val profileImage = act.profileImage

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            updateTitle(profile.fullName)
        else{
            fullNameView?.text = profile.fullName
        }
        if(profileImage!=null)
            profileImageView.setImageBitmap(profileImage)
        nickNameView.text = profile.nickName
        emailView.text = profile.email
        locationView.text = profile.location
        registrationDateView.text = profile.registrationDate.toString() // TODO fix format
        reputationBar.rating = profile.rating
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