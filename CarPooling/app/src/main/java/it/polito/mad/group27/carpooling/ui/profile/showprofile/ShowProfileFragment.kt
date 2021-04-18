package it.polito.mad.group27.carpooling.ui.profile.showprofile

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import it.polito.mad.group27.carpooling.MainActivity
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.profile.ProfileFragment

class ShowProfileFragment : ProfileFragment(R.layout.show_profile_fragment) {

    private lateinit var viewModel: ShowProfileViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ShowProfileViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val inflater: MenuInflater = inflater
        inflater.inflate(R.menu.show_profile_menu, menu)
        Log.d(getLogTag(), "HELLO")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)

        val act = activity as MainActivity
        act.setSupportActionBar(toolbar)
        act.setupActionBarWithNavController(findNavController(), act.appBarConfiguration)
        act.supportActionBar?.title = "PIPPO"


    }



}