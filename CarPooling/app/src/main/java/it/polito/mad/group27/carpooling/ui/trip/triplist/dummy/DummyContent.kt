package it.polito.mad.group27.carpooling.ui.trip.triplist.dummy

import it.polito.mad.group27.carpooling.R
import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    val ITEMS: MutableList<DummyItem> = ArrayList()

    /**
     * A map of sample (dummy) items, by ID.
     */
//    val ITEM_MAP: MutableMap<String, DummyItem> = HashMap()

    private val COUNT = 10

    private val carImages = listOf(R.drawable.audi_a6, R.drawable.ford_fiesta, R.drawable.tesla_cybertruck)
    private val priceTexts = listOf("9.99 €", "$ 32.48", "100000000 €", "32.75 €")
    private val departureTexts = listOf("Torino Centro", "Milano Porta Garibaldi", "Ancona", "La Spezia Manarola", "New New York")
    private val destinationTexts = departureTexts
    private val hourDepartureTexts = listOf("12:30", "2 P.M.", "23:42", "00:00", "9:41")
    private val dateDepartureTexts = listOf("17/04/2021", "14/03/1729", "02/02/2020")

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createDummyItem(i))
        }
    }

    private fun addItem(item: DummyItem) {
        ITEMS.add(item)
//        ITEM_MAP.put(item.id, item)
    }

    private fun createDummyItem(position: Int): DummyItem {
        return DummyItem(
            carImages[(carImages.indices).random()],
            priceTexts[(priceTexts.indices).random()],
            departureTexts[(departureTexts.indices).random()],
            destinationTexts[(destinationTexts.indices).random()],
            hourDepartureTexts[(hourDepartureTexts.indices).random()],
            dateDepartureTexts[(dateDepartureTexts.indices).random()],
        )
    }

    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0..position - 1) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }

    /**
     * A dummy item representing a piece of content.
     */
    data class DummyItem(
        val carImage: Int,
        val priceText: String,
        val departureText: String,
        val destinationText: String,
        val hourDepartureText: String,
        val dateDepartureText: String,
        ) {
//        override fun toString(): String = content
    }
}