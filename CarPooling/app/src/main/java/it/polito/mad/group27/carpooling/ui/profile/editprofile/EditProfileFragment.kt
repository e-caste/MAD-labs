package it.polito.mad.group27.carpooling.ui.profile.editprofile

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import it.polito.mad.group27.carpooling.MainActivity

import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.profile.ProfileFragment

class EditProfileFragment : ProfileFragment(R.layout.edit_profile_fragment, R.menu.edit_profile_menu) {

    private lateinit var viewModel: EditProfileViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(EditProfileViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save_profile ->{
                //TODO save
                findNavController().navigate(R.id.action_editProfileFragment_to_showProfileFragment)
            }
        }
        return true
    }

}