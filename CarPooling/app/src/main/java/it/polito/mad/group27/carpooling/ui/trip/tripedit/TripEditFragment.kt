package it.polito.mad.group27.carpooling.ui.trip.tripedit

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import it.polito.mad.group27.carpooling.R

class TripEditFragment : Fragment() {

    companion object {
        fun newInstance() = TripEditFragment()
    }

    private lateinit var viewModel: TripEditViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.trip_edit_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TripEditViewModel::class.java)
        // TODO: Use the ViewModel
    }

}