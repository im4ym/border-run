package com.borderrun.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borderrun.app.data.local.dao.UserPreferencesDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Application-scoped ViewModel owned by [MainActivity].
 *
 * Reads [darkModeEnabled] from [UserPreferencesDao] and exposes it as a
 * [StateFlow] so [MainActivity] can pass it down to [BorderRunTheme].
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    userPreferencesDao: UserPreferencesDao,
) : ViewModel() {

    /** `true` when the user has enabled dark mode in Settings. */
    val darkModeEnabled: StateFlow<Boolean> = userPreferencesDao.getPreferences()
        .map { it?.darkModeEnabled ?: false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false,
        )
}
