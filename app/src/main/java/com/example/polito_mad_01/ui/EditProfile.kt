package com.example.polito_mad_01.ui

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.ParseException
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.*
import android.view.*
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.*
import androidx.navigation.fragment.findNavController
import com.example.polito_mad_01.*
import com.example.polito_mad_01.db.User
import com.example.polito_mad_01.viewmodel.*
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class EditProfile : Fragment(R.layout.fragment_edit_profile) {

    private val vm: EditProfileViewModel by viewModels {
        EditProfileViewModelFactory((activity?.application as SportTimeApplication).userRepository)
    }

    private var imageUri: Uri? = null
    private val RESULT_LOAD_IMAGE = 123
    private val IMAGE_CAPTURE_CODE = 654
    private val PERMISSION_REQUEST_CODE = 200

    private var imageUriString: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher
            .addCallback(this) { showExitDialog() }
            .isEnabled = true

        val imgButton = view.findViewById<ImageButton>(R.id.imageButton)
        registerForContextMenu(imgButton)
        imgButton.setOnClickListener { v -> v.showContextMenu() }

        setHasOptionsMenu(true)
        setAllView()
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_edit_profile, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId ==   R.id.action_save_profile)
            return trySaveData()
        return super.onOptionsItemSelected(item)
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
        imageUri = activity?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        imageUriString = imageUri.toString()
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        view?.let {
            val frame = it.findViewById<ImageView>(R.id.profileImage_imageView)
            if (requestCode == IMAGE_CAPTURE_CODE && resultCode == AppCompatActivity.RESULT_OK)
                frame.setImageURI(imageUri)
            if (requestCode == RESULT_LOAD_IMAGE && resultCode == AppCompatActivity.RESULT_OK && data != null) {
                imageUri = data.data!!
                imageUriString = imageUri.toString()
                frame.setImageURI(imageUri)
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
        if (checkPermission()) {
            openCamera()
        } else
            showPermissionReasonAndRequest(
                "Notice",
                R.string.cameraPermission.toString()
            )
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



    private fun showPermissionReasonAndRequest(title: String, message: String) {
        AlertDialog.Builder(activity)
            .setTitle(title).setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                requestPermission()
            }
    }

    private fun showExitDialog(): Boolean {
        AlertDialog.Builder(activity)
            .setTitle("Are you sure?").setMessage("All changes will be lost")
            .setPositiveButton("YES") { _, _ -> findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment) }
            .setNegativeButton("NO") { _, _ -> }.show()
        return true
    }

    private fun trySaveData(): Boolean {
        return try {
            isNotValid()
            vm.updateUser()
            findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
            true
        } catch (e: Exception) {
            Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun isNotValid() {
        val user = vm.user.value!!
        fieldIsValid(user.name, "Full Name")
        fieldIsValid(user.nickname, "Nickname")
        fieldIsValid(user.description, "Description")
        fieldIsValid(user.email, "Email")
        fieldIsValid(user.phoneNumber, "Phone Number")
        fieldIsValid(user.location, "Location")
        fieldIsValid(user.birthdate, "BirthDate")

        val regexMail = Regex("^[A-Za-z\\d+_.-]+@(.+)\$")
        if (!regexMail.matches(user.email)) {
            throw Exception("invalid email format")
        }

        try {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formatter.parse(user.birthdate)
        } catch (e: ParseException) {
            throw Exception("Birthdate should be in dd-MM-yyyy format")
        }

        val regexPhone = Regex("^\\d{10}\$")
        if (!regexPhone.matches(user.phoneNumber)) {
            throw Exception("Phone number should be a 10 digit number")
        }

        imageUri?.let { user.image_uri = it.toString() }

    }

    private fun fieldIsValid(field: String?, fieldName: String) {
        if (field.isNullOrEmpty())
            throw Exception("$fieldName is invalid")
    }

    private fun setValue(attribute: String, newValue: String) {
        when (attribute) {
            "name" -> vm.user.value?.name = newValue
            "surname" -> vm.user.value?.surname = newValue
            "nickname" -> vm.user.value?.nickname = newValue
            "description" -> vm.user.value?.description = newValue
            "email" -> vm.user.value?.email = newValue
            "phoneNumber" -> vm.user.value?.phoneNumber = newValue
            "location" -> vm.user.value?.location = newValue
            "birthdate" -> vm.user.value?.birthdate = newValue
            "favouriteSport" -> vm.user.value?.favouriteSport = newValue
        }
    }

    private fun setAllView() {
        vm.getUser(1).observe(viewLifecycleOwner) { user ->

            setTextViews(user)
            setCheckBox(user)
            setImage(user)
            setSpinners(user)
        }

    }

    private fun setCheckBox(user: User) {
        setCheckedBoxViewAndListener(R.id.mondayAvailability, user.monday_availability, "monday")
        setCheckedBoxViewAndListener(R.id.tuesdayAvailability, user.tuesday_availability, "tuesday")
        setCheckedBoxViewAndListener(R.id.wednesdayAvailability, user.wednesday_availability, "wednesday")
        setCheckedBoxViewAndListener(R.id.thursdayAvailability, user.thursday_availability, "thursday")
        setCheckedBoxViewAndListener(R.id.fridayAvailability, user.friday_availability, "friday")
        setCheckedBoxViewAndListener(R.id.saturdayAvailability, user.saturday_availability, "saturday")
        setCheckedBoxViewAndListener(R.id.sundayAvailability, user.sunday_availability, "sunday")
    }

    private fun setTextViews(user: User) {
        setEditTextViewAndListener(R.id.name, user.name, "name")
        setEditTextViewAndListener(R.id.surname, user.surname, "surname")
        setEditTextViewAndListener(R.id.nickName_value, user.nickname,     "nickname")
        setEditTextViewAndListener(R.id.description_value, user.description, "description")
        setEditTextViewAndListener(R.id.mail_value, user.email, "email")
        setEditTextViewAndListener(R.id.phoneNumber_value, user.phoneNumber, "phoneNumber")
        setEditTextViewAndListener(R.id.location_value, user.location, "location")
        setEditTextViewAndListener(R.id.birthday, user.birthdate, "birthdate")
    }

    private fun setSpinners(user: User) {
        val genderSpinner = view?.findViewById<Spinner>(R.id.spinner)
        val genderArray = resources.getStringArray(R.array.genderArray)
        genderSpinner?.setSelection(genderArray.indexOf(user.gender))
        genderSpinner?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    user.gender = genderArray[position]
                }
            }

        val sportSpinner = view?.findViewById<Spinner>(R.id.sportSpinner)
        val sportArray = resources.getStringArray(R.array.sportArray)
        sportSpinner?.setSelection(sportArray.indexOf(user.favouriteSport))
        sportSpinner?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    user.favouriteSport = sportArray[position]
                }
            }

    }

    private fun setCheckedBoxViewAndListener(id: Int, availability: Boolean, attribute: String) {
        val checkBox = view?.findViewById<CheckBox>(id)
        checkBox ?.isChecked = availability
        checkBox?.setOnCheckedChangeListener { _, isChecked ->
            setAvailability(attribute, isChecked)
        }
    }

    private fun setAvailability(attribute: String, checked: Boolean) {
        when(attribute){
            "monday" -> vm.user.value?.monday_availability = checked
            "tuesday" -> vm.user.value?.tuesday_availability = checked
            "wednesday" -> vm.user.value?.wednesday_availability = checked
            "thursday" -> vm.user.value?.thursday_availability = checked
            "friday" -> vm.user.value?.friday_availability = checked
            "saturday" -> vm.user.value?.saturday_availability = checked
            "sunday" -> vm.user.value?.sunday_availability = checked
        }

    }

    private fun setEditTextViewAndListener(id: Int, field: String?, attribute: String) {
        setEditTextView(id, field)
        setOneListener(id, attribute)
    }

    private fun setEditTextView(id: Int, field: String?) {
        field?.let { view?.findViewById<EditText>(id)?.setText(field) }
    }

    private fun setOneListener(id: Int, attribute:String){
        view?.findViewById<EditText>(id)?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setValue(attribute, s.toString())
            }
        })
    }

    private fun setImage(user: User) {
        try {
            val uri = user.image_uri?.toUri()
            val imageView = view?.findViewById<CircleImageView>(R.id.profileImage_imageView)
            imageView?.setImageURI(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}