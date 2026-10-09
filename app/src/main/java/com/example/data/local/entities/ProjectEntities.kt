package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Main project entity storing project metadata, calculated KPIs, and full design payload.
 */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val projectId: String,
    val name: String,
    val clientName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val totalFloorAreaM2: Float = 0f,
    val totalEstimatedBudgetEgp: Double = 0.0,
    val notes: String = "",
    val snapshotJson: String // Complete JSON snapshot of walls, portals, rooms, furniture, and MEP
)

/**
 * Normalized Room Layout entity storing room polygon geometry, area, and finish specifications.
 */
@Entity(
    tableName = "room_layouts",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["projectId"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class RoomLayoutEntity(
    @PrimaryKey
    val roomId: String,
    val projectId: String,
    val roomName: String,
    val floorMaterial: String,
    val tileSizeCm: Int,
    val floorColorHex: Long,
    val ceilingHeightM: Float,
    val hasGypsumCove: Boolean,
    val calculatedAreaM2: Float,
    val polygonPointsJson: String
)

/**
 * Normalized Wall Dimension entity storing exact architectural parameters, thicknesses, and finishes.
 */
@Entity(
    tableName = "wall_dimensions",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["projectId"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class WallDimensionEntity(
    @PrimaryKey
    val wallId: String,
    val projectId: String,
    val lengthMeters: Float,
    val thicknessMeters: Float,
    val heightMeters: Float,
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val innerColorHex: Long,
    val outerColorHex: Long,
    val paintBrandCode: String,
    val isAccent: Boolean,
    val hasBumpOut: Boolean,
    val bumpOutDepthM: Float,
    val openingsCount: Int
)
