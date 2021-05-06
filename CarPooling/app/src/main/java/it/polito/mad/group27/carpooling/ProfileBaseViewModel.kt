package it.polito.mad.group27.carpooling

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

open class ProfileBaseViewModel(application: Application) : AndroidViewModel(application)  {
    val profile: MutableLiveData<Profile?> = MutableLiveData(null)



}