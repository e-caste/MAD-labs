package it.polito.mad.group27.hubert.ui.trip.tripfilter

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.slider.RangeSlider
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import it.polito.mad.group27.hubert.R
import it.polito.mad.group27.hubert.entities.TripFilter
import it.polito.mad.group27.hubert.Watcher
import it.polito.mad.group27.hubert.getLogTag
import it.polito.mad.group27.hubert.ui.BaseFragmentWithToolbar
import it.polito.mad.group27.hubert.entities.Hour
import it.polito.mad.group27.hubert.entities.Option
import it.polito.mad.group27.hubert.ui.trip.tripedit.clearHour
import it.polito.mad.group27.hubert.ui.trip.tripedit.getDatePicker
import it.polito.mad.group27.hubert.ui.trip.tripedit.getTimePicker
import it.polito.mad.group27.hubert.ui.trip.tripedit.updateTime
import java.math.BigDecimal
import java.text.DateFormat
import java.util.*

class TripFilterFragment : BaseFragmentWithToolbar(
    R.layout.trip_filter_fragment,
    R.menu.base_trip_list_menu,
    R.string.trip_edit_title
) {
    private lateinit var viewModel: TripFilterViewModel

    private lateinit var toInput: TextInputLayout
    private lateinit var fromInput: TextInputLayout
    private lateinit var startDayInput: TextInputLayout
    private lateinit var startHourInput: TextInputLayout

    private lateinit var priceRange: RangeSlider

    private lateinit var minPriceText: TextView
    private lateinit var maxPriceText: TextView

    private lateinit var optionsChip: ChipGroup

    private lateinit var fab: ExtendedFloatingActionButton

    private var timePicker: MaterialTimePicker? = null

    private val df: DateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ITALY)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(TripFilterViewModel::class.java)
        if(arguments?.getParcelable<TripFilter>("filter")==null)
            Log.d(getLogTag(), "No filter, applying default")
        viewModel.tripFilter = arguments?.getParcelable("filter") ?: TripFilter()

        toInput = view.findViewById(R.id.to_place)
        toInput.editText?.setText(viewModel.tripFilter.to)
        fromInput = view.findViewById(R.id.from_place)
        fromInput.editText?.setText(viewModel.tripFilter.from)


        startDayInput = view.findViewById(R.id.start_date)
        val datePicker =
            getDatePicker(viewModel.tripFilter.dateTime, startDayInput) {
                viewModel.tripFilter.dateTime = Calendar.getInstance()
                viewModel.tripFilter.dateTime!!.clearHour()
                viewModel.tripFilter.dateTime!!
            }
        startDayInput.editText?.setOnClickListener {
            if (!datePicker.isVisible)
                datePicker.show(requireActivity().supportFragmentManager, "datePickerTag")
        }
        startDayInput.editText?.addTextChangedListener(Watcher(
            { true },
            {startDayInput.error = null },
            {}
        ))

        startHourInput = view.findViewById(R.id.start_hour)
        startHourInput.editText?.setOnClickListener {
            if (timePicker == null || !timePicker!!.isVisible) {
                timePicker = getTimePicker(
                    startHourInput.editText!!,
                    viewModel.tripFilter.dateTime ?: Calendar.getInstance(),
                    this.requireContext()
                ) {
                    if (viewModel.tripFilter.dateTime == null)
                        viewModel.tripFilter.dateTime = Calendar.getInstance()
                    viewModel.tripFilter.dateTime!!.updateTime(it)
                }
                timePicker!!.show(requireActivity().supportFragmentManager, "timePickerTag")
            }
        }

        if(viewModel.tripFilter.dateTime!=null) {
            startDayInput.editText?.setText(df.format(viewModel.tripFilter.dateTime!!.time))
            startHourInput.editText?.setText(Hour(viewModel.tripFilter.dateTime!!).toString())
        }
        startHourInput.setEndIconOnClickListener {
            viewModel.tripFilter.dateTime?.clearHour()
            startHourInput.editText?.text =null
            startDayInput.error = null
        }

        startDayInput.setEndIconOnClickListener {
            viewModel.tripFilter.dateTime = null
            startDayInput.editText?.text  = null
        }

        priceRange = view.findViewById(R.id.price_range)

        minPriceText = view.findViewById(R.id.min_price)
        minPriceText.text = formatCurrency(viewModel.tripFilter.priceMin.toFloat())

        maxPriceText = view.findViewById(R.id.max_price)
        maxPriceText.text = formatCurrency(viewModel.tripFilter.priceMax.toFloat())

        optionsChip = view.findViewById(R.id.options_chip)
        optionsChip.children.forEach {
            val tag = Option.valueOf((it.tag as String).toUpperCase())
            if(viewModel.tripFilter.options[tag] == true)
                (it as Chip).isChecked = true
        }

        priceRange.values = listOf(
            viewModel.tripFilter.priceMin.toFloat(),
            viewModel.tripFilter.priceMax.toFloat()
        )

        priceRange.addOnChangeListener { range, _, _ ->
            val values = range.values
            minPriceText.text = formatCurrency(values[0])
            maxPriceText.text = formatCurrency(values[1])
        }

        fab = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            viewModel.tripFilter.from = extractText(fromInput)
            viewModel.tripFilter.to = extractText(toInput)

            viewModel.tripFilter.priceMin = if(minPriceText.text== "0 €") BigDecimal("0.00")
                        else BigDecimal(minPriceText.text.split(" ")[0])
            viewModel.tripFilter.priceMax = if(maxPriceText.text== "500 €") BigDecimal("500.00")
                        else BigDecimal(maxPriceText.text.split(" ")[0])


            viewModel.tripFilter.options = optionsChip.children
                .map { Option.valueOf((it.tag as String).toUpperCase()) to (it as Chip).isChecked }
                .toMap().toMutableMap()

            if(timePicker!=null)
                viewModel.tripFilter.dateTime?.updateTime(timePicker!!)

            if (isValidTimeSelection()) {
                startDayInput.error = getString(R.string.date_and_time_both_present)
            } else if(viewModel.tripFilter.dateTime!= null &&
                viewModel.tripFilter.dateTime!! < Calendar.getInstance() &&
                (startHourInput.editText?.text?.isNotBlank() == true)) {
                startDayInput.error = getString(R.string.date_not_in_past)
            }else if (viewModel.tripFilter.from!=null
                && viewModel.tripFilter.from == viewModel.tripFilter.to ){
                toInput.error = getString(R.string.error_equal_destination_departure)
                Log.d(getLogTag(), "From is ${viewModel.tripFilter.from}")
            }else {
                findNavController().navigate(R.id.action_tripFilterFragment_to_othersTripList,
                    bundleOf("filter" to viewModel.tripFilter)
                )
            }
        }


    }

    private fun isValidTimeSelection() =
        (startDayInput.editText?.text ?: "").isBlank() &&
                (startHourInput.editText?.text ?: "").trim().isNotBlank()

    private fun extractText(input: TextInputLayout) =
        if ((input.editText?.text?.toString() ?: "").trim() == "") null else input.editText?.text.toString()

    private fun formatCurrency(value: Float) =
        String.format("%.0f €", value)

}