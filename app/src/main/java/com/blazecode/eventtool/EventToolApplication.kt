package com.blazecode.eventtool

import android.app.Application
import android.content.Context
import com.blazecode.eventtool.util.errors.ErrorReportActivity
import org.acra.config.coreConfiguration
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra

class EventToolApplication: Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        initAcra{
            coreConfiguration {
                withBuildConfigClass(BuildConfig::class.java)
                withReportFormat(StringFormat.JSON)
            }
            mailSender {
                mailTo = resources.getString(R.string.email_address)
                reportAsFile = true
                reportFileName = "error_report.json"
                subject = "EventTool - Error Report"
            }
            dialog {
                enabled = true
                reportDialogClass = ErrorReportActivity::class.java
            }
        }
    }
}