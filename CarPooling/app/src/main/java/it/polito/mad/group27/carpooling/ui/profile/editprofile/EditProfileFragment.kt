package it.polito.mad.group27.carpooling.ui.profile.editprofile

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

class EditProfileFragment : EditFragment(R.layout.edit_profile_fragment, R.menu.edit_menu,
    R.string.profile_edit_title) {
    private lateinit var viewModel: EditProfileViewModel

    private lateinit var imageButton: FloatingActionButton
    private lateinit var fullNameEdit: TextInputEditText
    private lateinit var nickNameEdit: TextInputEditText
    private lateinit var emailEdit: TextInputEditText
    private lateinit var locationEdit: TextInputEditText

    private lateinit var profileImage:Bitmap
    private lateinit var profileTmp: Profile


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(EditProfileViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save_menu_button ->{
                //TODO save
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
        profileImage = act.profileImage

        imageView.setImageBitmap(profileImage)
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


    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = act.menuInflater
        inflater.inflate(R.menu.select_image_source_menu, menu)
        if (profileImage != null) {
            var deleteItem = menu?.findItem(R.id.delete)
            if (deleteItem != null) {
                deleteItem.isVisible = true
            }
        }
        Log.d(getLogTag(), "context menu created")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.camera -> {
                Log.d(getLogTag(), "taking picture...")
                checkCameraPermissionAndTakePhoto()
                return true
            }
            R.id.gallery -> {
                Log.d(getLogTag(), "choosing picture from gallery...")
                checkStoragePermissionAndGetPhoto()
                return true
            }
            R.id.delete -> {
                Log.d(getLogTag(), "deleting picture...")
                imageView.setImageResource(R.drawable.ic_baseline_person_24)
                image = null
                imageChanged = true
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



}