package com.bina.chat.domain.model

sealed class ModelAvailability {
    object Available : ModelAvailability()
    object Unavailable : ModelAvailability()
}