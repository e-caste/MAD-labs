package it.polito.mad.group27.carpooling.ui.trip.tripedit

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.group27.carpooling.getLogTag
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class SearchLocationViewModel : ViewModel() {
    val geoPoint: MutableLiveData<GeoPoint?> = MutableLiveData(null)
    val locationString: MutableLiveData<String?> = MutableLiveData(null)
    val searchSuggestions: MutableLiveData<List<Pair<String, GeoPoint>>?> = MutableLiveData(null)
}

interface SearchAPI{
    @GET("/search")
    suspend fun getSuggestions(@Query("q") search:String,
                       @Query("limit") limit:Int =10,
                       @Query("format") format:String = "json"): List<Suggestion>
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


