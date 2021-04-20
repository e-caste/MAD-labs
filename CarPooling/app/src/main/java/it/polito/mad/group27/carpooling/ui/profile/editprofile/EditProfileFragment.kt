package it.polito.mad.group27.carpooling.ui.profile.editprofile

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import it.polito.mad.group27.carpooling.*

import it.polito.mad.group27.carpooling.ui.profile.ProfileFragment

class EditProfileFragment : ProfileFragment(R.layout.edit_profile_fragment, R.menu.edit_profile_menu) {

    private lateinit var viewModel: EditProfileViewModel

    private lateinit var imageProfileView: ImageView
    private lateinit var imageButton: FloatingActionButton
    private lateinit var fullNameEdit: TextInputEditText
    private lateinit var nickNameEdit: TextInputEditText
    private lateinit var emailEdit: TextInputEditText
    private lateinit var locationEdit: TextInputEditText


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(EditProfileViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save_profile ->{
                //TODO save
                findNavController().navigate(R.id.action_editProfileFragment_to_showProfileFragment)
            }
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu!!.findItem(R.id.save_profile).isEnabled = validateFields()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageProfileView = view.findViewById(R.id.imageProfileView)
        imageButton = view.findViewById(R.id.fab)
        fullNameEdit = view.findViewById(R.id.fullNameEdit)
        nickNameEdit = view.findViewById(R.id.nicknameEdit)
        emailEdit = view.findViewById(R.id.emailEdit)
        locationEdit = view.findViewById(R.id.locationEdit)

        registerForContextMenu(imageButton)
        imageButton.setOnClickListener {
            Log.d(getLogTag(), "image button clicked")
//            openContextMenu(imageButton)
        }

        fullNameEdit.addTextChangedListener(Watcher(
            { fullNameEdit.text?.isEmpty() ?: false  || fullNameEdit.text?.trim()?.split("\\s+".toRegex())?.size ?: 0 < 2 },
            { fullNameEdit.error = getString(R.string.validation_fullname)
                activity?.invalidateOptionsMenu()
            },
            { fullNameEdit.error = null
                activity?.invalidateOptionsMenu()
            }
        ))

        nickNameEdit.addTextChangedListener(Watcher(
            { nickNameEdit.text?.length ?:0  < 4 },
            { nickNameEdit.error = getString(R.string.validation_nickname)
                activity?.invalidateOptionsMenu()
            },
            { nickNameEdit.error = null
                activity?.invalidateOptionsMenu()
            }
        ))

        emailEdit.addTextChangedListener(Watcher(
            { emailEdit.text?.isEmpty() ?: false || !Patterns.EMAIL_ADDRESS.matcher(emailEdit.text).matches() },
            { emailEdit.error = getString(R.string.validation_email)
                activity?.invalidateOptionsMenu()
            },
            { emailEdit.error = null
                activity?.invalidateOptionsMenu()
            }
        ))

        locationEdit.addTextChangedListener(Watcher(
            //TODO check location format
            { locationEdit.text?.isEmpty() ?: false },
            { locationEdit.error = getString(R.string.validation_location)
                activity?.invalidateOptionsMenu()
            },
            { locationEdit.error = null
                activity?.invalidateOptionsMenu()
            }
        ))
    }

    private fun validateFields() : Boolean {
        return fullNameEdit.text?.isNotEmpty() ?: false
                && fullNameEdit.text?.trim()?.split("\\s+".toRegex())?.size ?: 0 >= 2
                && nickNameEdit.text?.length ?: 0 >= 4
                && emailEdit.text?.isNotEmpty() ?: false
                && android.util.Patterns.EMAIL_ADDRESS.matcher(emailEdit.text!!).matches()
                && locationEdit.text?.isNotEmpty() ?: false
    }


}