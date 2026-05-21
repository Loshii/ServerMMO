package com.loshii.dndzerinx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope

class LocalProfileViewModel : ViewModel() {
    val scope: CoroutineScope = viewModelScope
}
