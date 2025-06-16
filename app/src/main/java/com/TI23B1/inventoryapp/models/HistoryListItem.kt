package com.TI23B1.inventoryapp.models

sealed class HistoryListItem {
    data class DateHeader(val date: String) : HistoryListItem()
    data class HistoryItem(val recentItem: RecentItem) : HistoryListItem()
}