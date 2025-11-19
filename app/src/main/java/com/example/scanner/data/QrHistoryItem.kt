package com.example.scanner.data

data class QrHistoryItem(
    val id: Long,
    val value: String,
    val qrType: String,
    val qrImage: String,
    val timestamp: String,
    val jsonData: String,
    val isFavorite: Boolean = false
)
