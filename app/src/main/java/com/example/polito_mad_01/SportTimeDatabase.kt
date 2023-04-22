package com.example.polito_mad_01

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.polito_mad_01.daos.*
import com.example.polito_mad_01.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        Playground::class,
        PlaygroundExtra::class,
        Reservation::class,
        ReservationExtra::class,
        Sport::class,
        SportSkill::class,
        User::class],
    version = 1
)
abstract class SportTimeDatabase : RoomDatabase() {

    companion object {
        @Volatile
        private var INSTANCE: SportTimeDatabase? = null
        fun getDatabase(context: Context): SportTimeDatabase {
            return INSTANCE ?: synchronized(this) {
                val i =
                    Room.databaseBuilder(
                        context.applicationContext,
                        SportTimeDatabase::class.java,
                        "sport_time.db"
                    )
                        //.addCallback(SportTimeDatabaseCallback(scope))
                        .createFromAsset("database/sport_time.db")
                        .build()
                INSTANCE = i
                return i
            }
        }
    }

    abstract fun userDao(): UserDao
    abstract fun playgroundDao(): PlaygroundDao
    abstract fun sportDao(): SportDao
    abstract fun reservationDao(): ReservationDao
    abstract fun skillDao(): SkillDao

}