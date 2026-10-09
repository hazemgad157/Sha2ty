package com.example.data.local

import com.example.data.local.dao.ApartmentDao
import com.example.data.local.entities.ProjectEntity
import com.example.data.local.entities.RoomLayoutEntity
import com.example.data.local.entities.WallDimensionEntity
import kotlinx.coroutines.flow.Flow

class ApartmentRepository(private val dao: ApartmentDao) {

    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()

    fun getProjectById(id: String): Flow<ProjectEntity?> = dao.getProjectById(id)

    suspend fun saveProject(
        project: ProjectEntity,
        rooms: List<RoomLayoutEntity>,
        walls: List<WallDimensionEntity>
    ) {
        dao.saveFullProject(project, rooms, walls)
    }

    suspend fun deleteProject(projectId: String) {
        dao.deleteProjectById(projectId)
    }
}
