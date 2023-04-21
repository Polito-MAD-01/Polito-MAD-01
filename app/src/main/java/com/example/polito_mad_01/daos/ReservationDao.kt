package com.example.polito_mad_01.daos

import androidx.room.Dao
import androidx.room.Query
import com.example.polito_mad_01.model.Reservation
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {
    @Query("SELECT * from reservation")
    fun getAllReservations(): Flow<List<Reservation>>
}

