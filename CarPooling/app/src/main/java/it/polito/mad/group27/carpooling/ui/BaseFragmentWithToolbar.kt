package it.polito.mad.group27.carpooling.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import it.polito.mad.group27.carpooling.MainActivity
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.getLogTag


open class BaseFragmentWithToolbar(resId: Int, private val optionsMenuId: Int): Fragment(resId) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val inflater: MenuInflater = inflater
        inflater.inflate(optionsMenuId, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)

        val act = activity as MainActivity
        act.setSupportActionBar(toolbar)
        act.setupActionBarWithNavController(findNavController(), act.appBarConfiguration)
        act.supportActionBar?.title =  null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar items
        val mDrawerLayout = (activity as MainActivity).drawerLayout
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(Gravity.LEFT); //CLOSE Nav Drawer!
        }else{
            mDrawerLayout.openDrawer(Gravity.LEFT); //OPEN Nav Drawer!
        }
        return true
    }
}