package com.borderrun.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.borderrun.app.navigation.BorderRunNavGraph
import com.borderrun.app.ui.theme.BorderRunTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host for the entire Border Run application.
 *
 * Annotated with [@AndroidEntryPoint][AndroidEntryPoint] so that Hilt can
 * inject members (e.g. ViewModels) into this activity and any hosted
 * composables that use [hiltViewModel][androidx.hilt.navigation.compose.hiltViewModel].
 *
 * Collects [MainViewModel.darkModeEnabled] and forwards it to [BorderRunTheme]
 * so the Material colour scheme updates reactively when the user toggles dark
 * mode in Settings.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkMode by mainViewModel.darkModeEnabled.collectAsState()
            BorderRunTheme(darkTheme = darkMode) {
                BorderRunNavGraph()
            }
        }
    }
}
