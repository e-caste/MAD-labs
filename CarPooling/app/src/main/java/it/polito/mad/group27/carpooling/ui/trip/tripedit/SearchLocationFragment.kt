package it.polito.mad.group27.carpooling.ui.trip.tripedit

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.ui.BaseFragmentWithToolbar

class SearchLocationFragment : BaseFragmentWithToolbar(R.layout.search_location_fragment, R.menu.edit_menu, null) {

    private lateinit var viewModel: SearchLocationViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SearchLocationViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // observe string to show in text input
        // observe geopoint to put pinpoint
    }

}