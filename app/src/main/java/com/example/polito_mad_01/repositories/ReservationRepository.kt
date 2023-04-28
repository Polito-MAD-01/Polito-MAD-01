package com.example.polito_mad_01.repositories

import com.example.polito_mad_01.db.ReservationDao
import com.example.polito_mad_01.db.Slot
import com.example.polito_mad_01.db.SlotWithPlayground
import kotlinx.coroutines.flow.Flow

class ReservationRepository(private val reservationDao: ReservationDao) {

    val reservations: Flow<List<Slot>> = reservationDao.getAllReservations()

    fun getReservationByUserId(userID: Int) : Flow<List<Slot>> {
        return reservationDao.getReservationByUserId(userID)
    }

    fun getFreeSlots(date:String): Flow<List<SlotWithPlayground>>{
        return reservationDao.getFreeSlots(date)
    }
}