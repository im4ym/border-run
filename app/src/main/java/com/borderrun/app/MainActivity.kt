package com.borderrun.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.borderrun.app.navigation.BorderRunNavGraph
import com.borderrun.app.ui.theme.BorderRunTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host for the entire Border Run application.
 *
 * Annotated with [@AndroidEntryPoint][AndroidEntryPoint] so that Hilt can
 * inject members (e.g. ViewModels) into this activity and any hosted
 * composables that use [hiltViewModel][androidx.hilt.navigation.compose.hiltViewModel].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BorderRunTheme {
                BorderRunNavGraph()
            }
        }
    }
}
