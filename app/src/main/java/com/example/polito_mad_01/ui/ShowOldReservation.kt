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
import com.example.polito_mad_01.util.UIUtils.setTextView
import com.example.polito_mad_01.viewmodel.*


class ShowOldReservation : Fragment(R.layout.fragment_show_old_reservation) {
    private var slotId = 0
    private lateinit var userId : String
    private var playgroundId = 0

    private val oldResVm: ShowOldReservationViewModel by viewModels {
        ShowOldReservationViewModelFactory((activity?.application as SportTimeApplication).reservationRepository)
    }

    private val reviewVm: ReviewViewModel by viewModels{
        ReviewViewModelFactory((activity?.application as SportTimeApplication).reviewRepository)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = requireArguments().getString("userId").orEmpty()
        slotId = requireArguments().getInt("slotId")
        playgroundId = requireArguments().getInt("playgroundId")

        val reviewLayout = view.findViewById<LinearLayout>(R.id.reviewedLinearLayout)
        val reviewButton = view.findViewById<Button>(R.id.ReviewButton)
        reviewLayout.visibility = View.GONE
        reviewButton.visibility = View.GONE

        reviewVm.getSingleReview(playgroundId).observe(viewLifecycleOwner){
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

            setTextView(R.id.oldResPlaygroundName, it.playgroundName, view)
            setTextView(R.id.oldResPlaygroundLocation, it.location, view)
            setTextView(R.id.oldResPlaygroundSport, it.sport, view)
            val stringPrice = it.total_price.toString() + "€"
            setTextView(R.id.oldResPlaygroundPrice, stringPrice, view)
            setTextView(R.id.oldResSlotDate, it.date, view)
            val stringTime = "${it.start_time}-${it.end_time}"
            setTextView(R.id.oldResSlotTime, stringTime, view)

            val image : ImageView = view.findViewById(R.id.oldResSportImage)

            oldResVm.getSportImage(playgroundId).observe(viewLifecycleOwner){imageUri ->
                imageUri?.let{image.setImageURI(imageUri)} }

            val services = mutableListOf<String>()
            if(it.services.getOrDefault("equipment",false)) services.add("- Equipment")
            if(it.services.getOrDefault("heating",false)) services.add("- Heating")
            if(it.services.getOrDefault("lighting",false)) services.add("- Lightning")
            if(it.services.getOrDefault("locker_room",false)) services.add("- Locker room")

            view.findViewById<RecyclerView>(R.id.oldResServicesView).let{list ->
                list.layoutManager = LinearLayoutManager(view.context)
                list.adapter = ServicesAdapter(services)
            }
        }
    }

}