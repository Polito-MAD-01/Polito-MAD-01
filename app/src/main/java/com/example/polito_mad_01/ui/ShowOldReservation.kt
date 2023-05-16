package com.example.polito_mad_01.ui

import android.os.*
import android.view.*
import android.widget.*
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.polito_mad_01.*
import com.example.polito_mad_01.adapters.ServicesAdapter
import com.example.polito_mad_01.viewmodel.*


class ShowOldReservation : Fragment(R.layout.fragment_show_old_reservation) {
    private var slotId = 0
    private var userId = 0
    private var playgroundId = 0

    private val oldResVm: ShowOldReservationViewModel by viewModels {
        ShowOldReservationViewModelFactory((activity?.application as SportTimeApplication).showReservationsRepository)
    }

    private val reviewVm: ReviewViewModel by viewModels{
        ReviewViewModelFactory((activity?.application as SportTimeApplication).reviewRepository)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = requireArguments().getInt("userId")
        slotId = requireArguments().getInt("slotId")
        playgroundId = requireArguments().getInt("playgroundId")

        val reviewLayout = view.findViewById<LinearLayout>(R.id.reviewedLinearLayout)
        val reviewButton = view.findViewById<Button>(R.id.ReviewButton)
        reviewLayout.visibility = View.GONE
        reviewButton.visibility = View.GONE

        reviewVm.getSingleReview(userId, playgroundId).observe(viewLifecycleOwner){
            if(it==null){
                reviewLayout.visibility = View.GONE
                reviewButton.visibility = View.VISIBLE
                reviewButton.setOnClickListener {
                    val args = bundleOf(
                        "userId" to userId,
                        "playgroundId" to playgroundId,
                        "slotId" to slotId
                    )
                    findNavController().navigate(R.id.action_showOldReservation_to_createFeedback, args)
                }
            }else{
                reviewLayout.visibility = View.VISIBLE
                reviewButton.visibility = View.GONE
                view.findViewById<RatingBar>(R.id.reviewedRatingBar).rating = it.rating.toFloat()
                view.findViewById<TextView>(R.id.reviewedText).text = it.review_text
            }
        }

        oldResVm.getOldReservationById(slotId).observe(viewLifecycleOwner) {
            requireActivity().onBackPressedDispatcher
                .addCallback(this) {
                    findNavController().navigate(R.id.action_showOldReservation_to_showOldReservations)
                }
                .isEnabled = true

            setTextView(R.id.oldResPlaygroundName, it.playground.name)
            setTextView(R.id.oldResPlaygroundLocation, it.playground.location)
            setTextView(R.id.oldResPlaygroundSport, it.playground.sport_name)
            val stringPrice = it.playground.price_per_slot.toString() + "€"
            setTextView(R.id.oldResPlaygroundPrice, stringPrice)
            setTextView(R.id.oldResSlotDate, it.slot.date)
            val stringTime = "${it.slot.start_time}-${it.slot.end_time}"
            setTextView(R.id.oldResSlotTime, stringTime)

            val image : ImageView = view.findViewById(R.id.oldResSportImage)
            when(it.playground.sport_name) {
                "Football" -> image.setImageResource(R.drawable.football_photo)
                "Basket" -> image.setImageResource(R.drawable.basketball_photo)
                "Volley" -> image.setImageResource(R.drawable.volleyball_photo)
                "Ping Pong" -> image.setImageResource(R.drawable.pingpong_photo)
                else -> image.setImageResource(R.drawable.sport_photo)
            }

            val services = mutableListOf<String>()
            if(it.slot.equipment) services.add("- Equipment")
            if(it.slot.heating) services.add("- Heating")
            if(it.slot.lighting) services.add("- Lightning")
            if(it.slot.locker_room) services.add("- Locker room")

            view.findViewById<RecyclerView>(R.id.oldResServicesView).let{list ->
                list.layoutManager = LinearLayoutManager(view.context)
                list.adapter = ServicesAdapter(services)
            }
        }
    }

    private fun setTextView(viewId: Int, text: String) {
        view?.findViewById<TextView>(viewId)?.text = text
    }

}