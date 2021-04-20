package it.polito.mad.group27.carpooling.ui.trip.tripedit

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.commit
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.getLogTag
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

        val trip = arguments?.getParcelable<Trip>("trip") ?: Trip()

        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraintsBuilder.build())
                .build()
        datePicker.addOnPositiveButtonClickListener {
            Log.d(getLogTag(), "positive")
            Log.d(getLogTag(), "${datePicker.selection.toString()}")

        }
        datePicker.addOnNegativeButtonClickListener {
            Log.d(getLogTag(), "negative")
        }
        datePicker.addOnCancelListener {
            Log.d(getLogTag(), "cancel")
        }
        datePicker.addOnDismissListener {
            Log.d(getLogTag(), "dismiss")
        }

        val date = view.findViewById<TextView>(R.id.editDateText)
        date.text = trip.date.toString(Locale.getDefault())
        date.setOnClickListener {
            if(!datePicker.isVisible)
                datePicker.show(requireActivity().supportFragmentManager, "tag")
        }

        val from = PlaceHourFragment("From")
        activity?.supportFragmentManager?.commit {
            add(R.id.editFrom, from)
        }

        val to = PlaceHourFragment("To")
        activity?.supportFragmentManager?.commit {
            add(R.id.editTo, to)
        }

    }

}