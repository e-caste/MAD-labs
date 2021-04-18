package it.polito.mad.group27.carpooling.ui.profile.editprofile

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle

import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.ui.profile.ProfileFragment

class EditProfileFragment : ProfileFragment(R.layout.edit_profile_fragment) {

    private lateinit var viewModel: EditProfileViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(EditProfileViewModel::class.java)
        // TODO: Use the ViewModel
    }

}