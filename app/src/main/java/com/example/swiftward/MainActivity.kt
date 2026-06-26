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

    // Deep-link intents from Khalti or "Return to App" button
    private var khaltiCallbackIntent: Intent? = null
    private var returnHomeIntent: Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent?.data
        when {
            uri?.scheme == "swiftward" && uri.host == "payment" -> khaltiCallbackIntent = intent
            uri?.scheme == "swiftward" && uri.host == "home"    -> returnHomeIntent = true
        }

        setContent {
            SwiftWardTheme {
                NavGraph(
                    sessionManager = sessionManager,
                    khaltiCallbackIntent = khaltiCallbackIntent,
                    navigateToHome = returnHomeIntent
                )
            }
        }
    }

    // Called when Khalti redirects back while app is already running (singleTask)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val uri = intent.data
        when {
            uri?.scheme == "swiftward" && uri.host == "payment" -> {
                khaltiCallbackIntent = intent
                recreate()
            }
            uri?.scheme == "swiftward" && uri.host == "home" -> {
                returnHomeIntent = true
                recreate()
            }
        }
    }
}