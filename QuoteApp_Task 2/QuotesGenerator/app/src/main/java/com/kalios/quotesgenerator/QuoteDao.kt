package com.kalios.quotesgenerator

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface QuoteDao {
    @Insert
    suspend fun insert(quote: Quote)

    @Query("SELECT * FROM favorite_quotes")
    suspend fun getAllQuotes(): List<Quote>

    @Delete
    suspend fun delete(quote: Quote)
}