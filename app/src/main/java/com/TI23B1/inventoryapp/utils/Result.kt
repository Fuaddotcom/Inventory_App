package com.TI23B1.inventoryapp.utils

sealed class Result<out T> {
    object Loading : Result<Nothing>() // Indicates an ongoing operation
    data class Success<out T>(val data: T?) : Result<T>() // Operation successful with optional data
    data class Error(val exception: Exception, val message: String = "An error occurred") : Result<Nothing>() // Operation failed with error details
}