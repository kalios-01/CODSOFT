package com.kalios.quotesgenerator

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_quotes")
data class Quote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val quote: String,
    val author: String
)
