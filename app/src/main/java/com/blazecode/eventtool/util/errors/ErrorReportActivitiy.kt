package com.blazecode.eventtool.util.errors

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontFamily
import com.blazecode.eventtool.R
import com.blazecode.eventtool.ui.theme.EventToolTheme
import org.acra.ReportField
import org.acra.data.CrashReportData
import org.acra.dialog.CrashReportDialogHelper
import java.io.IOException

class ErrorReportActivity: AppCompatActivity() {

    private lateinit var helper: CrashReportDialogHelper

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        helper = CrashReportDialogHelper(this, intent)

        setContent {
            val userComment = remember { mutableStateOf("") }
            val isUserCommentError = remember { mutableStateOf(false) }
            val userEmail = remember { mutableStateOf("") }

            EventToolTheme {
                Box (modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AlertDialog(
                        onDismissRequest = {
                            helper.cancelReports()
                            finish()
                        },
                        title = { Text(text = getString(R.string.error_report_title)) },
                        text = {
                            Column {
                                StackTraceCard(getStackTrace())
                                OutlinedTextField(
                                    value = userComment.value,
                                    onValueChange = {
                                        userComment.value = it
                                        if(userComment.value.isNotEmpty())  isUserCommentError.value = false
                                    },
                                    label = { Text(text = getString(R.string.error_user_comment)) },
                                    isError = isUserCommentError.value,
                                )
                                OutlinedTextField(
                                    value = userEmail.value,
                                    onValueChange = { userEmail.value = it },
                                    label = { Text(text = getString(R.string.error_user_email)) }
                                )
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                if(userComment.value.isEmpty()){
                                    isUserCommentError.value = true
                                } else {
                                    helper.sendCrash(userComment.value, userEmail.value)
                                    finish()
                                }
                            }) {
                                Text(text = getString(R.string.send))
                            }
                        },
                        dismissButton = {
                            Button(onClick = {
                                helper.cancelReports()
                                finish()
                            }) {
                                Text(text = getString(R.string.cancel))
                            }
                        }
                    )
                }
            }
        }

    }

    @Composable
    private fun StackTraceCard(stackTrace: String) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface)) {
            val scrollState = rememberScrollState(0)
            Text(
                modifier = Modifier
                    .padding(dimensionResource(R.dimen.small_padding))
                    .horizontalScroll(scrollState),
                text = stackTrace,
                fontFamily = FontFamily.Monospace,
                maxLines = 5,
            )
        }
    }

    private fun getStackTrace(): String {
        val i = intent ?: return ""
        val crashReportData: CrashReportData = try {
            helper.reportData
        } catch (e: IOException) {
            return ""
        }
        return if(crashReportData.getString(ReportField.STACK_TRACE).isNullOrEmpty()) "" else crashReportData.getString(ReportField.STACK_TRACE)!!
    }

}