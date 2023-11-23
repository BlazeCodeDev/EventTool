/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package com.blazecode.eventtool.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.blazecode.eventtool.R
import com.blazecode.eventtool.data.Event
import com.blazecode.eventtool.enums.EventType
import com.blazecode.eventtool.util.MailUtil
import com.blazecode.eventtool.util.PhoneUtil
import com.blazecode.eventtool.util.pdf.PdfPrinter
import com.google.accompanist.flowlayout.FlowRow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetails(printer: PdfPrinter, event: Event, onEdit: () -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val name = if(event.name.isNotEmpty()) event.name else "${event.firstName1} / ${event.firstName2} ${event.lastName}"
    val eventType = stringResource(context.resources.getIdentifier(event.eventType.toString().lowercase(), "string", context.packageName))
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    ModalBottomSheet(
        onDismissRequest = { onClose() },
        sheetState = state,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(
            start = dimensionResource(id = R.dimen.xlarge_padding),
            end = dimensionResource(id = R.dimen.xlarge_padding),
            bottom = dimensionResource(id = R.dimen.xlarge_padding))) {

            Row(modifier = Modifier.fillMaxWidth()) {
                val eventColor = colorResource(context.resources.getIdentifier(event.eventType.toString().lowercase(), "color", context.packageName))
                val onBackgroundColor = MaterialTheme.colorScheme.onBackground

                val title = if (event.eventType == EventType.RESERVED) event.comments else name
                Text(
                    text = ("$title\n$eventType"),
                    fontSize = 25.sp,
                    modifier = Modifier.weight(5f)
                        .graphicsLayer(alpha = 0.99f)
                        .drawWithCache {
                            val brush = Brush.horizontalGradient(listOf(eventColor, onBackgroundColor))
                            onDrawWithContent {
                                drawContent()
                                drawRect(brush, blendMode = BlendMode.SrcAtop)
                            }
                        })
                Box(
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.icon_button_size))
                        .weight(1f)
                ) {
                    IconButton(onClick = { printer.print(event) }) {
                        Icon(painterResource(R.drawable.ic_print), "settings")
                    }
                }

            }
            Spacer(modifier = Modifier.size(dimensionResource(id = R.dimen.xlarge_padding)))
            Column {
                // DATE
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Icon(painterResource(R.drawable.ic_calendar), "date")
                    Spacer(modifier = Modifier.size(24.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Text(text = event.date.format(dateFormatter))
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                // LOCATION
                if (event.venue.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Icon(painterResource(R.drawable.ic_venue), "venue")
                        Spacer(modifier = Modifier.size(24.dp))
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            Text(text = event.venue)
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                }

                //TIMES
                if (event.eventType != EventType.RESERVED) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_time), "times")
                        Column(
                            modifier = Modifier
                                .weight(2f)
                                .padding(24.dp, 0.dp, 0.dp, 0.dp)
                        ) {
                            Text(stringResource(R.string.time_ready))
                            val time_guest = if (event.eventType == EventType.WEDDING || event.eventType == EventType.BIRTHDAY)
                                stringResource(R.string.time_buffet) else stringResource(R.string.time_guests)
                            Text(time_guest)
                            Text(stringResource(R.string.time_start))
                            Text(stringResource(R.string.time_end))
                        }
                        Column(
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxWidth(), horizontalAlignment = Alignment.End
                        ) {
                            Text(event.timeReady.toString())
                            Text(event.timeGuests.toString())
                            Text(event.timeStart.toString())
                            Text(event.timeEnd.toString())
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                }

                // GUESTS
                if (event.guestAmount != "0") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        if (event.childrenAmount == "0") {
                            Icon(painterResource(R.drawable.ic_guests), "guests")
                            Spacer(modifier = Modifier.size(24.dp))
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                                Text(text = event.guestAmount)
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f)) {
                                Icon(painterResource(R.drawable.ic_guests), "guests")
                                Spacer(modifier = Modifier.size(24.dp))
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text(text = event.guestAmount)
                                }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                Icon(painterResource(R.drawable.ic_child), "children")
                                Spacer(modifier = Modifier.size(24.dp))
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text(text = event.childrenAmount)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                }
                // WISH MUSIC
                if (event.wishMusic.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Icon(painterResource(R.drawable.ic_music), "wish music")
                        Spacer(modifier = Modifier.size(24.dp))
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            Text(text = event.wishMusic)
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                }
                // Comments
                if (event.comments.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Icon(painterResource(R.drawable.ic_comment), "comments")
                        Spacer(modifier = Modifier.size(24.dp))
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            Text(text = event.comments)
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                }
                // Additions
                if (event.additions.size > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Icon(painterResource(R.drawable.ic_additions), "additions")
                        Spacer(modifier = Modifier.size(24.dp))
                        FlowRow {
                            event.additions.forEach {
                                SuggestionChip(
                                    modifier = Modifier.padding(2.dp, 0.dp, 2.dp, 0.dp),
                                    label = { Text(stringResource(LocalContext.current.resources.getIdentifier(it.name.lowercase(), "string", LocalContext.current.packageName))) },
                                    onClick = {}
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                }
                // E-Mail
                if (event.email.isNotEmpty()) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .clickable {
                            MailUtil
                                .Builder(context)
                                .overrideRecipient(event.email)
                                .send()
                        }) {

                        Icon(painterResource(R.drawable.ic_mail), "email")
                        Spacer(modifier = Modifier.size(24.dp))
                        Box(modifier = Modifier, contentAlignment = Alignment.CenterEnd) {
                            Text(text = event.email)
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                }
                // E-Mail
                if (event.phone.isNotEmpty()) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .clickable {
                            PhoneUtil
                                .Builder(context)
                                .number(event.phone)
                                .dial()
                        }) {

                        Icon(painterResource(R.drawable.ic_call), "phone")
                        Spacer(modifier = Modifier.size(24.dp))
                        Box(modifier = Modifier, contentAlignment = Alignment.CenterEnd) {
                            Text(text = event.phone)
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                }
            }
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.medium_padding))) {
                OutlinedButton(onClick = {
                    scope.launch { state.hide() }.invokeOnCompletion {
                        if (!state.isVisible) {
                        }
                        onEdit()
                    } }) { Text(stringResource(R.string.edit)) }
            }
        }
    }
}