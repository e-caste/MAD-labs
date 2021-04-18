package it.polito.mad.group27.carpooling.ui.trip.tripedit

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.EditText
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.ui.trip.Date
import it.polito.mad.group27.carpooling.ui.trip.Trip
import java.time.LocalDateTime
import java.util.Locale

class TripEditFragment : Fragment(R.layout.trip_edit_fragment) {

    private lateinit var viewModel: TripEditViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TripEditViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val date = view.findViewById<EditText>(R.id.editDateText)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            date.setText(
                arguments?.getParcelable<Trip>("trip")?.date?.toString(Locale.getDefault())
                ?: Date(
                    LocalDateTime.now().dayOfMonth ,
                    LocalDateTime.now().monthValue,
                    LocalDateTime.now().year)
                        .toString(Locale.getDefault()))
        }
        else{
            date.setText(
                arguments?.getParcelable<Trip>("trip")?.date?.toString(Locale.getDefault())
                    ?:
                    Date(java.util.Calendar.getInstance()).toString(Locale.getDefault())
            )
        }

    }

}