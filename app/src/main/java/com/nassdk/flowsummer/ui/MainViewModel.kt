package com.nassdk.flowsummer.ui

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nassdk.flowsummer.domain.MainUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class MainViewState(val sum: String, val inputState: String)

@Immutable
class MainViewModel(private val useCase: MainUseCase) : ViewModel() {

    private val _viewState = MutableStateFlow(MainViewState("", ""))
    val viewState: StateFlow<MainViewState>
        get() = _viewState.asStateFlow()

    fun updateInputState(newValue: String) {
        _viewState.update { it.copy(inputState = newValue) }
    }

    fun calculateSum() {
        _viewState.update { it.copy(sum = "") }

        if (_viewState.value.inputState.isBlank()) return


        useCase.getSumsFlow(n = _viewState.value.inputState.replace(" ", "").toInt())
            .buffer()
            .flatMapConcat { value ->
                flow {
                    emit(value)
                    delay(100)
                }
            }
            .onEach { newSum ->
                Log.d("MainViewModel", "${System.currentTimeMillis()}")
                _viewState.update { oldState ->
                    oldState.copy(sum = oldState.sum.plus("$newSum "))
                }
            }.launchIn(scope = viewModelScope)
    }
}