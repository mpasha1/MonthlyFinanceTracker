package com.example.monthlyfinancetracker.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CommitmentWithCategory(
    @Embedded val commitment: CommitmentEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity
)
