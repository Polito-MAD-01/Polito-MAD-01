package com.example.polito_mad_01.ui

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.polito_mad_01.R
import com.google.android.material.textfield.TextInputLayout

class Step3Fragment: Fragment(R.layout.step3fragment) {
    private lateinit var mView: View


    @SuppressLint("FragmentLiveDataObserve")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?    ): View {
        mView = inflater.inflate(R.layout.step3fragment, container, false)


        setupSkill(mView, R.id.checkBoxBasket, R.id.sportLevelBasket)
        setupSkill(mView, R.id.checkBoxFootball, R.id.sportLevelFootball)
        setupSkill(mView, R.id.checkBoxPingPong, R.id.sportLevelPingPong)
        setupSkill(mView, R.id.checkBoxVolleyball, R.id.sportLevelVolleyball)



        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setSpinners(R.id.sportLevelBasket)
        setSpinners(R.id.sportLevelFootball)
        setSpinners(R.id.sportLevelVolleyball)
        setSpinners(R.id.sportLevelPingPong)
    }


    private fun setupSkill(view: View, checkboxId: Int, textInputId: Int){
        mView.findViewById<CheckBox>(checkboxId).let { checkbox ->
            checkbox.setOnClickListener {
                mView.findViewById<TextInputLayout>(textInputId).isEnabled = checkbox.isChecked
            }
        }
    }


    private fun setSpinners(id: Int) {
        val textField1 = requireView().findViewById<TextInputLayout>(id)
        val sportLevelArray = resources.getStringArray(R.array.sportLevelArray)
        val adapter1 = ArrayAdapter(requireContext(), R.layout.sport_list_item, sportLevelArray)
        (textField1.editText as? AutoCompleteTextView)?.setAdapter(adapter1)

    }




}