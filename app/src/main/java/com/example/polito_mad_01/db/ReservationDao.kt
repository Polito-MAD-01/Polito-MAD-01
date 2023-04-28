package com.example.polito_mad_01.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {
    @Query("SELECT * from slot")
    fun getAllReservations(): Flow<List<Slot>>

    @Transaction
    @Query("SELECT * from slot WHERE slot.user_id = :user_id")
    fun getReservationByUserId(user_id: Int): Flow<List<SlotWithPlayground>>

    /** @param date format: yyyy-MM-dd*/
    @Transaction
    @Query("SELECT * from slot " +
            "INNER JOIN playground ON slot.playground_id = playground.playground_id " +
            "where user_id <> null " +
            "and date > :date")
    fun getFreeSlots(sport_name:String, date:String): Flow<List<SlotWithPlayground>>

    @Query("DELETE * from slot WHERE slot_id = :slot_id")
    fun deleteSlot(slot_id: Int) : Int

}

