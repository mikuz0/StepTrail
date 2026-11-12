package com.pandakind.steptrail.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandakind.steptrail.domain.model.Walk
import com.pandakind.steptrail.domain.usecase.GetWalksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel экрана журнала прогулок.
 */
@HiltViewModel
class JournalViewModel @Inject constructor(
    getWalksUseCase: GetWalksUseCase
) : ViewModel() {

    val walks: StateFlow<List<Walk>> = getWalksUseCase.execute()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
