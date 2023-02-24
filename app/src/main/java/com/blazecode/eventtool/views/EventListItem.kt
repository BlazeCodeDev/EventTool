/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package com.blazecode.eventtool.views

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.blazecode.eventtool.R
import com.blazecode.eventtool.data.Event
import com.blazecode.eventtool.enums.Additions
import com.blazecode.eventtool.enums.EventType
import com.blazecode.eventtool.ui.theme.Typography
import com.google.accompanist.flowlayout.FlowRow
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListItem(event: Event, onClick: () -> Unit) {
    val context = LocalContext.current
    Card (modifier = Modifier.fillMaxWidth().padding(8.dp, 8.dp, 8.dp, 0.dp),
        //onClick = { eventDetailsEvent.value = item; showEventDetails.value= true; selectedEvent.value = item }) {
        onClick = { onClick() }) {

        // GRADIENT
        Box (modifier = Modifier.background(
            brush = Brush.horizontalGradient(colors = listOf(
                colorResource(context.resources.getIdentifier(event.eventType.toString().lowercase(), "color", context.packageName)),
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.secondaryContainer)))
        ){

            Column {
                // NAME & ICON
                Row (modifier = Modifier.fillMaxWidth()){
                    // ICON
                    Box {
                        Icon(
                            painter = painterResource(context.resources.getIdentifier("ic_${event.eventType.toString().lowercase()}", "drawable", context.packageName)),
                            contentDescription = "EventType Icon",
                            modifier = Modifier.padding(8.dp))
                    }
                    // NAME
                    if(event.eventType != EventType.RESERVED){
                        Box (modifier = Modifier.fillMaxWidth().padding(0.dp,0.dp,40.dp,0.dp), contentAlignment = Alignment.Center){
                            val name = if(event.eventType == EventType.WEDDING) "${event.firstName1} / ${event.firstName2} ${event.lastName}" else event.name
                            Text(text = name, style = Typography.titleLarge, modifier = Modifier.padding(0.dp, 8.dp, 0.dp, 0.dp))
                        }
                    } else {
                        Box (modifier = Modifier.fillMaxWidth().padding(0.dp,0.dp,40.dp,0.dp), contentAlignment = Alignment.Center) {
                            Text(text = event.comments, style = Typography.titleLarge, modifier = Modifier.padding(0.dp, 8.dp, 0.dp, 0.dp))
                        }
                    }

                }

                // EVENT TYPE & DATE
                Row (modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){
                    // EVENT TYPE
                    Box {
                        val eventType = stringResource(context.resources.getIdentifier(event.eventType.toString().lowercase(), "string", context.packageName))
                        Text(text = eventType, style = Typography.bodyMedium, modifier = Modifier.padding(8.dp))
                    }
                    // DATE
                    Box (modifier = Modifier.fillMaxWidth(), Alignment.CenterEnd){
                        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                        val dayOfWeekFormatter = DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("EE").toFormatter(Locale.getDefault())
                        Text(text = "${event.date.format(dayOfWeekFormatter)}, ${event.date.format(dateFormatter)}", style = Typography.bodyMedium, modifier = Modifier.padding(8.dp))
                    }
                }

                if(event.eventType != EventType.RESERVED){
                    // VENUE & READY TIME
                    Row (verticalAlignment = Alignment.CenterVertically){
                        // VENUE
                        Box (modifier = Modifier.weight(5f)){
                            Text(text = event.venue, style = Typography.bodyMedium, modifier = Modifier.padding(8.dp))
                        }
                        // READY TIME TIME
                        Box (modifier = Modifier.weight(2f), Alignment.CenterEnd){
                            Text(text = "${stringResource(R.string.time_ready)}: ${event.timeReady}", style = Typography.bodyMedium, modifier = Modifier.padding(8.dp))
                        }
                    }
                }

                // ADDITIONS
                FlowRow (modifier = Modifier.padding(4.dp)) {
                    event.additions.forEach {
                        SuggestionChip(
                            modifier = Modifier.padding(2.dp, 0.dp, 2.dp, 0.dp),
                            label = { Text(stringResource(LocalContext.current.resources.getIdentifier(it.name.lowercase(), "string", LocalContext.current.packageName))) },
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview(){
    val context = LocalContext.current
    EventListItem(
        onClick = {
            Toast.makeText(context, "onClick", Toast.LENGTH_SHORT).show()
        },
        event = Event(null,
            name = "Test Event",
            eventType = EventType.BIRTHDAY,
            date = java.time.LocalDate.now(),
            venue = "Test Venue",
            additions = mutableListOf(Additions.FOTOBOX, Additions.CANVAS),
            timeReady = LocalTime.of(12, 0),
    ))
}