package com.bina.chat.chat.domain.model

sealed class ModelAvailability {
    object Available : ModelAvailability()
    object Unavailable : ModelAvailability()
    object Downloadable : ModelAvailability()
}
