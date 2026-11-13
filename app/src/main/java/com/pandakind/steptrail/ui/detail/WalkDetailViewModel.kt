package com.pandakind.steptrail.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandakind.steptrail.domain.model.Walk
import com.pandakind.steptrail.domain.usecase.GetWalkByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel экрана деталей прогулки.
 */
@HiltViewModel
class WalkDetailViewModel @Inject constructor(
    private val getWalkByIdUseCase: GetWalkByIdUseCase
) : ViewModel() {

    private val _walk = MutableStateFlow<Walk?>(null)
    val walk: StateFlow<Walk?> = _walk

    /**
     * Загружает прогулку по ID.
     */
    fun loadWalk(walkId: Long) {
        viewModelScope.launch {
            getWalkByIdUseCase.execute(walkId)
                .catch { emit(null) }
                .collect { w ->
                    _walk.value = w
                }
        }
    }
}
