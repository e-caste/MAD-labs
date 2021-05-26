package it.polito.mad.group27.carpooling.ui.trip.tripedit

import androidx.lifecycle.ViewModelProvider
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.BaseFragmentWithToolbar
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay


class SearchLocationFragment : BaseFragmentWithToolbar(R.layout.search_location_fragment, R.menu.edit_menu, null) {

    private lateinit var viewModel: SearchLocationViewModel
    private lateinit var map: MapView

    private lateinit var searchPlace: TextInputLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SearchLocationViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        map = view.findViewById(R.id.edit_trip_mapview)

        // set map style
        map.setTileSource(TileSourceFactory.MAPNIK)

        // set map visualization
        map.isVerticalMapRepetitionEnabled = false
        map.setScrollableAreaLimitLatitude(MapView.getTileSystem().maxLatitude, MapView.getTileSystem().minLatitude+5.0, 10)

        // set position on map opening
        map.controller.setCenter(GeoPoint(49.8, 6.12))
        map.minZoomLevel = 3.3
        map.controller.setZoom(5.5)

        // enable gestures for rotating map
        val rotationGestureOverlay = RotationGestureOverlay(map)
        rotationGestureOverlay.isEnabled = true
        map.setMultiTouchControls(true)
        map.overlays.add(rotationGestureOverlay)

        // observe string to show in text input
        // observe geopoint to put pinpoint
        searchPlace = view.findViewById(R.id.search_place)
        val adapter = AutoCompleteTextViewAdapter(requireContext(), R.layout.search_suggestion,
            mutableListOf())
        val autoCompleteTextView = searchPlace.editText as AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapter)

        viewModel.searchSuggestions.observe(viewLifecycleOwner){
            adapter.clear()
            if(it!=null){
                Log.d(getLogTag(), "setting list to ${it.map { element-> element.first }}")
                adapter.addAll(it)
            }
        }

        viewModel.locationString.observe(viewLifecycleOwner){
            if(it!=null){
                autoCompleteTextView.setText(it)
            }
        }
        //TODO add clear button
        //TODO add loading field

        autoCompleteTextView.doOnTextChanged { text, _, _, _ ->
            adapter.clear()
            if(viewModel.locationString.value != text.toString()){
                //it is a text input and not a selection
                Log.d(getLogTag(), "changed to $text")

                viewModel.loadSuggestions(text.toString())

                viewModel.geoPoint.value = null

            }
        }



    }


    inner class AutoCompleteTextViewAdapter(private val c: Context, private val layoutResource: Int, private val suggestions: List<Pair<String, GeoPoint>>) :
        ArrayAdapter<Pair<String, GeoPoint>>(c, layoutResource, suggestions) {



        override fun getCount(): Int = suggestions.size

        override fun getItem(position: Int): Pair<String, GeoPoint> = suggestions[position]

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(c).inflate(layoutResource, parent, false)

            view.findViewById<TextView>(R.id.content).text = suggestions[position].first
            view.setOnClickListener {
                viewModel.geoPoint.value = suggestions[position].second
                viewModel.locationString.value = suggestions[position].first
            }

            return view
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                    notifyDataSetChanged()
                }

                override fun performFiltering(charSequence: CharSequence?): FilterResults {
                    val filterResults = FilterResults()
                    filterResults.values = suggestions
                    return filterResults
                }
            }
        }
    }
}



