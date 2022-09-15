/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */
package com.blazecode.eventtool.util

import android.content.Context
import android.content.Intent
import android.net.Uri

// TUTORIAL
// https://www.baeldung.com/kotlin/builder-pattern

class PhoneUtil private constructor(
    val context: Context,
    val number: String,
)   {

    data class Builder(
        var context: Context,
        var number: String = "null",
    ) {

        fun context(context: Context) = apply { this.context = context }
        fun number(number: String) = apply { this.number = number }
        fun dial() = PhoneUtil(context, number).dialNumber(this)
    }

    fun dialNumber(builder: Builder) {

        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$number")

        context?.startActivity(intent)
    }

}