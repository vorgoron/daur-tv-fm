package com.vorgoron.daurtv

data class PlayerUiState(
    val isPlaying: Boolean,
    val isLoading: Boolean,
    val title: String?,
    val error: String?
)
