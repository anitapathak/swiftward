package com.example.swiftward

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import com.example.swiftward.ui.navigation.NavGraph
import com.swiftward.ui.theme.SwiftWardTheme
import com.swiftward.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var sessionManager: SessionManager

    // Holds the deep-link data from Khalti callback
    // e.g. swiftward://payment/callback?pidx=xxx&status=Completed&bookingId=SW-123
    private var khaltiCallbackIntent: Intent? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        khaltiCallbackIntent = intent?.takeIf { it.data?.scheme == "swiftward" }

        setContent {
            SwiftWardTheme {
                NavGraph(
                    sessionManager = sessionManager,
                    khaltiCallbackIntent = khaltiCallbackIntent
                )
            }
        }
    }

    // Called when Khalti redirects back while app is already running (singleTask)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.data?.scheme == "swiftward") {
            // Restart composition with the new intent
            khaltiCallbackIntent = intent
            recreate()
        }
    }
}