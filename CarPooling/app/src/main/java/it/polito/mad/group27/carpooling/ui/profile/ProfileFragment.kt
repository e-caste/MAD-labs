package it.polito.mad.group27.carpooling.ui.profile

import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.fragment.app.Fragment
import it.polito.mad.group27.carpooling.Profile
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.getLogTag
import it.polito.mad.group27.carpooling.ui.BaseFragmentWithToolbar
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

open class ProfileFragment(resId:Int, optionsMenuId: Int, titleId: Int?):
    BaseFragmentWithToolbar(resId, optionsMenuId, titleId){

    fun getProfile(): Profile? {
        var profile: Profile? = null
        // if something in storage -> set it
        val savedProfileJson = activity?.getPreferences(MODE_PRIVATE)
            ?.getString(getString(R.string.saved_profile_preference), null)
        if (savedProfileJson != null) try {
            profile = Json.decodeFromString(savedProfileJson)
        } catch (e: SerializationException) {
            Log.d(getLogTag(), "Cannot parse saved preference profile")
        }
        return profile
    }
}