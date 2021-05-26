package it.polito.mad.group27.carpooling.ui.trip.tripedit

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import it.polito.mad.group27.carpooling.R
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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
    }

}