package it.polito.mad.group27.carpooling.ui.trip.tripfilter

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.RangeSlider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import it.polito.mad.group27.carpooling.R
import it.polito.mad.group27.carpooling.TripFilter
import it.polito.mad.group27.carpooling.ui.BaseFragmentWithToolbar

class TripFilterFragment : BaseFragmentWithToolbar(R.layout.trip_filter_fragment, R.menu.edit_menu, R.string.trip_edit_title) {


    private lateinit var viewModel: TripFilterViewModel

    private lateinit var toInput:TextInputLayout
    private lateinit var fromInput:TextInputLayout
    private lateinit var startDayInput:TextInputLayout
    private lateinit var startHourInput:TextInputLayout

    private lateinit var priceRange:RangeSlider

    private lateinit var minPriceText:TextView
    private lateinit var maxPriceText:TextView

    private lateinit var optionsChip:ChipGroup

    private lateinit var fab:ExtendedFloatingActionButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(TripFilterViewModel::class.java)
        // TODO: Use the ViewModel

        viewModel.tripFilter = arguments?.getParcelable("filter") ?: TripFilter()

        toInput = view.findViewById(R.id.to_place)
        fromInput = view.findViewById(R.id.from_place)

        startDayInput = view.findViewById(R.id.start_date)
        startHourInput = view.findViewById(R.id.start_hour)

        priceRange = view.findViewById(R.id.price_range)

        minPriceText = view.findViewById(R.id.min_price)
        minPriceText.text=formatCurrency(viewModel.tripFilter.priceMin.toFloat())

        maxPriceText = view.findViewById(R.id.max_price)
        maxPriceText.text=formatCurrency(viewModel.tripFilter.priceMax.toFloat())

        optionsChip = view.findViewById(R.id.options_chip)

        priceRange.values = listOf(viewModel.tripFilter.priceMin.toFloat(),
            viewModel.tripFilter.priceMax.toFloat())

        priceRange.addOnChangeListener{ range, _, _ ->
            val values = range.values
            minPriceText.text = formatCurrency(values[0])
            maxPriceText.text = formatCurrency(values[1])
        }

        fab = view.findViewById(R.id.fab)
        fab.setOnClickListener{
            Snackbar.make(view,"IMPLEMENT ME", Snackbar.LENGTH_LONG).show()
        }


    }

    private fun formatCurrency(value: Float) =
        String.format("%.0f €", value)

}