package it.polito.mad.group27.carpooling.ui.trip.triplist

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.trip.triplist.dummy.DummyContent


/**
 * A fragment representing a list of Items.
 */
class TripList : Fragment(R.layout.fragment_trip_list) {

    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



//        arguments?.let {
//            columnCount = it.getInt(ARG_COLUMN_COUNT)
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trip_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                // TODO: when is this called?
                layoutManager = when (resources.configuration.orientation) {
                    Configuration.ORIENTATION_LANDSCAPE -> {
                        Log.d(getLogTag(), "orientation is landscape, using grid layout...")
                        GridLayoutManager(context, 2)
                    }
                    else -> {
                        Log.d(getLogTag(), "orientation is portrait, using linear layout...")
                        LinearLayoutManager(context)
                    }
                }
                adapter = TripCardRecyclerViewAdapter(DummyContent.ITEMS, findNavController())
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        recyclerView.layoutManager = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                Log.d(getLogTag(), "orientation is landscape, using grid layout...")
                // TODO: select number of columns based on screen width
                GridLayoutManager(context, 2)
            }
            else -> {
                Log.d(getLogTag(), "orientation is portrait, using linear layout...")
                LinearLayoutManager(context)
            }
        }
        recyclerView.adapter = TripCardRecyclerViewAdapter(DummyContent.ITEMS, findNavController())

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
            findNavController().navigate(R.id.action_tripList_to_tripEditFragment)
        }


    }
}