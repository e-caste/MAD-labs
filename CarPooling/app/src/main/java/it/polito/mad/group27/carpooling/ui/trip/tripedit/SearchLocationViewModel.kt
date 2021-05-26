package it.polito.mad.group27.carpooling.ui.trip.tripedit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.osmdroid.util.GeoPoint

class SearchLocationViewModel : ViewModel() {
    val geoPoint: MutableLiveData<GeoPoint?> = MutableLiveData(null)
    val locationString: MutableLiveData<String?> = MutableLiveData(null)
    val searchSuggestions: MutableLiveData<List<Pair<String, GeoPoint>>?> = MutableLiveData(null)
}
data class Suggestion (
    val lat:String,
    val lon:String,
    val display_name:String,
    val address:Address?
        ){
    val geopoint:GeoPoint
        get()= GeoPoint(lat.toDouble(), lon.toDouble())

    override fun toString():String{
        //TODO see if addess is present and format it
        return display_name
    }
}

data class Address (
    val road:String,
    val village: String,
    val county:String,
    val state:String,
    val country:String,
    val country_code: String
        )


