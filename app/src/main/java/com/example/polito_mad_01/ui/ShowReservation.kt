package com.example.polito_mad_01.ui

import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.polito_mad_01.*
import com.example.polito_mad_01.adapters.ServicesAdapter
import com.example.polito_mad_01.viewmodel.*

class ShowReservation : Fragment(R.layout.fragment_show_reservation) {
    private var slotID = 0

    private val vm: ShowReservationsViewModel by viewModels {
        ShowReservationsViewModelFactory((activity?.application as SportTimeApplication).showReservationsRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        slotID = requireArguments().getInt("slotID")

        vm.getReservation(slotID).observe(viewLifecycleOwner) {
            requireActivity().onBackPressedDispatcher
                .addCallback(this) {
                    val args = bundleOf(
                        "selectedDate" to it.slot.date
                    )
                    findNavController().navigate(R.id.action_showReservationFragment2_to_reservationsFragment, args)
                }
                .isEnabled = true

            setTextView(R.id.playgroundName, it.playground.name)
            setTextView(R.id.playgroundLocation, it.playground.location)
            setTextView(R.id.playgroundSport, it.playground.sport_name)

            val image : ImageView = view.findViewById(R.id.playgroundImage)
            when(it.playground.sport_name) {
                "Football" -> image.setImageResource(R.drawable.football_photo)
                "Basket" -> image.setImageResource(R.drawable.basketball_photo)
                "Volley" -> image.setImageResource(R.drawable.volleyball_photo)
                "Ping Pong" -> image.setImageResource(R.drawable.pingping_photo)
                else -> image.setImageResource(R.drawable.sport_photo)
            }

            val stringPrice = it.playground.price_per_slot.toString() + "€"
            setTextView(R.id.playgroundPrice, stringPrice)
            setTextView(R.id.slotDate, it.slot.date)
            val stringTime = "${it.slot.start_time} - ${it.slot.end_time}"
            setTextView(R.id.slotTime, stringTime)

            var services = listOf<String>()
            services = if(it.slot.equipment) services.plus("- Equipment") else services
            services = if(it.slot.heating) services.plus("- Heating") else services
            services = if(it.slot.lighting) services.plus("- Lightning") else services
            services = if(it.slot.locker_room) services.plus("- Locker room") else services

            view.findViewById<RecyclerView>(R.id.servicesView).let{list ->
                list.layoutManager = LinearLayoutManager(view.context)
                list.adapter = ServicesAdapter(services)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_show_reservation, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val args = bundleOf(
            "slotID" to slotID
        )

        if (item.itemId == R.id.action_edit_reservation)
            findNavController().navigate(
                R.id.action_showReservationFragment2_to_editReservationFragment,
                args
            )
        return true
    }

    private fun setTextView(viewId: Int, text: String) {
        view?.findViewById<TextView>(viewId)?.text = text
    }

}