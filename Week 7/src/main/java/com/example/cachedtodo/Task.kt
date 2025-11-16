package com.example.cachedtodo

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),

    var title: String = "",
    var isCompleted: Boolean = false,

    var isSynced: Boolean = false,
    var isDeleted: Boolean = false
) {

    constructor() : this(UUID.randomUUID().toString(), "", false, false, false)
}