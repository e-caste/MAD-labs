package it.polito.mad.group27.carpooling.ui.profile.editprofile

import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.*
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import it.polito.mad.group27.carpooling.*
import it.polito.mad.group27.carpooling.ui.BaseFragmentWithToolbar
import it.polito.mad.group27.carpooling.ui.EditFragment
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class EditProfileFragment : EditFragment(R.layout.edit_profile_fragment, R.menu.edit_menu,
    R.string.profile_edit_title) {
    private lateinit var viewModel: EditProfileViewModel

    private lateinit var imageButton: FloatingActionButton
    private lateinit var fullNameEdit: TextInputEditText
    private lateinit var nickNameEdit: TextInputEditText
    private lateinit var emailEdit: TextInputEditText
    private lateinit var locationEdit: TextInputEditText

    private lateinit var profileTmp: Profile


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(EditProfileViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save_menu_button ->{
                saveProfile()
                findNavController().navigate(R.id.action_editProfileFragment_to_showProfileFragment)
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.save_menu_button).isEnabled = validateFields()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageView = view.findViewById(R.id.imageProfileView)
        imageButton = view.findViewById(R.id.fab)
        fullNameEdit = view.findViewById(R.id.fullNameEdit)
        nickNameEdit = view.findViewById(R.id.nicknameEdit)
        emailEdit = view.findViewById(R.id.emailEdit)
        locationEdit = view.findViewById(R.id.locationEdit)

        profileTmp = act.profile.copy()
        image = act.profileImage

        if(image!=null)
            imageView.setImageBitmap(image)
        fullNameEdit.setText(profileTmp.fullName)
        nickNameEdit.setText(profileTmp.nickName)
        emailEdit.setText(profileTmp.email)
        locationEdit.setText(profileTmp.location)


        registerForContextMenu(imageButton)
        imageButton.setOnClickListener {
            Log.d(getLogTag(), "image button clicked")
            act.openContextMenu(imageButton)
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
                && Patterns.EMAIL_ADDRESS.matcher(emailEdit.text!!).matches()
                && locationEdit.text?.isNotEmpty() ?: false
    }

    private fun saveProfile() {
        saveImg(getString(R.string.profile_image))

        profileTmp.fullName = fullNameEdit.text.toString()
        profileTmp.nickName = nickNameEdit.text.toString()
        profileTmp.email = emailEdit.text.toString()
        profileTmp.location = locationEdit.text.toString()


        writeParcelable(profileTmp, getString(R.string.saved_profile_preference))
        act.loadProfile(profileTmp)
    }




}