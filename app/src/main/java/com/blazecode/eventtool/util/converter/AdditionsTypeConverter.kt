/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool.util.converter

import androidx.room.TypeConverter
import com.blazecode.eventtool.enums.Additions

class AdditionsTypeConverter {

    @TypeConverter
    fun toAdditions(additionsString: String): MutableList<Additions> {
        val splitString = additionsString.split(",")
        val list: MutableList<Additions> = mutableListOf()

        if(splitString.isNotEmpty()) {
            for(additions in splitString) {
                val fixedString = additions.replace("[","").replace("]", "").trim()
                if (fixedString.isNotEmpty())
                    list.add(Additions.valueOf(fixedString))
            }
            return list
        } else return mutableListOf()
    }

    @TypeConverter
    fun toAdditionString(additionsList: MutableList<Additions>): String {
        return additionsList.toString()
    }
}