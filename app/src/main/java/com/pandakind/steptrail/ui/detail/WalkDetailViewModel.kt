package com.pandakind.steptrail.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandakind.steptrail.domain.model.Walk
import com.pandakind.steptrail.domain.usecase.ObserveCurrentWalkUseCase
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
    private val observeCurrentWalkUseCase: ObserveCurrentWalkUseCase
) : ViewModel() {

    private val _walk = MutableStateFlow<Walk?>(null)
    val walk: StateFlow<Walk?> = _walk

    /**
     * Загружает прогулку по ID.
     * TODO: добавить отдельный use case GetWalkByIdUseCase для полной загрузки.
     * Сейчас используется observeCurrentWalk для демонстрации.
     */
    fun loadWalk(walkId: Long) {
        viewModelScope.launch {
            // TODO: Реализовать GetWalkByIdUseCase
            // Пока просто обновляем текущую прогулку
            observeCurrentWalkUseCase.execute()
                .catch { emit(null) }
                .collect { w ->
                    if (w?.id == walkId) {
                        _walk.value = w
                    }
                }
        }
    }
}
