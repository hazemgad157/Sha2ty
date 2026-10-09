package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ApartmentDatabase
import com.example.data.local.entities.ProjectEntity
import com.example.data.local.entities.RoomLayoutEntity
import com.example.data.local.entities.WallDimensionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: ApartmentDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ApartmentDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("مصمم شقتي", appName)
    }

    @Test
    fun `test room database save and retrieve project entities`() = runBlocking {
        val dao = db.apartmentDao()

        val project = ProjectEntity(
            projectId = "p_101",
            name = "شقتي - التجمع",
            clientName = "حازم",
            totalFloorAreaM2 = 120.5f,
            totalEstimatedBudgetEgp = 350000.0,
            notes = "تصميم مودرن هادئ",
            snapshotJson = "{}"
        )

        val room = RoomLayoutEntity(
            roomId = "r_1",
            projectId = "p_101",
            roomName = "صالون ومعيشة",
            floorMaterial = "LARGE_PORCELAIN",
            tileSizeCm = 80,
            floorColorHex = 0xFFEDE8DF,
            ceilingHeightM = 2.85f,
            hasGypsumCove = true,
            calculatedAreaM2 = 32.5f,
            polygonPointsJson = "[]"
        )

        val wall = WallDimensionEntity(
            wallId = "w_1",
            projectId = "p_101",
            lengthMeters = 5.5f,
            thicknessMeters = 0.20f,
            heightMeters = 2.80f,
            startX = 1.0f,
            startY = 1.0f,
            endX = 6.5f,
            endY = 1.0f,
            innerColorHex = 0xFFEAE6DC,
            outerColorHex = 0xFFB0BEC5,
            paintBrandCode = "Jotun 1024",
            isAccent = false,
            hasBumpOut = true,
            bumpOutDepthM = 0.50f,
            openingsCount = 1
        )

        dao.saveFullProject(project, listOf(room), listOf(wall))

        val projects = dao.getAllProjects().first()
        assertEquals(1, projects.size)
        assertEquals("شقتي - التجمع", projects[0].name)

        val rooms = dao.getRoomsForProject("p_101").first()
        assertEquals(1, rooms.size)
        assertEquals("صالون ومعيشة", rooms[0].roomName)

        val walls = dao.getWallsForProject("p_101").first()
        assertEquals(1, walls.size)
        assertEquals(5.5f, walls[0].lengthMeters, 0.01f)
        assertEquals(0.50f, walls[0].bumpOutDepthM, 0.01f)
    }
}
