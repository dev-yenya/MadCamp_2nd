package com.example.second_app

import android.content.Context
import androidx.room.*

// 로컬 DB를 쓰려고 사용하는 데이터 클래스.

@Entity(tableName="completed_levels")
data class CompletedLevels(
    @PrimaryKey
    val id: Int,
    val highscore: Double,

)

@Dao
interface CompletedLevelsDao {
    @Insert
    fun insert(data: CompletedLevels)

    @Update
    fun update(data: CompletedLevels)

    @Delete
    fun delete(data: CompletedLevels)

    @Query("SELECT highscore FROM completed_levels WHERE id=:id")
    fun getScore(id: Int): Double?
}

@Database(entities=[CompletedLevels::class], version=1)
abstract class CLDB: RoomDatabase() {
    abstract fun cldbDao(): CompletedLevelsDao

    companion object {
        private var instance: CLDB? = null

        @Synchronized
        fun getInstance(context: Context): CLDB? {
            if (instance == null) {
                synchronized(CLDB::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        CLDB::class.java,
                        "cldb"
                    ).build()
                }
            }
            return instance
        }
    }
}