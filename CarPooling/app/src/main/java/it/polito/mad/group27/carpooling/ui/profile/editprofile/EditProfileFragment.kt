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
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import it.polito.mad.group27.carpooling.*
import it.polito.mad.group27.carpooling.ui.BaseFragmentWithToolbar
import it.polito.mad.group27.carpooling.ui.EditFragment
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class EditProfileFragment : EditFragment(R.layout.edit_profile_fragment, R.menu.edit_menu,
    R.string.profile_edit_title) {

    private lateinit var imageButton: FloatingActionButton
    private lateinit var fullNameEdit: TextInputLayout
    private lateinit var nickNameEdit: TextInputLayout
    private lateinit var emailEdit: TextInputLayout
    private lateinit var locationEdit: TextInputLayout

    private lateinit var profileTmp: Profile

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        profileViewModel = ViewModelProvider(act).get(ProfileViewModel::class.java)
        profileTmp = profileViewModel.profile.value?.copy() ?: Profile()
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



        if(profileTmp.profileImageUri!=null)
            Glide.with(this).load(profileTmp.profileImageUri).into(imageView);
        fullNameEdit.editText!!.setText(profileTmp.fullName)
        nickNameEdit.editText!!.setText(profileTmp.nickName)
        emailEdit.editText!!.setText(profileTmp.email)
        locationEdit.editText!!.setText(profileTmp.location)


        registerForContextMenu(imageButton)
        imageButton.setOnClickListener {
            Log.d(getLogTag(), "image button clicked")
            act.openContextMenu(imageButton)
        }

        fullNameEdit.editText!!.addTextChangedListener(Watcher(
            { fullNameEdit.editText!!.text?.isEmpty() ?: true  || fullNameEdit.editText!!.text?.trim()?.split("\\s+".toRegex())?.size ?: 0 < 2 },
            { fullNameEdit.error = getString(R.string.validation_fullname)
                activity?.invalidateOptionsMenu()
            },
            { fullNameEdit.error = null
                activity?.invalidateOptionsMenu()
            }
        ))

        nickNameEdit.editText!!.addTextChangedListener(Watcher(
            { nickNameEdit.editText!!.text?.length ?:0  < 4 },
            { nickNameEdit.error = getString(R.string.validation_nickname)
                activity?.invalidateOptionsMenu()
            },
            { nickNameEdit.error = null
                activity?.invalidateOptionsMenu()
            }
        ))

        emailEdit.editText!!.addTextChangedListener(Watcher(
            { emailEdit.editText!!.text?.isEmpty() ?: true || !Patterns.EMAIL_ADDRESS.matcher(emailEdit.editText!!.text).matches() },
            { emailEdit.error = getString(R.string.validation_email)
                activity?.invalidateOptionsMenu()
            },
            { emailEdit.error = null
                activity?.invalidateOptionsMenu()
            }
        ))

        locationEdit.editText!!.addTextChangedListener(Watcher(
            //TODO check location format
            { locationEdit.editText!!.text?.isEmpty() ?: true },
            { locationEdit.error = getString(R.string.validation_location)
                activity?.invalidateOptionsMenu()
            },
            { locationEdit.error = null
                activity?.invalidateOptionsMenu()
            }
        ))
    }

    private fun validateFields() : Boolean {
        return fullNameEdit.editText!!.text?.isNotEmpty() ?: false
                && fullNameEdit.editText!!.text?.trim()?.split("\\s+".toRegex())?.size ?: 0 >= 2
                && nickNameEdit.editText!!.text?.length ?: 0 >= 4
                && emailEdit.editText!!.text?.isNotEmpty() ?: false
                && Patterns.EMAIL_ADDRESS.matcher(emailEdit.editText!!.text!!).matches()
                && locationEdit.editText!!.text?.isNotEmpty() ?: false
    }

    private fun saveProfile() {

        profileTmp.fullName = fullNameEdit.editText!!.text.toString()
        profileTmp.nickName = nickNameEdit.editText!!.text.toString()
        profileTmp.email = emailEdit.editText!!.text.toString()
        profileTmp.location = locationEdit.editText!!.text.toString()


        profileViewModel.updateProfile(profileTmp)
    }




}