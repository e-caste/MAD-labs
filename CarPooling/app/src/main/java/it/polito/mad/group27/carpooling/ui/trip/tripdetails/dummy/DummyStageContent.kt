package it.polito.mad.group27.carpooling.ui.trip.tripdetails.dummy
import it.polito.mad.group27.carpooling.R
import java.util.ArrayList

//TODO: Replace all uses of this class before publishing your app.
object DummyStageContent {

    val ITEMS: MutableList<DummyStage> = ArrayList()

    private const val COUNT = 10
    private val stageNameTexts = listOf("Torino Centro", "Milano Porta Garibaldi", "Ancona",
            "La Spezia Manarola", "New New York", "Roma Stazione Termini",
            "Aeroporto Milano Malpensa", "Canicattì", "Museo Oceanografico di San Benedetto del Tronto")
    private val stageTimeTexts = listOf("12:30", "2 P.M.", "23:42", "00:00", "9:41", "11:25", "22:09", "1:58", "23:46", "3:33")

    init {
        for (i in 1..COUNT) {
            addItem(createDummyItem(i))
        }
    }

    private fun addItem(item: DummyStage) {
        ITEMS.add(item)
    }

    private fun createDummyItem(position: Int): DummyStage {
        return DummyStage(
                stageTimeTexts[(stageTimeTexts.indices).random()],
                stageNameTexts[(stageNameTexts.indices).random()],
        )
    }

    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0 until position) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }

    data class DummyStage(val stageTime: String, val stageName: String)

}