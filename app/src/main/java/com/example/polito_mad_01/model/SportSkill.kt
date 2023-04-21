package com.example.polito_mad_01.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "sport_skill",
    primaryKeys = ["sport_id", "user_id"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("user_id"),
            childColumns = arrayOf("user_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Sport::class,
            parentColumns = arrayOf("sport_id"),
            childColumns = arrayOf("sport_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SportSkill (

    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo(name = "sport_id")
    val sportId: String,

    @ColumnInfo(name = "skill_level")
    val skill_level: String
    )