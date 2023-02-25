/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package com.blazecode.eventtool.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.blazecode.eventtool.R
import com.blazecode.eventtool.data.Event
import com.blazecode.eventtool.navigation.NavRoutes
import com.blazecode.eventtool.ui.theme.EventToolTheme
import com.blazecode.eventtool.util.pdf.PdfPrinter
import com.blazecode.eventtool.viewmodels.SearchViewModel
import com.blazecode.eventtool.views.EventDetails
import com.blazecode.eventtool.views.EventListItem

private var showEventDetails = mutableStateOf(false)
private var eventDetailsEvent = mutableStateOf(Event(null))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(viewModel: SearchViewModel = viewModel(), navController: NavController, printer: PdfPrinter) {
    EventToolTheme {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

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
            topBar = { TopAppBar(viewModel, LocalContext.current, scrollBehavior, navController) },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            content = { paddingValues ->
                Box (modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    MainLayout(viewModel)
                }
            },
        )
    }
}


@Composable
private fun MainLayout(viewModel: SearchViewModel){
    val eventList = remember { mutableStateOf(listOf<Event>()) }

    // SEARCH
    LaunchedEffect(viewModel.searchText.value) {
        eventList.value = viewModel.searchEvents()
    }

    LazyColumn {
        item { SearchLayout(viewModel) }
        items(items = eventList.value, itemContent = { item ->
            EventListItem(
                event = item,
                onClick = {
                    eventDetailsEvent.value = item
                    showEventDetails.value = true
                }
            )
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchLayout(viewModel: SearchViewModel){
    val scope = rememberCoroutineScope()
    val searchText = remember { mutableStateOf("") }
    Column (modifier = Modifier.fillMaxWidth()){
        OutlinedTextField(
            value = searchText.value,
            onValueChange = {
                searchText.value = it
                viewModel.searchText.value = it
            },
            label = { Text(text = stringResource(R.string.search_hint)) },
            leadingIcon = {
                Icon(painterResource(R.drawable.ic_search), "search")
            },
            trailingIcon = {
                if (searchText.value.isNotEmpty()) {
                    Icon(
                        painterResource(R.drawable.ic_close),
                        "close",
                        modifier = Modifier.clickable {
                            searchText.value = ""
                            viewModel.searchText.value = ""
                        }
                    )
                }
            },
            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.medium_padding)).fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(viewModel: SearchViewModel, context: Context, scrollBehavior: TopAppBarScrollBehavior, navController: NavController){
    LargeTopAppBar(
        title = { Text(text = stringResource(R.string.search)) },
        navigationIcon = {
            Box (modifier = Modifier.size(dimensionResource(R.dimen.icon_button_size)).clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center){
                Icon(painterResource(R.drawable.ic_back), "back")
            }
        },
        scrollBehavior = scrollBehavior
    )
}

private fun EditEvent(navController: NavController, event: Event){
    navController.currentBackStackEntry?.savedStateHandle?.set("event", event)
    navController.navigate(NavRoutes.NewEvent.route)
}