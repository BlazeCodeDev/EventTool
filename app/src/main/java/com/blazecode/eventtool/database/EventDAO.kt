/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.blazecode.eventtool.data.Event

@Dao
interface EventDAO {

    @Query("SELECT * FROM Event")
    fun getAll(): MutableList<Event>

    @Query("SELECT * FROM Event WHERE id LIKE :id")
    fun getById(id: Int): Event

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addEvent(vararg users: Event)

    @Query("DELETE FROM Event WHERE id LIKE :id")
    fun deleteById(id: Int)

    @Query("DELETE FROM Event")
    fun clear()
}