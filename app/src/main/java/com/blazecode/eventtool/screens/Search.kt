/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package com.blazecode.eventtool.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.blazecode.eventtool.ui.theme.EventToolTheme
import com.blazecode.eventtool.viewmodels.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(viewModel: SearchViewModel = viewModel(), navController: NavController) {
    val context = LocalContext.current

    EventToolTheme {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        Scaffold(
            topBar = { TopAppBar(viewModel, LocalContext.current, scrollBehavior, navController) },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            content = { paddingValues ->
                Column(modifier = Modifier.padding(paddingValues).verticalScroll(rememberScrollState())) {
                    MainLayout(viewModel, navController)
                }
            },
        )
    }
}

@Composable
private fun MainLayout(viewModel: SearchViewModel, navController: NavController){
    Column {
        Text(text = stringResource(R.string.search))
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