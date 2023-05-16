package com.example.polito_mad_01.adapters

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.polito_mad_01.R
import com.example.polito_mad_01.db.SlotWithPlayground


class FreeSlotAdapter(val data:List<SlotWithPlayground>): RecyclerView.Adapter<FreeSlotAdapter.FreeSlotHolder>(){
    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FreeSlotHolder{
        val v = LayoutInflater.from(parent.context).inflate(R.layout.freeslot_item_layout ,parent, false)
        return FreeSlotHolder(v)
    }

    override fun onBindViewHolder(holder: FreeSlotHolder, position: Int) {
        val fs = data[position]
        holder.bind(fs)
    }

    class FreeSlotHolder(val v: View): RecyclerView.ViewHolder(v){
        private val playgroundName = findTextById(R.id.reservationPlayground, v)
        private val date = findTextById(R.id.reservationDate,v)
        private val time = findTextById(R.id.reservationTime, v)
        private val sportImage : ImageView = v.findViewById(R.id.reservationSportImage)

        fun bind(fs: SlotWithPlayground){
            playgroundName.text = fs.playground.name
            date.text = fs.slot.date
            val startToEnd = "${fs.slot.start_time} - ${fs.slot.end_time}"
            time.text = startToEnd

            sportImage.setImageResource(when(fs.playground.sport_name){
                "Football" -> R.drawable.sports_soccer_48px
                "Volley" -> R.drawable.sports_volleyball_48px
                "Ping Pong" -> R.drawable.sports_tennis_48px
                "Basket" -> R.drawable.sports_basketball_48px
                else -> R.drawable.selection_background
            })
        }

        private fun findTextById(id: Int, v: View): TextView {
            return v.findViewById<TextView>(id)
        }
    }


}