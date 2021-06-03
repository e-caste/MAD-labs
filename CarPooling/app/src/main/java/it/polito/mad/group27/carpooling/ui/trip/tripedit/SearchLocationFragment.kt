package it.polito.mad.group27.carpooling.ui.trip.tripedit

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.res.getDrawableOrThrow
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.BaseFragmentWithToolbar
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay


class SearchLocationFragment : BaseFragmentWithToolbar(R.layout.search_location_fragment, R.menu.edit_menu, null) {

    private lateinit var viewModel: SearchLocationViewModel
    private lateinit var map: MapView

    private lateinit var searchPlace: TextInputLayout

    private var baseOverlays: Int = 0

    companion object {
        val REQUEST_KEY = "place"
        val location = "location"
        val geopoint = "geopoint"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SearchLocationViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // remove keyboard at start if present from previous fragment
        act.currentFocus?.clearFocus()
        if(arguments?.getString(location) != "" ){
            viewModel.locationString.value = arguments?.getString(location)

        }
        if(arguments?.getParcelable<GeoPoint>(geopoint) != null){
            viewModel.geoPoint.value = arguments?.getParcelable<GeoPoint>(geopoint)
        }

        if(viewModel.locationString.value != null && viewModel.geoPoint.value == null){
            viewModel.loadGeopointFromText(viewModel.locationString.value!!)
        }

        map = view.findViewById(R.id.edit_trip_mapview)

        // set map style
        map.setTileSource(TileSourceFactory.MAPNIK)

        // set map visualization
        map.isVerticalMapRepetitionEnabled = false
        map.setScrollableAreaLimitLatitude(MapView.getTileSystem().maxLatitude, MapView.getTileSystem().minLatitude+5.0, 10)

        // set position on map opening
        map.controller.setCenter(viewModel.geoPoint.value ?: GeoPoint(49.8, 6.12))
        map.minZoomLevel = 3.3
        map.controller.setZoom(5.5)

        // enable gestures for rotating map
        val rotationGestureOverlay = RotationGestureOverlay(map)
        rotationGestureOverlay.isEnabled = true
        map.setMultiTouchControls(true)
        map.overlays.add(rotationGestureOverlay)

        baseOverlays = map.overlays.size +1

        // observe geopoint to put pinpoint
        viewModel.geoPoint.observe(viewLifecycleOwner){
            if (it != null)
            {
                val marker = Marker(map)
                marker.position = it
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                if (map.overlays.size > baseOverlays) {
                    map.overlays.removeAt(map.overlays.size - 1)
                }
                map.overlays.add(map.overlays.size, marker)
                map.controller.animateTo(it)
            }
            else{
                if (map.overlays.size > baseOverlays) {
                    map.overlays.removeAt(map.overlays.size - 1)
                    //double to zoom to trigger repaint
                    map.controller.zoomTo(5.49)
                    map.controller.zoomTo(5.5)
                }
            }
        }

        // get tap on map
        map.overlays.add(object: Overlay() {
            override fun onSingleTapConfirmed(e: MotionEvent,
                                              mapView: MapView): Boolean {
                val projection = mapView.projection
                val geoPoint = projection.fromPixels(e.x.toInt(),
                    e.y.toInt())
                Log.d(
                    getLogTag(), "${geoPoint.latitude} , ${geoPoint.longitude}")

                viewModel.loadPlaceFromGeopoint(geoPoint as GeoPoint)
                act.currentFocus?.clearFocus()
                return true
            }
        })


        // observe unavailablePlace to show Snackbar
        viewModel.unavailablePlace.observe(viewLifecycleOwner){
            if (it == true){
                Snackbar.make(view, "Selected location does not exist", Snackbar.LENGTH_LONG)
                    .show()
            }
        }

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
            autoCompleteTextView.setText(it)
        }

        searchPlace.setEndIconOnClickListener {
            viewModel.locationString.value = null
            viewModel.geoPoint.value = null

        }
        //TODO add loading field
        viewModel.loadingSuggestions.observe(viewLifecycleOwner){
            adapter.loading = it
            adapter.notifyDataSetChanged()
        }



        viewModel.loadingGeopoint.observe(viewLifecycleOwner){
            if(it){

                searchPlace.endIconMode = TextInputLayout.END_ICON_CUSTOM
                val progress = requireContext().getProgressBarDrawable()
                searchPlace.endIconDrawable =progress
                (progress as? Animatable)?.start()

            }else{
                searchPlace.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
            }
        }

        autoCompleteTextView.doOnTextChanged { text, _, _, _ ->
            adapter.clear()
            if(text !=null && viewModel.locationString.value != text.toString()){
                //it is a text input and not a selection
                Log.d(getLogTag(), "changed to $text")

                viewModel.loadSuggestions(text.toString())

                viewModel.geoPoint.value = null

            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save_menu_button->{
                setFragmentResult(REQUEST_KEY, bundleOf(
                    location to (viewModel.locationString.value ?: ""),
                    geopoint to viewModel.geoPoint.value
                    )
                )
                findNavController().navigateUp()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }


    inner class AutoCompleteTextViewAdapter(private val c: Context, private val layoutResource: Int,
                                            private val suggestions: List<Pair<String, GeoPoint>>) :
        ArrayAdapter<Pair<String, GeoPoint>>(c, layoutResource, suggestions) {

        var loading = true


        override fun getCount(): Int = if (loading) 1 else if(suggestions.isEmpty()) 1 else suggestions.size

        override fun getItem(position: Int): Pair<String, GeoPoint>? = if(loading || suggestions.isEmpty()) null else suggestions[position]

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

            var view = convertView ?: LayoutInflater.from(c).inflate(layoutResource, parent, false)
            val textView = view.findViewById<TextView>(R.id.content)
            if(loading){
                textView.text = null
                textView.visibility = View.GONE
                view.findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
                view.setOnClickListener {}

            }else{
                textView.visibility = View.VISIBLE
                view.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                if(suggestions.isEmpty()){
                    textView.text = "No results for current search"
                }else {

                    textView.text = suggestions[position].first

                    view.setOnClickListener {

                        if (act.currentFocus != null) {
                            val imm: InputMethodManager =
                                context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(act.currentFocus!!.windowToken, 0)

                            act.currentFocus?.clearFocus()
                        }
                        viewModel.geoPoint.value = suggestions[position].second
                        viewModel.locationString.value = suggestions[position].first
                    }
                }
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

fun Context.getProgressBarDrawable(): Drawable {
    val value = TypedValue()
    theme.resolveAttribute(android.R.attr.progressBarStyleSmallTitle, value, false)
    val progressBarStyle = value.data
    val attributes = intArrayOf(android.R.attr.indeterminateDrawable)
    val array = obtainStyledAttributes(progressBarStyle, attributes)
    val drawable = array.getDrawableOrThrow(0)
    array.recycle()
    return drawable
}



