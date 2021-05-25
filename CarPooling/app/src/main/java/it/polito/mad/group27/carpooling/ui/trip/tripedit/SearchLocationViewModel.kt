package it.polito.mad.group27.carpooling.ui.trip.tripedit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.osmdroid.util.GeoPoint

class SearchLocationViewModel : ViewModel() {
    val geoPoint: MutableLiveData<GeoPoint?> = MutableLiveData(null)
    val locationString: MutableLiveData<String?> = MutableLiveData(null)
    val searchSuggestions: MutableLiveData<List<Pair<String, GeoPoint>>?> = MutableLiveData(null)
}