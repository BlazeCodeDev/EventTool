/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.eventtool.util.pdf

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.text.StaticLayout
import android.text.TextPaint
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import com.blazecode.eventtool.MainActivity
import com.blazecode.eventtool.R
import com.blazecode.eventtool.data.Event
import com.blazecode.eventtool.data.PdfLine
import com.blazecode.eventtool.enums.EventType
import java.io.File
import java.io.FileOutputStream


class PdfPrinter(val activity: MainActivity) {

    var event = Event(null)

    fun print(event: Event) {
        this.event = event
        startIntent()
    }

    private fun startIntent(){
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            val name = if(event.eventType == EventType.WEDDING) "${event.firstName1} / ${event.firstName2} ${event.lastName}" else event.name
            putExtra(Intent.EXTRA_TITLE, "${name}_${event.date}.pdf")
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
        }
        resultLauncher.launch(intent)
    }

    private var resultLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if(data != null) drawPdf(Uri.parse(data.dataString))
        }
    }

    private fun drawPdf(uri: Uri){
        // BITMAPS
        val logo = vectorToBitmap(R.drawable.header).scale(130, 30)
        val github = vectorToBitmap(R.drawable.ic_github).scale(20, 20)

        // https://www.geeksforgeeks.org/how-to-generate-a-pdf-file-in-android-app/
        val pdfDocument = PdfDocument()
        val paint = android.graphics.Paint(Paint.FILTER_BITMAP_FLAG)
        val title = TextPaint()

        val pageInfo = PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // HEADER
        paint.colorFilter = PorterDuffColorFilter(activity.resources.getColor(R.color.pdf_header_tint), PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(logo, (297.5f - logo.width / 2), 50f, paint)

        paint.strokeWidth = 2f          // LINE THICKNESS
        canvas.drawLine(50f, 100f, 545f, 100f, paint)

        // TEXT
        title.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        title.textSize = 13f

        val lines = mutableListOf<PdfLine>()
        // EVENT TYPE & DATE
        lines.add(PdfLine(activity.resources.getString(R.string.event_type), activity.resources.getString(activity.resources.getIdentifier(event.eventType.toString().lowercase(), "string", activity.packageName))))
        lines.add(PdfLine(activity.resources.getString(R.string.data), "${event.date.dayOfMonth}.${event.date.monthValue}.${event.date.year}"))
        lines.add(PdfLine("", ""))      //SPACER
        lines.add(PdfLine("", ""))      //SPACER

        //NAME
        val name = if(event.eventType == EventType.WEDDING) "${event.firstName1} / ${event.firstName2} ${event.lastName}" else event.name
        lines.add(PdfLine(activity.resources.getString(R.string.name), name))
        if(event.venue != "") lines.add(PdfLine(activity.resources.getString(R.string.venue), event.venue))
        lines.add(PdfLine("",""))       //SPACER

        // TIMES
        val time_guest = if(event.eventType == EventType.WEDDING || com.blazecode.eventtool.eventType.value == EventType.BIRTHDAY)
            activity.resources.getString(R.string.time_buffet) else activity.resources.getString(R.string.time_guests)
        lines.add(PdfLine(activity.resources.getString(R.string.time_ready), event.timeReady.toString()))
        lines.add(PdfLine(activity.resources.getString(R.string.time_start), event.timeStart.toString()))
        lines.add(PdfLine(time_guest, event.timeGuests.toString()))
        lines.add(PdfLine(activity.resources.getString(R.string.time_end), event.timeEnd.toString()))
        lines.add(PdfLine("",""))       //SPACER

        // GUESTS
        if(event.guestAmount != "0") lines.add(PdfLine("${activity.resources.getString(R.string.guests)} ${activity.resources.getString(R.string.guests_total)}", event.guestAmount))
        if(event.childrenAmount != "0") lines.add(PdfLine(activity.resources.getString(R.string.guests_children), event.childrenAmount))
        lines.add(PdfLine("",""))       //SPACER

        // ADDITIONS
        val additionStringList = mutableListOf<String>()
        for(addition in event.additions) {
            additionStringList.add(activity.resources.getString(activity.resources.getIdentifier(addition.toString().lowercase(), "string", activity.packageName)))
        }
        if(additionStringList.size > 0) lines.add(PdfLine(activity.resources.getString(R.string.additions), additionStringList.joinToString()))

        // WISH MUSIC & COMMENT
        lines.add(PdfLine("",""))       //SPACER
        if(event.wishMusic != "") lines.add(PdfLine(activity.resources.getString(R.string.wish_music), event.wishMusic))
        if(event.comments != "") lines.add(PdfLine(activity.resources.getString(R.string.comments), event.comments))
        lines.add(PdfLine("", ""))      //SPACER

        // CONTACT DATA
        if(event.email != "") lines.add(PdfLine(activity.resources.getString(R.string.email), event.email))
        if(event.phone != "") lines.add(PdfLine(activity.resources.getString(R.string.phone), event.phone))

        var lastHeight = 140f
        for ((index, line) in lines.withIndex()){
            title.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText(line.title, 60f, lastHeight + 10f, title)

            canvas.save()
            canvas.translate(250f, lastHeight - 3.6f)
            title.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val staticLayout = StaticLayout.Builder.obtain(line.data, 0, line.data.length, title, 250).build()
            staticLayout.draw(canvas)
            canvas.restore()

            lastHeight += staticLayout.height.toFloat()
        }

        // FOOTER
        canvas.drawLine(50f, 760f, 545f, 760f, paint)
        canvas.drawBitmap(github, (220f - logo.width / 2), 781f, paint)
        title.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        title.textSize = 11f
        title.textAlign = Paint.Align.CENTER
        canvas.drawText(activity.resources.getString(R.string.GITHUB_SOURCE_URL), 300f, 795f, title)

        pdfDocument.finishPage(page)

        savePdf(uri, pdfDocument)
    }

    private fun savePdf(uri: Uri, pdf: PdfDocument){
        val file = File(Environment.getExternalStorageDirectory(), "GFG.pdf")
        try {
            activity.contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use {
                    pdf.writeTo(it)
                }
            }
            activity.sendToast(activity.getString(R.string.export_successful))
        } catch (e: java.lang.Exception) {
            activity.sendErrorDialog(e.toString().split("Exception: ")[2])
        }
    }

    private fun vectorToBitmap(drawable: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(activity.applicationContext, drawable)
        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}