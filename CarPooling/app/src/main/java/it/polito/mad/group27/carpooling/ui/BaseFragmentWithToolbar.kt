package it.polito.mad.group27.carpooling.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import it.polito.mad.group27.carpooling.MainActivity
import it.polito.mad.group27.carpooling.R


open class BaseFragmentWithToolbar(layoutId: Int,
                                   private val optionsMenuId: Int,
                                   private val titleId: Int?): Fragment(layoutId) {
    protected lateinit var act :MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val inflater: MenuInflater = inflater
        inflater.inflate(optionsMenuId, menu)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        act = activity as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)


        act.setSupportActionBar(toolbar)
        act.setupActionBarWithNavController(findNavController(), act.appBarConfiguration)
        act.supportActionBar?.title =  if (titleId!=null) getString(titleId) else null
    }

    fun updateTitle(title: String){
        act.supportActionBar?.title = title
    }

}