package it.polito.mad.group27.carpooling.ui.profile.showprofile

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import it.polito.mad.group27.carpooling.MainActivity
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.profile.ProfileFragment

class ShowProfileFragment : ProfileFragment(R.layout.show_profile_fragment, R.menu.show_profile_menu) {

    private lateinit var viewModel: ShowProfileViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ShowProfileViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.edit_profile ->
                findNavController().navigate(R.id.action_showProfileFragment_to_editProfileFragment)
            else -> {

            }
        }
        return true
    }



}