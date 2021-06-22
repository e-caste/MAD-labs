package it.polito.mad.group27.hubert

import android.content.Context
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import it.polito.mad.group27.hubert.ui.BaseFragmentWithToolbar
import java.util.*

const val TAG = "MAD-group-27"

fun getLogTag() = TAG

fun timestampToCalendar(dateTime: Timestamp): Calendar =
    Calendar.getInstance().also { it.timeInMillis = dateTime.seconds * 1000 }

fun calendarToTimestamp(dateTime: Calendar): Timestamp =
    Timestamp(dateTime.time)

enum class Size {
    SMALL,
    HUGE
}

fun Fragment.loadImage(uri:String, view: ImageView, dimension:Size, circularCrop:Boolean = false){
    val circularProgressDrawable = CircularProgressDrawable(requireContext())
    //TODO set dimensions
    circularProgressDrawable.strokeWidth = dpTopixel(requireContext(), 5f)
    circularProgressDrawable.centerRadius = dpTopixel(requireContext(), 50f)
    circularProgressDrawable.start()

    Glide.with(this).load(uri)
        .apply { if(circularCrop) circleCrop() }
        .placeholder(circularProgressDrawable)
        .into(view)
}

fun dpTopixel(c: Context, dp: Float): Float {
    val density = c.resources.displayMetrics.density
    return dp * density
}

fun BaseFragmentWithToolbar.showNotificationFeedback(result: Boolean, successMessage :Int, errorMessage:Int){
    val fragment = getVisibleFragment()
    if (fragment!=null) {
        Snackbar.make(
            fragment.requireView(),
            fragment.requireContext().getString(if (result) successMessage else errorMessage),
            Snackbar.LENGTH_LONG
        ).also {
            if (!result)
                it.setTextColor(ResourcesCompat.getColor(resources, R.color.colorAccent, null))
        }.show()
    }
}

private fun BaseFragmentWithToolbar.getVisibleFragment(): Fragment? {
    val fragmentManager: FragmentManager = act.supportFragmentManager
    val fragments: List<Fragment> = fragmentManager.fragments
    for (fragment in fragments) {
        if (fragment.isVisible) return fragment
    }
    return null
}
