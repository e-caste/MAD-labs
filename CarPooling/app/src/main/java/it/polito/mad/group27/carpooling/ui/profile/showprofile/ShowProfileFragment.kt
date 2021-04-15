package it.polito.mad.group27.carpooling.ui.profile.showprofile

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import it.polito.mad.group27.carpooling.R

class ShowProfileFragment : Fragment(R.layout.show_profile_fragment) {

    private lateinit var viewModel: ShowProfileViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ShowProfileViewModel::class.java)
        // TODO: Use the ViewModel
    }

}