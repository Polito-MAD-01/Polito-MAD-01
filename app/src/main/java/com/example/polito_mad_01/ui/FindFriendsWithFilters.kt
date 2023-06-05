package com.example.polito_mad_01.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.polito_mad_01.*
import com.example.polito_mad_01.adapters.FindFriendsAdapter
import com.example.polito_mad_01.util.UIUtils
import com.example.polito_mad_01.viewmodel.*
import com.google.android.material.textfield.TextInputLayout


class FindFriendsWithFilters : Fragment(R.layout.fragment_find_friends_with_filters){

    private lateinit var mView : View
    private lateinit var skillName: TextInputLayout
    private lateinit var skillValue: TextInputLayout
    private lateinit var location: TextInputLayout
    private lateinit var recyclerViewFoundFriends: RecyclerView
    private lateinit var noFriendsFound: TextView

    private val vm: FindFriendsWithFiltersViewModel by viewModels{
        FindFriendsWithFiltersViewModelFactory((activity?.application as SportTimeApplication).userRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).setTitle("Find Friends")


        mView = view

        skillName = mView.findViewById(R.id.skillName)
        skillValue = mView.findViewById(R.id.skillValue)
        location = mView.findViewById(R.id.location)

        recyclerViewFoundFriends = view.findViewById(R.id.findFriendsRecyclerView)
        noFriendsFound = UIUtils.findTextViewById(view,R.id.noFriendsFoundTextView)!!
        recyclerViewFoundFriends.visibility = View.GONE
        noFriendsFound.visibility = View.GONE
        recyclerViewFoundFriends.layoutManager = LinearLayoutManager(view.context)

        setSelectors()

        mView.findViewById<Button>(R.id.searchButton).setOnClickListener {

            var flag = false

            vm.skillName.value = skillName.editText?.text.toString()

            if(vm.skillName.value!!.isEmpty()) {
                skillName.error = "Skill name is required"
                flag = true
            }else{skillName.error = null}

            vm.skillValue.value = skillValue.editText?.text.toString()
            if(vm.skillValue.value!!.isEmpty()) {
                skillValue.error = "Skill value is required"
                flag = true
            }else{skillValue.error = null}

            vm.location.value = location.editText?.text.toString()
            if(vm.location.value!!.isEmpty()) {
                location.error = "Location is required"
                flag = true
            }else{location.error = null}

            if(flag) return@setOnClickListener

            vm.findFriendBySkillAndLocation(
                vm.skillName.value!!,
                vm.skillValue.value!!,
                vm.location.value!!
            )
                .observe(viewLifecycleOwner) { users ->
                    if(users.isEmpty()){
                        recyclerViewFoundFriends.visibility = View.GONE
                        noFriendsFound.visibility = View.VISIBLE
                    }else{
                        noFriendsFound.visibility = View.GONE
                        recyclerViewFoundFriends.visibility = View.VISIBLE
                        recyclerViewFoundFriends.adapter=FindFriendsAdapter(users, vm, findNavController())
                    }
                }
        }
    }

    private fun setSelectors() {
        val skillNameSelector = mView.findViewById<AutoCompleteTextView>(R.id.skillNameSelector)

        val skillNameArray = resources.getStringArray(R.array.sportArray)
        val skillNameAdapter = ArrayAdapter(requireContext(), R.layout.skillname_list_item, skillNameArray)
        (skillName.editText as? AutoCompleteTextView)?.setAdapter(skillNameAdapter)

        skillNameSelector.setOnItemClickListener { parent, _, position, _ ->
            vm.skillName.value = parent.getItemAtPosition(position).toString()
        }

        val skillValueArray = resources.getStringArray(R.array.sportLevelArray)
        val skillValueAdapter = ArrayAdapter(requireContext(), R.layout.skillvalue_list_item, skillValueArray)
        (skillValue.editText as? AutoCompleteTextView)?.setAdapter(skillValueAdapter)

        val skillValueSelector = mView.findViewById<AutoCompleteTextView>(R.id.skillValueSelector)
        skillValueSelector.setOnItemClickListener { parent, _, position, _ ->
            vm.skillValue.value = parent.getItemAtPosition(position).toString()
        }
    }

}