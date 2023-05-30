package com.example.polito_mad_01.ui

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.*
import android.os.*
import android.provider.MediaStore
import android.text.*
import android.view.*
import android.widget.*
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.net.toUri
import androidx.fragment.app.*
import androidx.navigation.fragment.findNavController
import com.example.polito_mad_01.*
import com.example.polito_mad_01.model.User
import com.example.polito_mad_01.util.UIUtils
import com.example.polito_mad_01.viewmodel.*
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import io.getstream.avatarview.AvatarView
import io.getstream.avatarview.coil.loadImage
import okhttp3.internal.indexOf
import java.net.URI
import java.util.*


class EditProfile(val vm: EditProfileViewModel) : Fragment(R.layout.fragment_edit_profile) {

    private val RESULT_LOAD_IMAGE = 123
    private val IMAGE_CAPTURE_CODE = 654
    private val PERMISSION_REQUEST_CODE = 200
    private  var imageUriForCamera : Uri? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher
            .addCallback(this) {
                showExitDialog()
            }
            .isEnabled = true

        val imgButton = view.findViewById<ImageButton>(R.id.imageButton)
        registerForContextMenu(imgButton)
        imgButton.setOnClickListener { v -> v.showContextMenu() }

        if(savedInstanceState == null )
            setAllView(view)

    }


    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        requireActivity().menuInflater.inflate(R.menu.menu_picture, menu)
        super.onCreateContextMenu(menu, v, menuInfo)
    }


    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        imageUriForCamera =
            activity?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriForCamera)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(resultCode == AppCompatActivity.RESULT_OK) {
            if (requestCode == IMAGE_CAPTURE_CODE
                && imageUriForCamera != null  && imageUriForCamera != Uri.EMPTY ) {
                vm.user.value?.image_uri = imageUriForCamera.toString()
                vm.updateUserImage(imageUriForCamera!!)
            }
            else if (requestCode == RESULT_LOAD_IMAGE){
                if( data?.data != null) {
                    vm.user.value?.image_uri = data.data.toString()
                    vm.updateUserImage(data.data!!)
                }
            }

        }

    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.gallery -> gallery()
            R.id.picture -> picture()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun gallery(): Boolean {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE)
        return true
    }


    private fun picture(): Boolean {
        if (checkPermission()) openCamera()
        else showPermissionReasonAndRequest()
        return true
    }

    private fun checkPermission(): Boolean {
        if (activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) }
            != PackageManager.PERMISSION_GRANTED
        ) return false
        return true
    }


    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            PERMISSION_REQUEST_CODE
        )
    }


    private fun showPermissionReasonAndRequest() {
        AlertDialog.Builder(activity)
            .setTitle("Notice")
            .setMessage(R.string.cameraPermission.toString())
            .setPositiveButton("OK") { _, _ -> requestPermission() }
    }

    private fun showExitDialog(): Boolean {
        AlertDialog.Builder(activity)
            .setTitle("Are you sure?").setMessage("All changes will be lost")
            .setPositiveButton("YES")
            { _, _ -> findNavController().navigate(R.id.action_editProfileContainer_to_profileFragment) }
            .setNegativeButton("NO") { _, _ -> }.show()
        return true
    }

    private fun setValue(attribute: String, newValue: String) {
        when (attribute) {
            "name" -> vm.user.value?.name = newValue
            "surname" -> vm.user.value?.surname = newValue
            "nickname" -> vm.user.value?.nickname = newValue
            "description" -> vm.user.value?.achievements = listOf(newValue)
            "email" -> vm.user.value?.email = newValue
            "phoneNumber" -> vm.user.value?.phoneNumber = newValue
            "location" -> vm.user.value?.location = newValue
            "gender" -> vm.user.value?.gender = newValue
            "birthdate" -> vm.user.value?.birthdate = newValue
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setAllView(view: View) {

        vm.getUser().observe(viewLifecycleOwner) { user ->
            setTextViews(view, user)
            setButtons(user)
            setSpinners()
        }

        vm.getUserImage().observe(viewLifecycleOwner){image ->
            setImage(image)
        }

    }

    private fun setButtons(user: User) {
        setButtonAndListener(R.id.mondayButton, user.availability["monday"]!!, "monday")
        setButtonAndListener(R.id.tuesdayButton, user.availability["tuesday"]!!, "tuesday")
        setButtonAndListener(R.id.wednesdayButton, user.availability["wednesday"]!!, "wednesday")
        setButtonAndListener(R.id.thursdayButton, user.availability["thursday"]!!, "thursday")
        setButtonAndListener(R.id.fridayButton, user.availability["friday"]!!, "friday")
        setButtonAndListener(R.id.saturdayButton, user.availability["saturday"]!!, "saturday")
        setButtonAndListener(R.id.sundayButton, user.availability["sunday"]!!, "sunday")
    }

    private fun setButtonAndListener(id: Int, value: Boolean, attribute: String) {
        val button = requireView().findViewById<Button>(id)
        setButtonColor(value, button)
        button.setOnClickListener {
            val newValue = !getAvailability(attribute)
            setAvailability(attribute, newValue)
            setButtonColor(newValue, button)
        }
    }

    private fun setButtonColor(value: Boolean, button: Button) {
        val colorTrue = getColor(requireContext(), R.color.powder_blue)
        val colorFalse = getColor(requireContext(), R.color.gray)

        if (value) button.setBackgroundColor(colorTrue)
        else button.setBackgroundColor(colorFalse)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setTextViews(view: View, user: User) {

        setEditTextViewAndListener(R.id.name, user.name, "name")
        setEditTextViewAndListener(R.id.surname, user.surname, "surname")
        setEditTextViewAndListener(R.id.nickName_value, user.nickname, "nickname")
        setEditTextViewAndListener(R.id.achievements_value, user.achievements.toString(), "description")
        setEditTextViewAndListener(R.id.mail_value, user.email, "email")
        setEditTextViewAndListener(R.id.phoneNumber_value, user.phoneNumber, "phoneNumber")
        setEditTextViewAndListener(R.id.location_value, user.location, "location")
        setGenderView(view, user)
        setBirthdateView(view, user)
    }

    private fun setBirthdateView(view: View, user: User) {
        val birthdateView = UIUtils.findTextInputById(view,R.id.birthday)
        birthdateView?.editText?.setText(user.birthdate)

        val materialDatePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a Date").build()
        materialDatePicker.addOnPositiveButtonClickListener {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
            birthdateView?.editText?.setText(date)
            setValue("birthdate", date)
        }

        birthdateView?.editText?.setOnClickListener {
            materialDatePicker.show(childFragmentManager, "DATE_PICKER")
        }

    }

    private fun setGenderView(view: View, user: User){
        view.findViewById<AutoCompleteTextView>(R.id.genderSelector)?.let {
            it.setText(user.gender)
            it.setOnItemClickListener {  parent, _, position, _ ->
                setValue("gender", parent.getItemAtPosition(position).toString())
            }
        }
    }

    private fun setSpinners() {
        val textField = UIUtils.findTextInputById(view,R.id.gender)
        val genderArray = resources.getStringArray(R.array.genderArray)
        val adapter = ArrayAdapter(requireContext(), R.layout.gender_list_item, genderArray)
        (textField?.editText as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setAvailability(attribute: String, checked: Boolean) {
        vm.user.value?.availability?.put(attribute, checked)
    }

    private fun getAvailability(attribute: String): Boolean {
        return vm.user.value?.availability?.get(attribute)!!
    }

    private fun setEditTextViewAndListener(id: Int, field: String?, attribute: String) {
        val textName = requireView().findViewById<TextInputLayout>(id)
        textName.editText?.setText(field)
        textName.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) =
                setValue(attribute, s.toString())
        })
    }
    private fun setImage(image: URI?) {

        println("IMAGE: $image")

        val frame = view?.findViewById<ImageView>(R.id.profileImage_imageView)!!
        image?.let {
            val imageUri = Uri.parse(image.toString())
            if (imageUri != Uri.EMPTY && imageUri != null) {
                frame.setImageURI(imageUri)
            }
        }
    }
}