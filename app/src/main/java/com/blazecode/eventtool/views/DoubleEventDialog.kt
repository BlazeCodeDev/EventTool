package com.blazecode.eventtool.views

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.blazecode.eventtool.R
import com.blazecode.eventtool.data.Event
import com.blazecode.eventtool.enums.EventType

@Composable
fun DoubleEventDialog(context: Context, eventList: MutableList<Event>, onDismiss : () -> Unit, onClickEvent1 : () -> Unit, onClickEvent2 : () -> Unit) {
    val eventName1 =
        if(eventList[0].eventType == EventType.WEDDING) {
            "${eventList[0].firstName1} / ${eventList[0].firstName2} ${eventList[0].lastName}"
        } else if(eventList[0].eventType == EventType.RESERVED) {
            eventList[0].comments
        } else {
            eventList[0].name
        }

    val eventName2 =
        if(eventList[1].eventType == EventType.WEDDING) {
            "${eventList[1].firstName1} / ${eventList[1].firstName2} ${eventList[1].lastName}"
        } else if(eventList[1].eventType == EventType.RESERVED) {
            eventList[1].comments
        } else {
            eventList[1].name
        }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.choose_event)) },
        text = {
            Column {
                FilledTonalButton(
                    onClick = { onClickEvent1() },
                    modifier = Modifier
                        .fillMaxWidth())
                {
                    Text(eventName1)
                }
                FilledTonalButton(
                    onClick = { onClickEvent2() },
                    modifier = Modifier
                        .fillMaxWidth())
                {
                    Text(eventName2)
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = { onDismiss() }) {
                Text(stringResource(R.string.cancel))
            }
        })
}