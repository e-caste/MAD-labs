package it.polito.mad.group27.carpooling.ui.trip.tripdetails

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.group27.carpooling.MainActivity
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.trip.tripdetails.dummy.DummyStageContent
import it.polito.mad.group27.carpooling.ui.trip.triplist.dummy.DummyContent

class TripDetailsFragment : Fragment(R.layout.trip_details_fragment) {

    private lateinit var viewModel: TripDetailsViewModel
    private lateinit var dropdownListButton : LinearLayout

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TripDetailsViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.show_profile_menu, menu)
        Log.d(getLogTag(),"Toolbar displayed")
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.trip_details_fragment, container, false)

        if (view is RecyclerView) {
            with(view) {
                adapter = TripStageViewAdapter(DummyStageContent.ITEMS)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        dropdownListButton = view.findViewById(R.id.startTripView)

        val activity = activity as MainActivity
        activity.setSupportActionBar(toolbar)
        activity.setupActionBarWithNavController(findNavController(), activity.appBarConfiguration)
        activity.supportActionBar?.title = "Trip details"

        val recyclerView = view.findViewById<RecyclerView>(R.id.tripStageList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = TripStageViewAdapter(DummyStageContent.ITEMS)


        dropdownListButton.setOnClickListener {
            val arrowImageView : ImageView = view.findViewById(R.id.expandButton)
            val recyclerView = view.findViewById<RecyclerView>(R.id.tripStageList)

            Log.d(getLogTag(),"Touched route list")

            recyclerView.visibility =
                    when(recyclerView.visibility) {
                View.GONE -> {
                    arrowImageView.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
                    View.VISIBLE
                }
                else ->{
                    arrowImageView.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                    View.GONE
                }
            }
        }
    }

}