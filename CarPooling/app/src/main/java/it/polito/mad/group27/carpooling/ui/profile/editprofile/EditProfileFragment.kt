package it.polito.mad.group27.carpooling.ui.profile.editprofile

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.*
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import it.polito.mad.group27.carpooling.*
import it.polito.mad.group27.carpooling.ui.EditFragment

class EditProfileFragment : EditFragment(R.layout.edit_profile_fragment, R.menu.edit_menu,
    R.string.profile_edit_title) {

    private lateinit var imageButton: FloatingActionButton
    private lateinit var fullNameEdit: TextInputLayout
    private lateinit var nickNameEdit: TextInputLayout
    private lateinit var emailEdit: TextInputLayout
    private lateinit var locationEdit: TextInputLayout


    private lateinit var profileViewModel: EditProfileViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val profile = ViewModelProvider(act).get(ProfileViewModel::class.java).profile.value!!.copy()
        profileViewModel = ViewModelProvider(this).get(EditProfileViewModel::class.java)


        profileViewModel.profile = profile
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save_menu_button ->{
                saveProfile()
                findNavController().navigateUp()
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



        if(profileViewModel.profile.profileImageUri!=null) {
            setImage(profileViewModel.profile.profileImageUri, true)
        }
        fullNameEdit.editText!!.setText(profileViewModel.profile.fullName)
        nickNameEdit.editText!!.setText(profileViewModel.profile.nickName)
        emailEdit.editText!!.setText(profileViewModel.profile.email)
        locationEdit.editText!!.setText(profileViewModel.profile.location)


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

        profileViewModel.profile.fullName = fullNameEdit.editText!!.text.toString()
        profileViewModel.profile.nickName = nickNameEdit.editText!!.text.toString()
        profileViewModel.profile.email = emailEdit.editText!!.text.toString()
        profileViewModel.profile.location = locationEdit.editText!!.text.toString()

        val mainProfileViewModel =  ViewModelProvider(act).get(ProfileViewModel::class.java)
        mainProfileViewModel.profile.value = profileViewModel.profile
        saveImg("profile",  profileViewModel.profile.uid!!){ uri:String?, changed:Boolean ->
             if(changed) {
                 profileViewModel.profile.profileImageUri = uri
                 mainProfileViewModel.profile.value = profileViewModel.profile
             }
            profileViewModel.updateProfile()
        }

    }




}