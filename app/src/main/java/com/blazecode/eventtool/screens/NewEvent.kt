/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package com.blazecode.eventtool

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.blazecode.eventtool.enums.Additions
import com.blazecode.eventtool.enums.EventType
import com.blazecode.eventtool.enums.TimeType
import com.blazecode.eventtool.ui.theme.EventToolTheme
import com.blazecode.eventtool.ui.theme.Typography
import com.blazecode.eventtool.util.MailUtil
import com.blazecode.eventtool.util.PhoneUtil
import com.blazecode.eventtool.viewmodels.NewEventViewModel
import com.google.accompanist.flowlayout.FlowRow
import com.marosseleng.compose.material3.datetimepickers.time.ui.dialog.TimePickerDialog
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

private val eventType = mutableStateOf(EventType.UNKNOWN)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEvent(viewModel: NewEventViewModel = viewModel(), navController: NavController){
    val context = LocalContext.current

    LaunchedEffect(context){
        eventType.value = viewModel.event.eventType
    }

    EventToolTheme {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        Scaffold(
            topBar = { TopAppBar(viewModel, LocalContext.current, scrollBehavior) },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            content = { paddingValues ->
                Column(modifier = Modifier
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())) {
                    MainLayout(viewModel, navController) }
            },
            floatingActionButton = { SaveFAB(viewModel, navController) }
        )
    }

    BackHandler (enabled = true){
        navController.popBackStack()
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun MainLayout(viewModel: NewEventViewModel, navController: NavController) {
    val context = LocalContext.current

    AnimatedContent(targetState = eventType.value,
        transitionSpec = { fadeIn() with fadeOut() }) { targetState ->
        Column (horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(0.dp,0.dp,0.dp, dimensionResource(R.dimen.fab_height_padding))){
            when (targetState) {
            EventType.WEDDING -> {
                DatePickerLayout(viewModel, context)
                WeddingNameLayout(viewModel)
                VenueLayout(viewModel)
                TimePickerLayout(viewModel, context)
                GuestAmountLayout(viewModel)
                WishMusicLayout(viewModel)
                CommentsLayout(viewModel)
                AdditionsLayout(viewModel)
                ContactLayout(viewModel)
                if(viewModel.editMode) DeleteLayout(viewModel, navController)
            }
            EventType.CLUB, EventType.FOLKFEST -> {
                DatePickerLayout(viewModel, context)
                SimpleNameLayout(viewModel)
                VenueLayout(viewModel)
                TimePickerLayout(viewModel, context)
                WishMusicLayout(viewModel)
                CommentsLayout(viewModel)
                AdditionsLayout(viewModel)
                ContactLayout(viewModel)
                if(viewModel.editMode) DeleteLayout(viewModel, navController)
            }
            EventType.RENTAL -> {
                DatePickerLayout(viewModel, context)
                SimpleNameLayout(viewModel)
                VenueLayout(viewModel)
                TimePickerLayout(viewModel, context)
                CommentsLayout(viewModel)
                AdditionsLayout(viewModel)
                ContactLayout(viewModel)
                if(viewModel.editMode) DeleteLayout(viewModel, navController)
            }
            EventType.RESERVED -> {
                DatePickerLayout(viewModel, context)
                CommentsLayout(viewModel)
                if(viewModel.editMode) DeleteLayout(viewModel, navController)
            }
            else -> {
                DatePickerLayout(viewModel, context)
                SimpleNameLayout(viewModel)
                VenueLayout(viewModel)
                TimePickerLayout(viewModel, context)
                GuestAmountLayout(viewModel)
                WishMusicLayout(viewModel)
                CommentsLayout(viewModel)
                AdditionsLayout(viewModel)
                ContactLayout(viewModel)
                if(viewModel.editMode) DeleteLayout(viewModel, navController)
            }
        }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerLayout(viewModel: NewEventViewModel, context: Context){
    val showDialog = rememberSaveable { mutableStateOf(false) }
    val tempDate = rememberSaveable { mutableStateOf(viewModel.event.date) }
    val state = rememberDatePickerState(initialSelectedDateMillis = tempDate.value.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
    val showDialogDateBefore = rememberSaveable { mutableStateOf(false) }

    Card (modifier = Modifier
        .padding(8.dp, 0.dp, 8.dp, 8.dp)
        .clickable { showDialog.value = true }){
        Row (modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)){

            Text(stringResource(R.string.date), style = Typography.titleLarge,
            modifier = Modifier.weight(1f))

            Box (modifier = Modifier
                .fillMaxWidth()
                .weight(1f), Alignment.Center){
                Text("${tempDate.value.dayOfMonth}." +
                        "${tempDate.value.monthValue}." +
                        "${tempDate.value.year}",
                    fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    if(showDialog.value){
        DatePickerDialog(
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                Button(onClick = {
                    tempDate.value = Instant.ofEpochMilli(state.selectedDateMillis!!).atZone(ZoneId.systemDefault()).toLocalDate()
                    if(tempDate.value.isBefore(LocalDate.now())){
                        showDialogDateBefore.value = true
                    } else viewModel.event.date = tempDate.value
                    showDialog.value = false
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog.value = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(
                state = state
            )
        }
    }

    if(showDialogDateBefore.value) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.error)) },
            text = { Text(stringResource(R.string.date_is_in_past)) },
            confirmButton = { Button(onClick = { showDialogDateBefore.value = false; showDialog.value = true }) { Text(stringResource(R.string.cancel)) } },
            dismissButton = { OutlinedButton(onClick = { showDialogDateBefore.value = false; viewModel.event.date = tempDate.value }) { Text(stringResource(R.string.save_anyway)) } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerLayout(viewModel: NewEventViewModel, context: Context){
    val showDialog = rememberSaveable { mutableStateOf(false) }
    val showTimeInPast = rememberSaveable { mutableStateOf(false) }
    val timeType = rememberSaveable { mutableStateOf(TimeType.READY) }
    val tempTimeReady = remember { mutableStateOf(viewModel.event.timeReady) }
    val tempTimeStart = remember { mutableStateOf(viewModel.event.timeStart) }
    val tempTimeGuests = remember { mutableStateOf(viewModel.event.timeReady) }
    val tempTimeEnd = remember { mutableStateOf(viewModel.event.timeEnd) }

    Card (modifier = Modifier.padding(8.dp, 0.dp, 8.dp, 8.dp)){
        Column (modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)){
            Text(stringResource(R.string.times), style = Typography.titleLarge)

            Row {
                Box (modifier = Modifier.weight(1f), Alignment.Center){
                    Text(stringResource(R.string.time_ready))
                }
                Box (modifier = Modifier.weight(1f), Alignment.Center) {
                    val time_guest = if(eventType.value == EventType.WEDDING || eventType.value == EventType.BIRTHDAY)
                        stringResource(R.string.time_buffet) else stringResource(R.string.time_guests)
                    Text(time_guest)
                }
                Box (modifier = Modifier.weight(1f), Alignment.Center) {
                    Text(stringResource(R.string.time_start))
                }
                Box (modifier = Modifier.weight(1f), Alignment.Center) {
                    Text(stringResource(R.string.time_end))
                }
            }

            Row {
                Box (modifier = Modifier
                    .weight(1f)
                    .clickable {
                        showDialog.value = true
                        timeType.value = TimeType.READY
                    }, Alignment.Center){
                    Text("${tempTimeReady.value}", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
                Box (modifier = Modifier
                    .weight(1f)
                    .clickable {
                        showDialog.value = true
                        timeType.value = TimeType.GUESTS
                    }, Alignment.Center){
                    Text("${tempTimeGuests.value}", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
                Box (modifier = Modifier
                    .weight(1f)
                    .clickable {
                        showDialog.value = true
                        timeType.value = TimeType.START
                    }, Alignment.Center){
                    Text("${tempTimeStart.value}", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
                Box (modifier = Modifier
                    .weight(1f)
                    .clickable {
                        showDialog.value = true
                        timeType.value = TimeType.END
                    }, Alignment.Center){
                    Text("${tempTimeEnd.value}", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    if(showDialog.value){

        var hour = LocalTime.now().hour
        var minute = LocalTime.now().minute

        when (timeType.value){
            TimeType.READY -> {
                hour = tempTimeReady.value.hour
                minute = tempTimeReady.value.minute
            }
            TimeType.GUESTS -> {
                hour = tempTimeGuests.value.hour
                minute = tempTimeGuests.value.minute
            }
            TimeType.START -> {
                hour = tempTimeStart.value.hour
                minute = tempTimeStart.value.minute
            }
            TimeType.END -> {
                hour = tempTimeEnd.value.hour
                minute = tempTimeEnd.value.minute
            }
        }

        TimePickerDialog(
            onDismissRequest = { showDialog.value = false },
            initialTime = LocalTime.of(hour, minute),
            onTimeChange = {
                when (timeType.value){
                    TimeType.READY -> {
                        tempTimeReady.value = LocalTime.of(it.hour, it.minute)
                        if(viewModel.event.date == LocalDate.now() && tempTimeReady.value.isBefore(LocalTime.now())){
                            showTimeInPast.value = true
                        } else viewModel.event.timeReady = tempTimeReady.value
                    }
                    TimeType.GUESTS -> {
                        tempTimeGuests.value = LocalTime.of(it.hour, it.minute)
                        if(viewModel.event.date == LocalDate.now() && tempTimeGuests.value.isBefore(LocalTime.now())){
                            showTimeInPast.value = true
                        } else viewModel.event.timeGuests = tempTimeGuests.value
                    }
                    TimeType.START -> {
                        tempTimeStart.value = LocalTime.of(it.hour, it.minute)
                        if(viewModel.event.date == LocalDate.now() && tempTimeStart.value.isBefore(LocalTime.now())){
                            showTimeInPast.value = true
                        } else viewModel.event.timeStart = tempTimeStart.value
                    }
                    TimeType.END -> {
                        tempTimeEnd.value = LocalTime.of(it.hour, it.minute)
                        if(viewModel.event.date == LocalDate.now() && tempTimeEnd.value.isBefore(LocalTime.now())){
                            showTimeInPast.value = true
                        } else viewModel.event.timeEnd = tempTimeEnd.value
                    }
                }
                showDialog.value = false
            }
        )
    }

    if(showTimeInPast.value) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.error)) },
            text = { Text(stringResource(R.string.time_is_in_past)) },
            confirmButton = { Button(onClick = {
                showTimeInPast.value = false
                showDialog.value = true}) {
                Text(stringResource(R.string.cancel)) } },
            dismissButton = { OutlinedButton(onClick = {
                showTimeInPast.value = false
                when (timeType.value){
                    TimeType.READY -> viewModel.event.timeReady = tempTimeReady.value
                    TimeType.GUESTS -> viewModel.event.timeGuests = tempTimeGuests.value
                    TimeType.START -> viewModel.event.timeStart = tempTimeStart.value
                    TimeType.END -> viewModel.event.timeEnd = tempTimeEnd.value
                }}) {
                Text(stringResource(R.string.save_anyway)) } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleNameLayout(viewModel: NewEventViewModel) {
    val tempName = rememberSaveable { mutableStateOf(viewModel.event.name) }

    Card (modifier = Modifier
        .padding(8.dp, 0.dp, 8.dp, 8.dp)
        .fillMaxWidth()) {
        Column (modifier = Modifier.padding(12.dp)){
            Text(stringResource(R.string.name), style = Typography.titleLarge)
            OutlinedTextField(
                value = tempName.value,
                onValueChange = { tempName.value = it; viewModel.event.name = it.trim() },
                singleLine = true,
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VenueLayout(viewModel: NewEventViewModel) {
    val tempVenue = rememberSaveable { mutableStateOf(viewModel.event.venue) }

    Card (modifier = Modifier
        .padding(8.dp, 0.dp, 8.dp, 8.dp)
        .fillMaxWidth()) {
        Column (modifier = Modifier.padding(12.dp)){
            Text(stringResource(R.string.venue), style = Typography.titleLarge)
            OutlinedTextField(
                value = tempVenue.value,
                onValueChange = { tempVenue.value = it; viewModel.event.venue = it.trim() },
                label = { Text(stringResource(R.string.venue)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 4
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WishMusicLayout(viewModel: NewEventViewModel) {
    val tempWishMusic = rememberSaveable { mutableStateOf(viewModel.event.wishMusic) }

    Card (modifier = Modifier
        .padding(8.dp, 0.dp, 8.dp, 8.dp)
        .fillMaxWidth()) {
        Column (modifier = Modifier.padding(12.dp)){
            Text(stringResource(R.string.wish_music), style = Typography.titleLarge)
            OutlinedTextField(
                value = tempWishMusic.value,
                onValueChange = { tempWishMusic.value = it; viewModel.event.wishMusic = it.trim() },
                label = { Text(stringResource(R.string.wish_music)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentsLayout(viewModel: NewEventViewModel) {
    val tempComments = rememberSaveable { mutableStateOf(viewModel.event.comments) }

    Card (modifier = Modifier
        .padding(8.dp, 0.dp, 8.dp, 8.dp)
        .fillMaxWidth()) {
        Column (modifier = Modifier.padding(12.dp)){
            Text(stringResource(R.string.comments), style = Typography.titleLarge)
            OutlinedTextField(
                value = tempComments.value,
                onValueChange = { tempComments.value = it; viewModel.event.comments = it.trim() },
                label = { Text(stringResource(R.string.comments)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdditionsLayout(viewModel: NewEventViewModel) {
    val allAdditions: MutableList<Additions> = Additions.values().toMutableList()
    val additions = rememberSaveable { mutableStateOf(viewModel.event.additions) }

    Card (modifier = Modifier
        .padding(8.dp, 0.dp, 8.dp, 8.dp)
        .fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
             Text(stringResource(R.string.additions), style = Typography.titleLarge)
             FlowRow () {
                 allAdditions.forEach {
                    var isSelected by remember { mutableStateOf(additions.value.contains(it)) }
                    val color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                    InputChip(
                        modifier = Modifier.padding(2.dp, 0.dp, 2.dp, 0.dp),
                        colors = InputChipDefaults.inputChipColors(selectedContainerColor = color),
                        label = {Text(stringResource(LocalContext.current.resources.getIdentifier(it.name.lowercase(), "string", LocalContext.current.packageName)))},
                        selected = isSelected,
                        onClick = {
                            isSelected = !isSelected
                            val addition = getAddition(it.toString())

                            if(additions.value.contains(addition)){
                                additions.value.remove(addition)
                            }else{
                                additions.value.add(addition!!)
                            }
                            viewModel.event.additions = additions.value
                        }
                    )
                }
            }
        }
    }
}

private fun getAddition(value: String): Additions? {
    val map = Additions.values().associateBy(Additions::name)
    return map[value]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuestAmountLayout(viewModel: NewEventViewModel){
    val tempGuestAmount = rememberSaveable { mutableStateOf(viewModel.event.guestAmount) }
    val tempChildAmount = rememberSaveable { mutableStateOf(viewModel.event.childrenAmount) }

    Card (modifier = Modifier
        .padding(8.dp, 0.dp, 8.dp, 8.dp)
        .fillMaxWidth()) {
        Column (modifier = Modifier.padding(12.dp)){
            Text(stringResource(R.string.guests), style = Typography.titleLarge)
            Row {
                OutlinedTextField(
                    value = tempGuestAmount.value,
                    onValueChange = { tempGuestAmount.value = it; viewModel.event.guestAmount = it.trim() },
                    label = { Text(stringResource(R.string.guests_total)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(8.dp))
                OutlinedTextField(
                    value = tempChildAmount.value,
                    onValueChange = { tempChildAmount.value = it; viewModel.event.childrenAmount = it.trim() },
                    label = { Text(stringResource(R.string.guests_children)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeddingNameLayout(viewModel: NewEventViewModel){
    val tempFirstName1 = rememberSaveable { mutableStateOf(viewModel.event.firstName1) }
    val tempFirstName2 = rememberSaveable { mutableStateOf(viewModel.event.firstName2) }
    val tempLastName = rememberSaveable { mutableStateOf(viewModel.event.lastName) }

    Card (modifier = Modifier
        .padding(8.dp, 0.dp, 8.dp, 8.dp)
        .fillMaxWidth()) {
        Column (modifier = Modifier.padding(12.dp)){
            Text(stringResource(R.string.names), style = Typography.titleLarge)
            Row {
                OutlinedTextField(
                    value = tempFirstName1.value,
                    onValueChange = { tempFirstName1.value = it; viewModel.event.firstName1 = it.trim() },
                    label = { Text(stringResource(R.string.first_name)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(8.dp))
                OutlinedTextField(
                    value = tempFirstName2.value,
                    onValueChange = { tempFirstName2.value = it; viewModel.event.firstName2 = it.trim() },
                    label = { Text(stringResource(R.string.first_name)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f))
            }
            OutlinedTextField(
                value = tempLastName.value,
                onValueChange = { tempLastName.value = it; viewModel.event.lastName = it.trim() },
                label = { Text(stringResource(R.string.last_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactLayout(viewModel: NewEventViewModel) {
    val tempEmail = rememberSaveable { mutableStateOf(viewModel.event.email) }
    val tempPhone = rememberSaveable { mutableStateOf(viewModel.event.phone) }

    val sendMail = rememberSaveable { mutableStateOf(false) }
    val callNumber = rememberSaveable { mutableStateOf(false) }

    Card (modifier = Modifier
        .padding(8.dp, 0.dp, 8.dp, 8.dp)
        .fillMaxWidth()) {
        Column (modifier = Modifier.padding(12.dp)){
            Text(stringResource(R.string.contact), style = Typography.titleLarge)
            Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
                OutlinedTextField(
                    value = tempEmail.value,
                    onValueChange = { tempEmail.value = it; viewModel.event.email = it.trim() },
                    singleLine = true,
                    label = { Text(stringResource(R.string.email)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.weight(5f)
                )
                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f),
                    onClick = { sendMail.value = true }) {
                    Icon(Icons.Filled.Send, stringResource(R.string.email))
                }
            }
            Spacer(modifier = Modifier.size(6.dp))
            Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                OutlinedTextField(
                    value = tempPhone.value,
                    onValueChange = { tempPhone.value = it; viewModel.event.phone = it.trim() },
                    singleLine = true,
                    label = { Text(stringResource(R.string.phone)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(5f)
                )
                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f),
                    onClick = { callNumber.value = true }) {
                    Icon(Icons.Filled.Call, stringResource(R.string.phone))
                }
            }
        }
    }

    if(sendMail.value) {
        sendMail.value = false
        if(tempEmail.value.isNotEmpty()) {
            MailUtil.Builder(LocalContext.current).overrideRecipient(tempEmail.value).send()
        } else {
            Toast.makeText(LocalContext.current, stringResource(R.string.email_empty), Toast.LENGTH_SHORT).show()
        }
    }

    if(callNumber.value) {
        callNumber.value = false
        if(tempPhone.value.isNotEmpty()) {
            PhoneUtil.Builder(LocalContext.current).number(tempPhone.value).dial()
        } else {
            Toast.makeText(LocalContext.current, stringResource(R.string.phone_empty), Toast.LENGTH_SHORT).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(viewModel: NewEventViewModel, context: Context, scrollBehavior: TopAppBarScrollBehavior){
    var title = ""
    title = if(viewModel.editMode) stringResource(R.string.edit_event_title, stringResource(context.resources.getIdentifier(eventType.value.toString().lowercase(), "string", context.packageName)))
    else stringResource(R.string.add_event_title, stringResource(context.resources.getIdentifier(eventType.value.toString().lowercase(), "string", context.packageName)))

    LargeTopAppBar(
        title = {
            Row (verticalAlignment = Alignment.CenterVertically){
                Box(modifier = Modifier.weight(5f)) {
                    Text(title)
                }
                if(viewModel.editMode) {
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Box(modifier = Modifier.weight(1f)) {
                        TopAppBarDropDown(viewModel, context)
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun TopAppBarDropDown(viewModel: NewEventViewModel, context: Context){
    var menuExpanded by remember { mutableStateOf(false) }
    val eventTypeList = EventType.values()

    Box(contentAlignment = Alignment.Center, modifier = Modifier
        .size(dimensionResource(R.dimen.icon_button_size))
        .clickable { menuExpanded = !menuExpanded }) {
        Icon(painterResource(R.drawable.ic_expand_more), "change event type")
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            eventTypeList.forEach {
                if(it != viewModel.event.eventType &&  it != EventType.UNKNOWN) {
                    DropdownMenuItem(
                        onClick = { eventType.value = it; viewModel.changeEventType(it); menuExpanded = false },
                        text = { Text(stringResource(context.resources.getIdentifier(it.toString().lowercase(), "string", context.packageName))) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveFAB(viewModel: NewEventViewModel, navController: NavController){
    val context = LocalContext.current
    val showErrorDialog = rememberSaveable { mutableStateOf(false) }
    val save = rememberSaveable { mutableStateOf(false) }

    ExtendedFloatingActionButton(
        icon = { Icon(painterResource(R.drawable.ic_save),"") },
        text = { Text(stringResource(R.string.save)) },
        onClick = { save.value = true },
        elevation = FloatingActionButtonDefaults.elevation(8.dp))

    if(save.value){
        // INPUT RULES
        if((viewModel.event.date != LocalDate.now() && (viewModel.event.name.isNotEmpty() || viewModel.event.lastName.isNotEmpty()) &&
            viewModel.event.venue.isNotEmpty() && (viewModel.event.email.isNotEmpty() || viewModel.event.phone.isNotEmpty())) ||
            viewModel.event.eventType == EventType.RESERVED) {

            viewModel.saveEvent()
            navController.popBackStack()
        } else {
            showErrorDialog.value = true
        }
        save.value = false
    }

    if(showErrorDialog.value){
        AlertDialog(
            onDismissRequest = {showErrorDialog.value = false},
            title = { Text(stringResource(R.string.error)) },
            text = { Text(stringResource(R.string.please_enter_min_req)) },
            dismissButton = { OutlinedButton(onClick = { showErrorDialog.value = false; viewModel.saveEvent(); navController.popBackStack() }) { Text(stringResource(R.string.save_anyway)) } },
            confirmButton = { Button(onClick = { showErrorDialog.value = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}


@Composable
private fun DeleteLayout(viewModel: NewEventViewModel, navController: NavController){
    val context = LocalContext.current
    val showConfirmDialog = rememberSaveable { mutableStateOf(false) }
    val deletionConfirmed = rememberSaveable { mutableStateOf(false) }

    Button(
        modifier = Modifier.padding(16.dp),
        onClick = { showConfirmDialog.value = true },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        content = {
            Icon(painter = painterResource(R.drawable.ic_delete), "delete", tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = stringResource(R.string.delete_event),
                textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onErrorContainer)

        }
    )

    if(showConfirmDialog.value){
        AlertDialog(
            onDismissRequest = {showConfirmDialog.value = false},
            title = { Text(stringResource(R.string.delete_event)) },
            text = { Text(stringResource(R.string.cannot_be_undone)) },
            confirmButton = { Button(onClick = { showConfirmDialog.value = false })
            { Text(stringResource(R.string.cancel)) } },
            dismissButton = { OutlinedButton(onClick = { showConfirmDialog.value = false; deletionConfirmed.value = true },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error))
            { Text(stringResource(R.string.delete_event), color = MaterialTheme.colorScheme.error) } }
        )
    }

    if(deletionConfirmed.value){
        deletionConfirmed.value = false
        viewModel.deleteEvent()
        navController.popBackStack()
    }
}