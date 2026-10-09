package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ApartmentDao
import com.example.data.local.entities.ProjectEntity
import com.example.data.local.entities.RoomLayoutEntity
import com.example.data.local.entities.WallDimensionEntity

@Database(
    entities = [
        ProjectEntity::class,
        RoomLayoutEntity::class,
        WallDimensionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ApartmentDatabase : RoomDatabase() {

    abstract fun apartmentDao(): ApartmentDao

    companion object {
        @Volatile
        private var INSTANCE: ApartmentDatabase? = null

        fun getDatabase(context: Context): ApartmentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ApartmentDatabase::class.java,
                    "apartment_designs.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
