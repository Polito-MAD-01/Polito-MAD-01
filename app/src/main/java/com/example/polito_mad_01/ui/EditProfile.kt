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
import androidx.fragment.app.*
import androidx.navigation.fragment.findNavController
import com.example.polito_mad_01.*
import com.example.polito_mad_01.model.User
import com.example.polito_mad_01.util.UIUtils
import com.example.polito_mad_01.viewmodel.*
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
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
    private lateinit var mView: View
    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var surnameInputLayout: TextInputLayout
    private lateinit var achievementsInputLayout: TextInputLayout
    private lateinit var genderInputLayout: TextInputLayout
    private lateinit var birthdayInputLayout: TextInputLayout
    private lateinit var locationInputLayout: TextInputLayout
    private lateinit var nicknameInputLayout: TextInputLayout
    private lateinit var phonenumberInputLayout: TextInputLayout
    private lateinit var emailInputLayout: TextInputLayout

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mView = view
        requireActivity().onBackPressedDispatcher
            .addCallback(this) {
                showExitDialog()
            }
            .isEnabled = true

        val imgButton = view.findViewById<ImageButton>(R.id.imageButton)
        registerForContextMenu(imgButton)
        imgButton.setOnClickListener { v -> v.showContextMenu() }
        getAllView()
    }

    private fun getAllView() {
        nameInputLayout = mView.findViewById(R.id.nameInputLayout)
        surnameInputLayout = mView.findViewById(R.id.surnameInputLayout)
        locationInputLayout = mView.findViewById(R.id.locationInputLayout)
        nicknameInputLayout = mView.findViewById(R.id.nicknameInputLayout)
        achievementsInputLayout = mView.findViewById(R.id.achievementsInputLayout)
        genderInputLayout = mView.findViewById(R.id.genderInputLayout)
        birthdayInputLayout = mView.findViewById(R.id.birthdayInputLayout)
        phonenumberInputLayout = mView.findViewById(R.id.phonenumberInputLayout)
        emailInputLayout = mView.findViewById(R.id.emailInputLayout)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        setAllView()
    }

    override fun onStop() {
        super.onStop()
        saveAllView()
    }

    private fun saveAllView() {
        vm.user.value?.name = nameInputLayout.editText?.text.toString()
        vm.user.value?.surname = surnameInputLayout.editText?.text.toString()
        vm.user.value?.nickname = nicknameInputLayout.editText?.text.toString()
        vm.user.value?.gender = genderInputLayout.editText?.text.toString()
        vm.user.value?.location = locationInputLayout.editText?.text.toString()
        vm.user.value?.achievements = listOf( achievementsInputLayout.editText?.text.toString() )
        vm.user.value?.birthdate = birthdayInputLayout.editText?.text.toString()
        vm.user.value?.phoneNumber = phonenumberInputLayout.editText?.text.toString()
        vm.user.value?.email = emailInputLayout.editText?.text.toString()
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
                vm.updateUserImage(imageUriForCamera!!)
                setImage(imageUriForCamera)
            }
            else if (requestCode == RESULT_LOAD_IMAGE && data?.data != null) {
                    vm.updateUserImage(data.data!!)
                    setImage(data.data)
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
            { _, _ -> findNavController().navigate(R.id.showProfileContainer) }
            .setNegativeButton("NO") { _, _ -> }.show()
        return true
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setAllView() {
        vm.getUser().observe(viewLifecycleOwner) { user ->
            setTextViews(user)
            setButtons()
            setSpinners()
            setGenderView(user)
            setBirthdateView(user)
        }
        vm.getUserImage().observe(viewLifecycleOwner){ setImage(it) }
    }

    private fun setTextViews(user: User) {
        nameInputLayout.editText?.setText(user.name)
        surnameInputLayout.editText?.setText(user.surname)
        locationInputLayout.editText?.setText(user.location)
        nicknameInputLayout.editText?.setText(user.nickname)
        achievementsInputLayout .editText?.setText(user.achievements.toString())
        genderInputLayout .editText?.setText(user.gender)
        birthdayInputLayout.editText?.setText(user.birthdate)
        phonenumberInputLayout.editText?.setText(user.phoneNumber)
        emailInputLayout.editText?.setText(user.email)
    }

    private fun setBirthdateView(user: User) {
        val birthdateView = UIUtils.findTextInputById(view,R.id.birthday)
        birthdateView?.editText?.setText(user.birthdate)

        val materialDatePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a Date").build()
        materialDatePicker.addOnPositiveButtonClickListener {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
            birthdateView?.editText?.setText(date)
            vm.user.value?.birthdate = date
        }

        birthdateView?.editText?.setOnClickListener {
            materialDatePicker.show(childFragmentManager, "DATE_PICKER")
        }

    }

    private fun setGenderView(user: User){
        mView.findViewById<AutoCompleteTextView>(R.id.genderSelector)?.let {
            it.setText(user.gender)
            it.setOnItemClickListener {  parent, _, position, _ ->
                vm.user.value?.gender = parent.getItemAtPosition(position).toString()
            }
        }
    }

    private fun setSpinners() {
        val textField = UIUtils.findTextInputById(view,R.id.gender)
        val genderArray = resources.getStringArray(R.array.genderArray)
        val adapter = ArrayAdapter(requireContext(), R.layout.gender_list_item, genderArray)
        (textField?.editText as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setButtons(){
        setButtonAndListener(R.id.mondayButton, "monday")
        setButtonAndListener(R.id.tuesdayButton,"tuesday")
        setButtonAndListener(R.id.wednesdayButton, "wednesday")
        setButtonAndListener(R.id.thursdayButton, "thursday")
        setButtonAndListener(R.id.fridayButton, "friday")
        setButtonAndListener(R.id.saturdayButton, "saturday")
        setButtonAndListener(R.id.sundayButton, "sunday")
    }

    private fun setButtonAndListener(id: Int, attribute: String) {
        val value = vm.user.value?.availability?.get(attribute)!!
        val button = mView.findViewById<Button>(id)
        setButtonColor(value, button)
        button.setOnClickListener {
            val newValue = !vm.user.value?.availability?.get(attribute)!!
            vm.user.value?.availability?.put(attribute, newValue)
            setButtonColor(newValue, button)
        }
    }

    private fun setButtonColor(value: Boolean, button: Button) {
        val colorTrue = getColor(requireContext(), R.color.powder_blue)
        val colorFalse = getColor(requireContext(), R.color.gray)

        if (value) button.setBackgroundColor(colorTrue)
        else button.setBackgroundColor(colorFalse)
    }


    private fun setImage(image: Uri?) {
        val frame = mView.findViewById<ImageView>(R.id.profileImage_imageView)!!
        if (image!= null && image != Uri.EMPTY)
            frame.setImageURI(image)
    }
}