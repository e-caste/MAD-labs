package it.polito.mad.group27.carpooling.ui.trip.tripedit

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import it.polito.mad.group27.carpooling.R

class TripEditFragment : Fragment(R.layout.trip_edit_fragment) {

    private lateinit var viewModel: TripEditViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TripEditViewModel::class.java)
        // TODO: Use the ViewModel
    }

}