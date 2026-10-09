package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.ProjectEntity
import com.example.data.local.entities.RoomLayoutEntity
import com.example.data.local.entities.WallDimensionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApartmentDao {

    // Projects
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE projectId = :id LIMIT 1")
    fun getProjectById(id: String): Flow<ProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE projectId = :id")
    suspend fun deleteProjectById(id: String)

    // Room Layouts
    @Query("SELECT * FROM room_layouts WHERE projectId = :projectId")
    fun getRoomsForProject(projectId: String): Flow<List<RoomLayoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoomLayouts(rooms: List<RoomLayoutEntity>)

    @Query("DELETE FROM room_layouts WHERE projectId = :projectId")
    suspend fun deleteRoomsForProject(projectId: String)

    // Wall Dimensions
    @Query("SELECT * FROM wall_dimensions WHERE projectId = :projectId")
    fun getWallsForProject(projectId: String): Flow<List<WallDimensionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallDimensions(walls: List<WallDimensionEntity>)

    @Query("DELETE FROM wall_dimensions WHERE projectId = :projectId")
    suspend fun deleteWallsForProject(projectId: String)

    // Transaction for saving full project
    @Transaction
    suspend fun saveFullProject(
        project: ProjectEntity,
        rooms: List<RoomLayoutEntity>,
        walls: List<WallDimensionEntity>
    ) {
        insertProject(project)
        deleteRoomsForProject(project.projectId)
        insertRoomLayouts(rooms)
        deleteWallsForProject(project.projectId)
        insertWallDimensions(walls)
    }
}
