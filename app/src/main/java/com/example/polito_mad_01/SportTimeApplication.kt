package com.example.polito_mad_01

import android.app.Application
import com.example.polito_mad_01.repositories.ReservationRepository
import com.example.polito_mad_01.repositories.ReviewRepository
import com.example.polito_mad_01.repositories.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class SportTimeApplication : Application() {
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    private val database by lazy { SportTimeDatabase.getDatabase(this) }
    val userRepository by lazy { UserRepository(database.userDao()) }
    val reservationRepository by lazy { ReservationRepository(database.reservationDao()) }
    val showReservationsRepository by lazy { ReservationRepository(database.reservationDao()) }
    val reviewRepository by lazy { ReviewRepository(database.reviewDao()) }
}