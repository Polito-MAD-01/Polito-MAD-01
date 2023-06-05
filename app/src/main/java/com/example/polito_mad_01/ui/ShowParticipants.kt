package com.example.polito_mad_01.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.example.polito_mad_01.R
import com.example.polito_mad_01.adapters.FriendsAdapter
import com.example.polito_mad_01.util.UIUtils
import com.example.polito_mad_01.viewmodel.ShowReservationsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class ShowParticipants(val slotID: Int, val vm: ShowReservationsViewModel) : Fragment(R.layout.fragment_show_participants) {

    private lateinit var recyclerViewParticipants: RecyclerView
    private lateinit var noParticipants: TextView
    private lateinit var plusButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fAuth = FirebaseAuth.getInstance()

        noParticipants = UIUtils.findTextViewById(view, R.id.noParticipantsTextView)!!
        noParticipants.visibility=View.GONE
        recyclerViewParticipants = view.findViewById(R.id.participantsRecyclerView)
        recyclerViewParticipants.layoutManager = LinearLayoutManager(view.context)

        vm.getReservationParticipants(slotID).observe(viewLifecycleOwner) {
            recyclerViewParticipants.adapter= FriendsAdapter(it,findNavController())

            if(it.isEmpty()){
                recyclerViewParticipants.visibility=View.GONE
                noParticipants.visibility=View.VISIBLE
            }else{
                recyclerViewParticipants.visibility=View.VISIBLE
                noParticipants.visibility=View.GONE
            }
        }

        vm.getReservation(slotID).observe(viewLifecycleOwner){ slot ->

            plusButton = view.findViewById(R.id.addParticipantsButton)
            if(slot.user_id == fAuth.currentUser?.uid){
                plusButton.visibility=View.VISIBLE

                vm.getUserFriends().observe(viewLifecycleOwner) {friends ->
                    val friendsName : Array<String> = friends.map { it.first.nickname }.toTypedArray()
                    val selectedFriends = mutableListOf<String>()

                    plusButton.setOnClickListener{
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Invite Friends")
                            .setMultiChoiceItems(
                                friendsName, null
                            ) { _, which, isChecked ->
                                if (isChecked) {
                                    selectedFriends += friends[which].first.email
                                } else if (selectedFriends.contains(friends[which].first.email)) {
                                    selectedFriends.remove(friends[which].first.email)
                                }
                            }
                            .setPositiveButton("Invite"){ dialog, _ ->
                                selectedFriends.forEach {
                                    vm.sendGameRequest(it, slot)
                                }
                                dialog.dismiss()
                            }
                            .setNegativeButton("Cancel"){ dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                }
            }else{
                plusButton.visibility=View.GONE
            }
        }
    }
}