/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package com.blazecode.eventtool.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.blazecode.eventtool.R
import com.blazecode.eventtool.data.Event
import com.blazecode.eventtool.data.EventDateType
import com.blazecode.eventtool.enums.Additions
import com.blazecode.eventtool.enums.EventType
import com.blazecode.eventtool.navigation.NavRoutes
import com.blazecode.eventtool.notificationTapEvent
import com.blazecode.eventtool.ui.theme.EventToolTheme
import com.blazecode.eventtool.ui.theme.Typography
import com.blazecode.eventtool.util.NotificationManager
import com.blazecode.eventtool.util.pdf.PdfPrinter
import com.blazecode.eventtool.viewmodels.HomeViewModel
import com.blazecode.eventtool.views.DefaultPreference
import com.blazecode.eventtool.views.EventDetails
import com.blazecode.eventtool.views.EventListItem
import com.blazecode.eventtool.views.SwitchPreference
import com.blazecode.tsviewer.util.updater.GitHubUpdater
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

private var showDialog = mutableStateOf(false)
private var showEventDetails = mutableStateOf(false)
private var eventDetailsEvent = mutableStateOf(Event(null))
private val isDialogVisible = mutableStateOf(false)

private val tappedDate = mutableStateOf( LocalDate.now() )

private val colorfulDaysEnabled = mutableStateOf(false)
private val debugUpdateCheckEnabled = mutableStateOf(false)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(viewModel: HomeViewModel = viewModel(), navController: NavController, printer: PdfPrinter) {
    val context = LocalContext.current

    LaunchedEffect(context) {
        colorfulDaysEnabled.value = viewModel.getColorfulDays(context)
        debugUpdateCheckEnabled.value = viewModel.getDebugUpdateCheck(context)

        if(notificationTapEvent.value != null){
            NotificationManager().remove(context, notificationTapEvent.value!!.id!!)
            eventDetailsEvent.value = notificationTapEvent.value!!
            showEventDetails.value = true
            notificationTapEvent.value = null
        }
    }

    EventToolTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

            if(showDialog.value) EventTypeChooser(navController, tappedDate.value)
            if(showEventDetails.value)
                EventDetails(
                    navController = navController,
                    printer = printer,
                    event = eventDetailsEvent.value,
                    onEdit = {
                        EditEvent(navController, eventDetailsEvent.value)
                        showEventDetails.value = false
                    },
                    onClose = {
                        showEventDetails.value = false
                    })

            Scaffold(
                topBar = { TopAppBar(navController, DebugDialog(viewModel)) },
                floatingActionButtonPosition = FabPosition.End,
                floatingActionButton = { AddEventFAB() },
                content = { paddingValues ->
                    Column(modifier = Modifier.padding(paddingValues)) {
                        if((debugUpdateCheckEnabled.value && com.blazecode.eventtool.BuildConfig.DEBUG) || !com.blazecode.eventtool.BuildConfig.DEBUG)
                            GitHubUpdater(context)
                        MainLayout(viewModel, navController)
                    }
                })
        }
    }
}

@Composable
private fun MainLayout(viewModel: HomeViewModel, navController: NavController) {
    val context = LocalContext.current

    dynamicLightColorScheme(context)

    // GET DATABASE
    var eventList by remember { mutableStateOf(mutableListOf<Event>()) }

    val scope = rememberCoroutineScope()
    LaunchedEffect(context) {
        scope.launch {
            eventList = viewModel.getEventList()
        }
    }
    // LAYOUT

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row (){
            CalendarView(navController, eventList)
        }
        Text(stringResource(R.string.upcoming_events), style = Typography.titleLarge)
        Row {
            ListView(eventList)
        }
    }
}

@Composable
private fun CalendarView(navController: NavController, eventList: MutableList<Event>){
    val scope = rememberCoroutineScope()

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) } // Adjust as needed
    val endMonth = remember { currentMonth.plusMonths(24) } // Adjust as needed
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() } // Available from the library

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    // START WEEK WITH MONDAY
    val daysOfWeek = daysOfWeek(firstDayOfWeek = firstDayOfWeek)
    val monthName: YearMonth = state.firstVisibleMonth.yearMonth

    val eventDateList = mutableListOf<EventDateType>()
    for (event in eventList) {
        eventDateList.add(EventDateType(CalendarDay(event.date, DayPosition.InDate), event.eventType))
        eventDateList.add(EventDateType(CalendarDay(event.date, DayPosition.MonthDate), event.eventType))
        eventDateList.add(EventDateType(CalendarDay(event.date, DayPosition.OutDate), event.eventType))
    }

    val openCalendarTap = remember { mutableStateOf(false) }
    val calendarTapDate = remember { mutableStateOf(LocalDate.now()) }

    // SHOW "GO BACK" ARROWS
    val arrowLeftVisible = rememberSaveable { mutableStateOf(false) }
    val arrowRightVisible = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.firstVisibleMonth){
        arrowLeftVisible.value = state.firstVisibleMonth.yearMonth.isAfter(YearMonth.now())
        arrowRightVisible.value = state.firstVisibleMonth.yearMonth.isBefore(YearMonth.now())
    }

    Column (modifier = Modifier.padding(dimensionResource(R.dimen.large_padding))){
        Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
            AnimatedVisibility(arrowLeftVisible.value){
                Card (colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)){
                    Icon(painterResource(R.drawable.ic_arrow_left), null, Modifier.clickable {
                        scope.launch { state.animateScrollToMonth(YearMonth.now()) }
                    })
                }
            }
            Text(
                modifier = Modifier.padding(dimensionResource(R.dimen.large_padding), 0.dp, dimensionResource(R.dimen.large_padding), 0.dp),
                style = Typography.titleLarge,
                text = "${monthName.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${monthName.year}"
            )
            AnimatedVisibility(arrowRightVisible.value){
                Card (colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)){
                    Icon(painterResource(R.drawable.ic_arrow_right), null, Modifier.clickable {
                        scope.launch { state.animateScrollToMonth(YearMonth.now()) }
                    })
                }
            }
        }
        Spacer(modifier = Modifier.size(8.dp))

        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                var eventType: EventType? = null
                for(eventDate in eventDateList) {
                    if(eventDate.date == day) {
                        eventType = eventDate.type
                        break
                    }
                }
                Day(day, day.date.equals(LocalDate.now()), eventType) { clicked ->
                    openCalendarTap.value = true; calendarTapDate.value = day.date
                } },
            monthHeader = { DaysOfWeekTitle(daysOfWeek = daysOfWeek) }
        )
    }

    if(openCalendarTap.value) CalendarTap(navController, calendarTapDate.value, eventList); openCalendarTap.value = false
}

@Composable
private fun CalendarTap(navController: NavController,date: LocalDate, eventList: MutableList<Event>) {
    var foundEvent = Event(null)

    for (event in eventList){
        if(date.isEqual(event.date)){
            foundEvent = event
            break
        }
    }

    if(foundEvent.id == null){
        showDialog.value = true
        tappedDate.value = date
    } else {
        eventDetailsEvent.value = foundEvent
        showEventDetails.value = true
    }
}

@Composable
private fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            )
        }
    }
}

@Composable
private fun Day(day: CalendarDay, isToday: Boolean, eventType: EventType? , onClick: (CalendarDay) -> Unit) {
    val context = LocalContext.current
    val backgroundColor = remember { mutableStateOf(Color.Unspecified) }

    if(eventType != null) {
        if(colorfulDaysEnabled.value) {
            backgroundColor.value = Color(context.resources.getColor(context.resources.getIdentifier("${eventType.toString().lowercase()}_full", "color", context.packageName)))
        } else {
            backgroundColor.value = MaterialTheme.colorScheme.primary
        }
    } else if(isToday) {
        backgroundColor.value = MaterialTheme.colorScheme.secondaryContainer
    } else {
        backgroundColor.value = Color.Unspecified
    }

    Box(Modifier
        .aspectRatio(1.3f) // This is important for square-sizing!
        .padding(5.dp)
        .clip(CircleShape)
        .background(color = backgroundColor.value)
        .clickable(
            enabled = true,
            onClick = { onClick(day) }
        ),
        contentAlignment = Alignment.Center
    ) {
        val textColor: Color?
        if(eventType != null && colorfulDaysEnabled.value){
            val color = backgroundColor.value.toArgb()
            textColor = if(color.red > 125 || color.green > 125 || color.blue > 125){
                Color.Black
            } else {
                Color.White
            }
        } else if(eventType != null){
            textColor = MaterialTheme.colorScheme.onPrimary
        } else {
            textColor = MaterialTheme.colorScheme.onBackground
        }

        Text(
            text = day.date.dayOfMonth.toString(),
            color = textColor,
            fontSize = 14.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListView(eventList: MutableList<Event>){
    val context = LocalContext.current
    val sortedList = eventList.sortedBy { it.date }
    val filteredList = mutableListOf<Event>()

    // SHOW ONLY THE NEXT 3 EVENTS
    for (event in sortedList) {
        if((event.date.isEqual(LocalDate.now()) || event.date.isAfter(LocalDate.now())) && filteredList.size < 3) {
            filteredList.add(event)
        }
    }

    val selectedEvent = remember { mutableStateOf(Event(null)) }
    if(filteredList.size > 0){
        LazyColumn {
            items(items = filteredList, itemContent = { item ->
                EventListItem(
                    event = item,
                    onClick = {
                        eventDetailsEvent.value = item
                        showEventDetails.value= true
                        selectedEvent.value = item
                    }
                )
            })
        }
    } else {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_empty_box))
        val progress by animateLottieCompositionAsState(composition)
        Box(modifier = Modifier.fillMaxSize().padding(0.dp, 0.dp, 0.dp, dimensionResource(R.dimen.fab_height_padding)),
            contentAlignment = Alignment.Center){
            LottieAnimation(
                composition = composition,
                progress = { progress },
            )
        }

    }
}

private fun EditEvent(navController: NavController, event: Event){
    navController.currentBackStackEntry?.savedStateHandle?.set("event", event)
    navController.navigate(NavRoutes.NewEvent.route)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun TopAppBar(navController: NavController, debugDialog: Unit){
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    CenterAlignedTopAppBar(
        title = {
            Row (verticalAlignment = Alignment.CenterVertically){
                Icon(
                    painterResource(R.drawable.header),
                    "header",
                    modifier = Modifier.graphicsLayer(alpha = 0.99f)
                        .drawWithCache {
                            val brush = Brush.horizontalGradient(listOf(primary, secondary))
                            onDrawWithContent {
                                drawContent()
                                drawRect(brush, blendMode = BlendMode.SrcAtop)
                            }
                        })
            }
        },
        actions = {
            Row {
                Box (modifier = Modifier.size(dimensionResource(R.dimen.icon_button_size)), contentAlignment = Alignment.Center){
                    Box (modifier = Modifier.clickable {
                        navController.navigate(NavRoutes.Search.route)
                    }) {
                        Icon(painterResource(R.drawable.ic_search), "search")
                    }
                }
                Box (modifier = Modifier.size(dimensionResource(R.dimen.icon_button_size)), contentAlignment = Alignment.Center){
                    Box(Modifier.combinedClickable (
                        onClick = {
                            navController.navigate(NavRoutes.Settings.route)
                        },
                        onLongClick = {
                            debugDialog
                            isDialogVisible.value = true
                        })) {
                        Icon(painterResource(R.drawable.ic_settings), "settings")
                    }
                }
            }
        }
    )
}

@Composable
private fun DebugDialog(viewModel: HomeViewModel){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val debugUpdateCheckEnabled = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(context){
        debugUpdateCheckEnabled.value = viewModel.getDebugUpdateCheck(context)
    }

    if(isDialogVisible.value && com.blazecode.eventtool.BuildConfig.DEBUG){
        AlertDialog(
            onDismissRequest = {isDialogVisible.value = false},
            modifier = Modifier.fillMaxWidth(),
            title = { Text("Debug Menu") },
            text = {
                Column (modifier = Modifier.fillMaxWidth()){
                    DefaultPreference(null, "POST_NOTIFICATION", null){
                        NotificationManager().postReminderNotification(context,
                            Event(id = 1, name = "TestName", additions = mutableListOf(Additions.FOTOBOX, Additions.LASER)))
                        isDialogVisible.value = false
                    }
                    SwitchPreference(null, "DEBUG_UPDATE_CHECK", null, debugUpdateCheckEnabled.value){ checked ->
                        debugUpdateCheckEnabled.value = checked
                        scope.launch { viewModel.setDebugUpdateCheck(context, debugUpdateCheckEnabled.value) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { OutlinedButton(onClick = { isDialogVisible.value = false }) { Text(stringResource(R.string.close)) } }
        )
    }
}

@Composable
private fun EventTypeChooser(navController: NavController, date: LocalDate?){
    val context = LocalContext.current
    val eventType: MutableState<EventType?> = rememberSaveable { mutableStateOf(null) }
    val showDialog = rememberSaveable { showDialog }

    if(showDialog.value) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.choose_event_type)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val eventTypes = EventType.values()
                    for (thisEventType in eventTypes) {
                        FilledTonalButton(onClick = { eventType.value = thisEventType },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp, 4.dp, 4.dp, 4.dp)
                        ) {
                            Text(text = stringResource(context.resources.getIdentifier(thisEventType.toString().lowercase(), "string", context.packageName))) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { OutlinedButton(onClick = { showDialog.value = false }) { Text(stringResource(R.string.cancel)) } }
        )

        if(eventType.value != null){
            val tempEvent = Event(id = null, eventType = eventType.value!!)
            if(date != null) tempEvent.date = date

            navController.currentBackStackEntry?.savedStateHandle?.set("event", tempEvent)
            navController.navigate(NavRoutes.NewEvent.route)
            showDialog.value = false
        }
    }
}

@Composable
private fun AddEventFAB(){
    ExtendedFloatingActionButton(
        icon = { Icon(Icons.Filled.Add,"") },
        text = { Text(stringResource(R.string.add_event)) },
        onClick = { showDialog.value = true; tappedDate.value = LocalDate.now() },
        elevation = FloatingActionButtonDefaults.elevation(8.dp))
}