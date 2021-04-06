package it.polito.mad.group27.carpooling

import android.text.Editable
import android.text.TextWatcher

class Watcher(val predicate : () -> Boolean, val actionTrue : () -> Unit, val actionFalse : () -> Unit) :
    TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if(predicate.invoke()) actionTrue()
        else actionFalse()
    }
}