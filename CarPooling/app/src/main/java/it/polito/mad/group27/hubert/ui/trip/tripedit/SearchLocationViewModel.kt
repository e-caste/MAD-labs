package it.polito.mad.group27.hubert.ui.trip.tripedit

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.group27.hubert.getLogTag
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class SearchLocationViewModel : ViewModel() {

    val loadingSuggestions: MutableLiveData<Boolean> = MutableLiveData(false)
    val loadingGeopoint: MutableLiveData<Boolean> = MutableLiveData(false)
    val geoPoint: MutableLiveData<GeoPoint?> = MutableLiveData(null)
    val locationString: MutableLiveData<String?> = MutableLiveData(null)
    val searchSuggestions: MutableLiveData<List<Pair<String, GeoPoint>>?> = MutableLiveData(null)

    val unavailablePlace: MutableLiveData<Boolean> = MutableLiveData(false)

    private var activeJob : Job? = null

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SearchAPI::class.java)

    }

    fun loadSuggestions(search:String){
        clearPreviousJobs()
        activeJob = MainScope().launch {
            //TODO manage errors
            loadingSuggestions.value = true
            val results = retrofit.getSuggestions(search)
            searchSuggestions.value = results.map{ Pair(it.toString(), it.geopoint)}
            loadingSuggestions.value = false
        }
    }

    fun loadPlaceFromGeopoint(point: GeoPoint){
        clearPreviousJobs()
        activeJob = MainScope().launch {
                loadingGeopoint.value  = true
            Log.d(getLogTag(), "asking for place at position %.6f".format( point.latitude ).replace(",", ".") + " %.6f".format( point.longitude ).replace(",", "."))
                val result = retrofit.getPlaceFromGeoPoint(
                        "%.6f".format( point.latitude ).replace(",", "."),
                        "%.6f".format( point.longitude ).replace(",", "."))
                if(result?.display_name != null) {
                    locationString.value = result.toString()
                    geoPoint.value = point
                    Log.d(getLogTag(), "got $result")
                }
                else{
                    unavailablePlace.value = false
                    unavailablePlace.value = true
                    Log.d(getLogTag(), "got $result")
                }
                loadingGeopoint.value  = false
        }
    }

    private fun clearPreviousJobs() {
        if (activeJob != null && activeJob!!.isActive) {
            activeJob!!.cancel()
            loadingSuggestions.value = false
            loadingGeopoint.value  = false
        }
    }

    fun loadGeopointFromText(location:String){
        clearPreviousJobs()
        activeJob = MainScope().launch {
            loadingGeopoint.value  = true
            val result = retrofit.getSuggestions(location)[0]
            geoPoint.value = result.geopoint
            loadingGeopoint.value  = false
        }
    }
}

interface SearchAPI{
    @GET("/search")
    suspend fun getSuggestions(@Query("q") search:String,
                       @Query("limit") limit:Int =10,
                       @Query("format") format:String = "json"): List<Suggestion>

    @GET("/reverse")
    suspend fun getPlaceFromGeoPoint(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("format") format:String = "json"): Suggestion?

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




